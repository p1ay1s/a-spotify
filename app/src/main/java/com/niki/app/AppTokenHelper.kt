package com.niki.app

import com.niki.app.App.Companion.OFDSeconds
import com.niki.app.App.Companion.accessToken
import com.niki.app.App.Companion.lastSet
import com.niki.app.App.Companion.prefAccessToken
import com.niki.app.App.Companion.prefLastTokenSet
import com.niki.app.App.Companion.prefOFD
import com.niki.app.App.Companion.prefRefreshToken
import com.niki.app.App.Companion.refreshToken
import com.niki.app.net.ErrorData
import com.niki.app.net.auth.AuthApi
import com.niki.app.net.auth.TokenResponse
import com.niki.app.net.handleRequest
import com.niki.app.net.web_api.TestApi
import com.niki.app.util.MVI
import com.zephyr.base.log.logE
import com.zephyr.util.getValue
import com.zephyr.util.putValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed class TokenIntent {
    data object Refresh : TokenIntent()
    class GetWithCode(val code: String) : TokenIntent()
}

data class TokenState(
    val isAvailable: Boolean = false,
    val isRefreshing: Boolean = false,
    val isGetting: Boolean = false
)

sealed class TokenEffect {
    data object CodeNeeded : TokenEffect() // 当刷新失败或没有 refresh token 等情况, 必须重新获取 code 时发送
    class RequestError(val error: ErrorData) : TokenEffect()
}

object AppTokenHelper :
    MVI<TokenIntent, TokenState, TokenEffect>(CoroutineScope(SupervisorJob() + Dispatchers.IO)) {
    override val TAG
        get() = this::class.java.simpleName

    override fun getInitState(): TokenState = TokenState()

    override fun handleIntent(intent: TokenIntent) {
        when (intent) {
            TokenIntent.Refresh -> refreshTokens()
            is TokenIntent.GetWithCode -> getTokensWithCode(intent.code)
        }
    }

    private var putTokensMutex = Mutex()
    private var checkJob: Job? = null

    private val authApi: AuthApi by lazy { AuthApi() }
    private val testApi: TestApi by lazy { TestApi() }


    fun collectEffect(coroutineScope: CoroutineScope, block: (TokenEffect) -> Any) =
        coroutineScope.launch {
            uiEffectFlow.collect { block(it) }
        }

    fun observeState(coroutineScope: CoroutineScope, observe: suspend Flow<TokenState>.() -> Unit) {
        observeState {
            coroutineScope.launch {
                observe()
            }
        }
    }

    fun startCheckJob() {
        checkJob?.cancel()
        checkJob = scope.launch(Dispatchers.IO) {
            while (true) {
                if (isOFD() && !uiStateFlow.value.isRefreshing && !uiStateFlow.value.isGetting) {
                    logE(TAG, "token 过期, 将刷新")
                    refreshTokens()
                }
                delay(500)
            }
        }
    }

    /**
     * 通过 refresh token 请求新的 token
     */
    private fun refreshTokens() {
        if (uiStateFlow.value.isRefreshing)
            return
        updateState { copy(isRefreshing = true) }

        if (refreshToken.isBlank()) {
            sendEffect(TokenEffect.CodeNeeded)
            return
        }

        authApi.service.refreshToken(refreshToken)
            .handleRequest(
                onSuccess = { tokens ->
                    scope.launch {
                        tokens.handleOnSuccess()
                    }
                },
                onError = { error ->
                    if (error != null)
                        logE(TAG, "refresh token 请求失败")
                },
                onFinish = {
                    updateState { copy(isRefreshing = false) }
                }
            )
    }

    private fun getTokensWithCode(authCode: String) {
        if (uiStateFlow.value.isGetting)
            return
        updateState { copy(isGetting = true) }

        authApi.service.getTokenWithCode(authCode)
            .handleRequest(
                onSuccess = { tokens ->
                    scope.launch {
                        tokens.handleOnSuccess()
                    }
                },
                onError = {
                    it.handleOnError()
                },
                onFinish = {
                    updateState { copy(isGetting = false) }
                }
            )
    }

    private fun TokenResponse?.handleOnSuccess() {
        scope.launch {
            val access = this@handleOnSuccess?.accessToken
            val refresh = this@handleOnSuccess?.refreshToken
            putTokens(access, refresh).await()

            updateState { copy(isAvailable = true) }
            logE(TAG, "success:\naccess token: $access\nrefresh token: $refresh")
        }
    }

    private fun ErrorData?.handleOnError() {
        if (this == null) return
        logE(TAG, "token 请求失败")
        sendEffect(TokenEffect.RequestError(this))
    }

    suspend fun loadPrefs() = coroutineScope {
        async {
            lastSet = prefLastTokenSet.getValue(0L)!!
            OFDSeconds = prefOFD.getValue(3600L)!!
            accessToken = prefAccessToken.getValue("")!!
            refreshToken = prefRefreshToken.getValue("")!!
        }
    }

    private suspend fun putTokens(access: String?, refresh: String?) = coroutineScope {
        async {
            putTokensMutex.withLock {
                access?.let { a ->
                    accessToken = a
                    prefAccessToken.putValue(a)
                }
                refresh?.let { r ->
                    refreshToken = r
                    prefRefreshToken.putValue(r)
                }
                lastSet = System.currentTimeMillis()
                prefLastTokenSet.putValue(lastSet)
                prefOFD.putValue(OFDSeconds)
            }
        }
    }

    fun notifyUnavailable() {
        updateState { copy(isAvailable = false) }
    }

    fun checkTokens() {
        if (isOFD())
            refreshTokens()
        else {
            scope.launch {
                val ok = testApi.testRequest()
                if (!ok)
                    refreshTokens()
                else
                    updateState { copy(isAvailable = true) }
            }
        }
    }

    /**
     * 是否过期, 此处多算了 5s 提前更新
     */
    private fun isOFD() = System.currentTimeMillis() - lastSet + 5000 > OFDSeconds * 1000
}