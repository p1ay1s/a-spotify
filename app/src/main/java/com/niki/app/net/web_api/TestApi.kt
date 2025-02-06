package com.niki.app.net.web_api

import com.niki.app.App
import com.niki.app.net.createService
import com.niki.spotify.remote.log
import com.niki.spotify.remote.logS
import com.niki.spotify.web.LIMIT
import com.niki.spotify.web.OFFSET
import com.niki.spotify.web.models.NewReleases
import kotlinx.coroutines.coroutineScope
import okhttp3.Interceptor
import retrofit2.Call
import retrofit2.awaitResponse
import retrofit2.http.GET
import retrofit2.http.QueryMap

class TestApi {
    companion object {
        private const val BASE_URL = "https://api.spotify.com/v1/"
    }

    interface TestService {
        @GET("browse/new-releases")
        fun getNewReleases(@QueryMap options: Map<String, String> = emptyMap()): Call<NewReleases>
    }

    private val service
            by lazy { createService<TestService>(BASE_URL, createAuthInterceptor()) }

    private var listener: (() -> Unit)? = null

    suspend fun testRequest(): Boolean = coroutineScope {
        try {
            val response = service.getNewReleases(
                mapOf(
                    LIMIT to "1",
                    OFFSET to "0"
                )
            ).awaitResponse()
            logS("test-api: ${response.code()}")
            response.isSuccessful
        } catch (t: Throwable) {
            t.log("")
            false
        }
    }

    private fun createAuthInterceptor() = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .apply { // 拦截器的逻辑是在每次请求时都执行
                if (App.accessToken.isNotBlank()) {
                    addHeader("Authorization", "Bearer ${App.accessToken}")
                } else
                    listener?.invoke()
            }
            .build()
        chain.proceed(request)
    }
}