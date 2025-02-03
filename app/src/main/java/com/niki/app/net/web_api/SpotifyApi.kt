package com.niki.app.net.web_api

import com.niki.app.net.createService
import com.niki.app.util.appAccess
import okhttp3.Interceptor

class SpotifyApi {
    companion object {
        private const val BASE_URL = "https://api.spotify.com/v1/"
    }

//    private val clazz = kaaes.spotify.webapi.android.SpotifyService::class.java

    val service by lazy { createService<SService>(BASE_URL, createAuthInterceptor()) }
    private var listener: (() -> Unit)? = null


    private fun setOnErrorListener(listener: (() -> Unit)?) {
        this.listener = listener
    }

    private fun createAuthInterceptor() = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .apply { // 拦截器的逻辑是在每次请求时都执行
                if (appAccess.isNotBlank())
                    addHeader("Authorization", "Bearer $appAccess")
                else
                    listener?.invoke()
            }
            .build()
        chain.proceed(request)
    }
}