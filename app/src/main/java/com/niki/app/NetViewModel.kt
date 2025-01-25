package com.niki.app

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niki.app.net.AuthModel
import com.niki.app.net.SpotifyModel
import com.zephyr.base.extension.TAG
import com.zephyr.base.log.logE
import com.zephyr.util.getValue
import com.zephyr.util.putValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NetViewModel : ViewModel() {
    companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_LAST_TOKEN_SET = "last_token_set"

        const val TOKEN_NETWORK_PROBLEM = 0
        const val TOKEN_OK = 1
        const val TOKEN_FAILED = 2
    }

    private var isRefreshingTokens = false
    private var isGettingTokens = false

    private var watchTokensJob: Job? = null

    private val authModel = AuthModel()
    private val spotifyModel = SpotifyModel()

    private val prefAccessToken = stringPreferencesKey(KEY_ACCESS_TOKEN)
    private val prefRefreshToken = stringPreferencesKey(KEY_REFRESH_TOKEN)
    private val prefLastTokenSet = longPreferencesKey(KEY_LAST_TOKEN_SET)

    fun checkTokens(callback: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            appLastSet = prefLastTokenSet.getValue(0L)!!

            appAccess = prefAccessToken.getValue("")!!
            appRefresh = prefRefreshToken.getValue("")!!

            if (appRefresh.isNotBlank()) {
                if (System.currentTimeMillis() - appLastSet <= 60 * 60 * 1000)
                    callback(TOKEN_OK)
                else
                    refreshTokens { callback(it) }
            } else {
                callback(TOKEN_FAILED)
            }
        }
    }

    private fun refreshTokens(callback: (Int) -> Unit) {
        if (isRefreshingTokens)
            return
        isRefreshingTokens = true
        authModel.refreshToken(appRefresh,
            {
                val access = it?.accessToken
                val refresh = it?.refreshToken
                if (access != null && refresh != null) {
                    putTokens(access, refresh)
                    callback(TOKEN_OK)
                } else {
                    logE(TAG, access + "\n" + refresh)
                    callback(TOKEN_FAILED)
                }
                isRefreshingTokens = false
            },
            { code, _ ->
                if (code != null)
                    callback(TOKEN_FAILED)
                else
                    callback(TOKEN_NETWORK_PROBLEM)
                isRefreshingTokens = false
            }
        )
    }

    fun getTokensWithCode(code: String, callback: ((Boolean) -> Unit) = {}) {
        if (isGettingTokens)
            return
        isGettingTokens = true
        authModel.getAccessToken(code,
            {
                val access = it?.accessToken
                val refresh = it?.refreshToken
                if (access != null && refresh != null) {
                    putTokens(access, refresh)
                    callback(true)
                } else {
                    callback(false)
                }
                isGettingTokens = false
            },
            { c, _ ->
                if (c != null) callback(false)
                isGettingTokens = false
            }
        )
    }

    fun putTokens(access: String, refresh: String) {
        viewModelScope.launch(Dispatchers.IO) {
            appAccess = access
            appRefresh = refresh
            appLastSet = System.currentTimeMillis()
            prefAccessToken.putValue(appAccess)
            prefRefreshToken.putValue(appRefresh)
            prefLastTokenSet.putValue(appLastSet)
        }
    }

    fun startWatchTokenDateJob() {
        watchTokensJob?.cancel()
        watchTokensJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                if (appRefresh.isNotBlank() && System.currentTimeMillis() - appLastSet > 60 * 60 * 1000)
                    refreshTokens { }
                delay(500)
            }
        }
    }
}