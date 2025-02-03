package com.niki.app.net.new

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

const val API_BASE_URL = "https://api.spotify.com/v1/"
const val READ_TIMEOUT: Long = 15
const val CONNECT_TIMEOUT: Long = 30

object SpotifyApi {
    // 决定了 service 的类型
//    private val clazz = kaaes.spotify.webapi.android.SpotifyService::class.java
    private val clazz = SService::class.java

    @Volatile
    var accessToken = ""
    val service by lazy { createSpotifyService() }
    private var listener: (() -> Unit)? = null


    private fun setOnErrorListener(listener: (() -> Unit)?) {
        SpotifyApi.listener = listener
    }

    private fun createSpotifyService() = Retrofit.Builder()
        .baseUrl(API_BASE_URL)
        .client(createClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(clazz)

    private fun createClient() = OkHttpClient.Builder()
        .addInterceptor(createAuthInterceptor())
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .build()

    private fun createAuthInterceptor() = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .apply { // 拦截器的逻辑是在每次请求时都执行
                if (accessToken.isNotBlank())
                    addHeader("Authorization", "Bearer $accessToken")
                else
                    listener?.invoke()
            }
            .build()
        chain.proceed(request)
    }
}