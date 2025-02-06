package com.niki.app.net

import com.zephyr.util.net.Error
import com.zephyr.util.net.Success
import com.zephyr.util.net.requestEnqueue
import com.zephyr.util.toJsonClass
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


const val READ_TIMEOUT: Long = 15
const val CONNECT_TIMEOUT: Long = 30

fun <T> Call<T>.handleRequest(
    onSuccess: ((T?) -> Unit)? = null,
    onError: ((ErrorData?) -> Unit)? = null,
    onFinish: (() -> Unit)? = null
) = requestEnqueue { result ->
    try {
        when (result) {
            is Success -> onSuccess?.invoke(result.data)
            is Error -> when (result.code) {
                null ->
                    onError?.invoke(null)

                else -> {
                    val response = result.msg.toJsonClass<ErrorResponse>()
                    val error = response?.error
                    onError?.invoke(error)
                }
            }
        }
    } finally {
        onFinish?.invoke()
    }
}

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
