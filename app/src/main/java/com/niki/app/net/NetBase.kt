package com.niki.app.net

import com.zephyr.util.net.handleResult
import com.zephyr.util.net.requestEnqueue
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

const val READ_TIMEOUT: Long = 15
const val CONNECT_TIMEOUT: Long = 30

inline fun <T> Call<T>.request(
    crossinline onSuccess: (T?) -> Unit,
    crossinline onError: (Int?, String) -> Unit
) = requestEnqueue { it.handleResult(onSuccess, onError) }

inline fun <reified T> createService(baseurl: String, interceptor: Interceptor? = null) =
    Retrofit.Builder()
        .baseUrl(baseurl)
        .client(createClient(interceptor))
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(T::class.java)

fun createClient(interceptor: Interceptor? = null) = OkHttpClient.Builder()
    .apply {
        interceptor?.let { addInterceptor(it) }
    }
    .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
    .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
    .build()
