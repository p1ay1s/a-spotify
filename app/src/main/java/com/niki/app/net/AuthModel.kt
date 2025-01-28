package com.niki.app.net

import com.niki.app.util.CLIENT_ID
import com.niki.app.util.CLIENT_SECRET
import com.niki.app.util.REDIRECT_URI
import com.zephyr.util.ServiceBuilder
import com.zephyr.util.ServiceBuilder.requestEnqueue
import com.zephyr.util.toBase64String

class AuthModel {

    val authService: AuthService by lazy { ServiceBuilder.create<AuthService>("https://accounts.spotify.com/") }

    val base64Credentials: String = "Basic " + toBase64String("$CLIENT_ID:$CLIENT_SECRET")

    inline fun getAccessToken(
        code: String,
        crossinline onSuccess: (TokenResponse?) -> Unit,
        crossinline onError: (Int?, String) -> Unit
    ) {
        requestEnqueue(
            authService.getAccessToken(
                code = code,
                redirectUri = REDIRECT_URI,
                authorization = base64Credentials
            ), onSuccess, onError
        )
    }

    inline fun refreshToken(
        refreshToken: String,
        crossinline onSuccess: (TokenResponse?) -> Unit,
        crossinline onError: (Int?, String) -> Unit
    ) {
        requestEnqueue(
            authService.refreshToken(
                refreshToken = refreshToken,
                authorization = base64Credentials
            ),
            onSuccess,
            onError
        )
    }
}