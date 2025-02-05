package com.niki.app

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niki.app.net.auth.AuthApi
import com.niki.spotify.web.request
import com.niki.spotify.web.SpotifyApi
import com.niki.app.util.appAccess
import com.niki.app.util.appLastSet
import com.niki.app.util.appOFD
import com.niki.app.util.appRefresh
import com.zephyr.base.extension.TAG
import com.zephyr.base.log.logE
import com.zephyr.util.getValue
import com.zephyr.util.putValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed class NetEffect
sealed class NetIntent

data object TokenInitOk : NetEffect()
data object GetSpotifyCode : NetEffect()
class TokenRequestError(val code: Int, val msg: String) : NetEffect()
data object TokensRefreshed : NetEffect() // 成功刷新 token

class GetTokensWithCode(val code: String) : NetIntent()
data object RequireRefreshTokens : NetIntent()
data object DevApiTest : NetIntent()

/**
 * 使用 mvi 架构, 专用于处理有关 spotify 的请求
 *
 * 没有实现 state 相关部分, 因为网络请求使用 effect 和 intent 基本可以满足需求
 */
class NetViewModel : ViewModel() {
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_OFD = "out_of_date"
        private const val KEY_LAST_TOKEN_SET = "last_token_set"

        private val prefAccessToken = stringPreferencesKey(KEY_ACCESS_TOKEN)
        private val prefRefreshToken = stringPreferencesKey(KEY_REFRESH_TOKEN)
        private val prefOFD = longPreferencesKey(KEY_OFD)
        private val prefLastTokenSet = longPreferencesKey(KEY_LAST_TOKEN_SET)
    }

    private var autoRefreshTokensJob: Job? = null
    private var putTokensMutex = Mutex()

    private var isTokensRefreshing = false
    private var isGettingTokens = false

    private val authApi = AuthApi()
    private val spotifyApi = SpotifyApi()

    // { MVI 样板代码
    private val mviChannel = Channel<NetIntent>(Channel.UNLIMITED)

    private val _effectFlow = MutableSharedFlow<NetEffect>()
    val uiEffectFlow: SharedFlow<NetEffect> by lazy { _effectFlow.asSharedFlow() }

    init { // 启动 channel 收集
        loadPrefs {
            startWatchTokenDateJob()
        }
        viewModelScope.launch(Dispatchers.IO) {
            mviChannel.consumeAsFlow().collect { intent ->
                logE(TAG, "接受 intent: ${intent::class.java.simpleName}")
                handleIntent(intent)
            }
        }
    }

    /**
     * channel 收集到后处理 intent
     */
    private fun handleIntent(intent: NetIntent) {
        when (intent) {
            is GetTokensWithCode -> getTokensWithCode(intent.code)
            RequireRefreshTokens -> refreshTokens()
            DevApiTest -> devApiTest()
        }
    }

    /**
     * view 通过 effect 向 view model 层发送数据
     */
    fun sendIntent(intent: NetIntent) = viewModelScope.launch(Dispatchers.IO) {
        logE(TAG, "发送 intent: ${intent::class.java.simpleName}")
        mviChannel.send(intent)
    }

    /**
     * view model 通过 effect 向 view 层发送数据
     */
    private fun sendEffect(effect: NetEffect) = viewModelScope.launch(Dispatchers.IO) {
        logE(TAG, "发送 effect: ${effect::class.java.simpleName}")
        _effectFlow.emit(effect)
    }
    // }

    private fun devApiTest() {
////        spotifyApi.recommendationsService.getRecommendations(
//            spotifyApi.browseService.getNewReleases(
////            mapOf(
////                "seed_artists" to "4NHQUGzhtTLFvgF5SZesLK",
////                "seed_genres" to "classical,country",
////                "seed_tracks" to "0c6xIDDpzE81m2q797ordA"
////            )
//        )
//            .request(
//                onSuccess = { r ->
//                    r?.let {
//                        r.albums
//                    }
//                },
//                onError = { _, _ -> }
//            )
    }

    private fun loadPrefs(callback: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            loadPrefs().await()
            if (!isOFD() && appAccess.isNotBlank())
                sendEffect(TokenInitOk)
            callback()
        }
    }

    private suspend fun loadPrefs() = coroutineScope {
        async {
            appLastSet = prefLastTokenSet.getValue(0L)!!
            appOFD = prefOFD.getValue(3600L)!!
            appAccess = prefAccessToken.getValue("")!!
            appRefresh = prefRefreshToken.getValue("")!!
        }
    }

    /**
     * 发送 refresh tokens error
     */
    private fun refreshTokens() {
        if (isTokensRefreshing)
            return
        isTokensRefreshing = true

        if (appRefresh.isBlank()) {
            sendEffect(GetSpotifyCode)
            return
        }

        authApi.service.refreshToken(appRefresh)
            .request(
                onSuccess = { tokens ->
                    val access = tokens?.accessToken
                    val refresh = tokens?.refreshToken

                    if (!access.isNullOrBlank() && !refresh.isNullOrBlank())
                        putTokens(access, refresh)
                    else
                        sendEffect(GetSpotifyCode)
                    logE(TAG, "get new tokens:\naccess token: $access\nrefresh token: $refresh")
                    isTokensRefreshing = false
                },
                onError = { code, msg ->
                    isTokensRefreshing = false
                    if (code == null)
                        refreshTokens() // 请求失败
                    else
                        sendEffect(TokenRequestError(code, msg))
                }
            )
    }

    private fun getTokensWithCode(authCode: String) {
        if (isGettingTokens)
            return
        isGettingTokens = true
        authApi.service.getTokenWithCode(authCode)
            .request(
                onSuccess = { tokens ->
                    val access = tokens?.accessToken
                    val refresh = tokens?.refreshToken

                    if (!access.isNullOrBlank() && !refresh.isNullOrBlank())
                        putTokens(access, refresh)
                    else
                        sendEffect(GetSpotifyCode)
                    logE(TAG, "get new tokens:\naccess token: $access\nrefresh token: $refresh")
                    isGettingTokens = false
                },
                onError = { code, msg ->
                    isGettingTokens = false
                    if (code == null)
                        refreshTokens() // 请求失败
                    else
                        sendEffect(TokenRequestError(code, msg))
                }
            )
    }


    /**
     * 发送 token refreshed
     */
    private fun putTokens(access: String, refresh: String) {
        viewModelScope.launch(Dispatchers.IO) {
            putTokensMutex.withLock {
                appAccess = access
                appRefresh = refresh
                appLastSet = System.currentTimeMillis()
                prefAccessToken.putValue(appAccess)
                prefRefreshToken.putValue(appRefresh)
                prefOFD.putValue(appOFD)
                prefLastTokenSet.putValue(appLastSet)
                sendEffect(TokensRefreshed)
            }
        }
    }

    private fun startWatchTokenDateJob() {
        autoRefreshTokensJob?.cancel()
        autoRefreshTokensJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                if (isOFD() && !isTokensRefreshing) {
                    logE(TAG, "token 过期, 将刷新")
                    refreshTokens()
                }
                delay(500)
            }
        }
    }

    /**
     * 是否过期, 此处多算了 5s 提前更新
     */
    private fun isOFD() = System.currentTimeMillis() - appLastSet + 5000 > appOFD * 1000
}