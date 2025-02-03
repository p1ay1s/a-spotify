package com.niki.app.net

import com.niki.spotify_objs.CLIENT_ID
import com.niki.spotify_objs.CLIENT_SECRET
import com.niki.spotify_objs.REDIRECT_URI
import com.zephyr.util.net.ServiceBuilder
import com.zephyr.util.net.handleResult
import com.zephyr.util.net.requestEnqueue
import com.zephyr.util.toBase64String

class AuthModel {

    val authService: AuthService by lazy { ServiceBuilder.create<AuthService>("https://accounts.spotify.com/") }

    val base64Credentials: String = "Basic " + toBase64String("$CLIENT_ID:$CLIENT_SECRET")

    inline fun getAccessToken(
        code: String,
        crossinline onSuccess: (TokenResponse?) -> Unit,
        crossinline onError: (Int?, String) -> Unit
    ) {
        authService.getAccessToken("authorization_code", code, REDIRECT_URI, base64Credentials)
            .requestEnqueue {
                it.handleResult(onSuccess, onError)
            }
    }

    inline fun refreshToken(
        refreshToken: String,
        crossinline onSuccess: (TokenResponse?) -> Unit,
        crossinline onError: (Int?, String) -> Unit
    ) {
        authService.refreshToken("refresh_token", refreshToken, base64Credentials)
            .requestEnqueue {
                it.handleResult(onSuccess, onError)
            }
    }
}