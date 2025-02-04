package com.niki.app.net.auth

import com.niki.app.net.createService
import com.niki.spotify.remote.CLIENT_ID
import com.niki.spotify.remote.CLIENT_SECRET
import com.zephyr.util.toBase64String
import okhttp3.Interceptor

class AuthApi {
    companion object {
        private const val BASE_URL = "https://accounts.spotify.com/"
    }

    private val base64Credentials: String = "Basic " + toBase64String("${com.niki.spotify.remote.CLIENT_ID}:${com.niki.spotify.remote.CLIENT_SECRET}")

    val service by lazy {
        createService<AuthService>(
            BASE_URL,
            createAuthInterceptor()
        )
    }

    private fun createAuthInterceptor() = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Authorization", base64Credentials)
            .build()
        chain.proceed(request)
    }
}