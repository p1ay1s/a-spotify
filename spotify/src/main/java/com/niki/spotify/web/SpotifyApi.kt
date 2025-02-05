package com.niki.spotify.web

import com.niki.spotify.web.service.AlbumsService
import com.niki.spotify.web.service.ArtistsService
import com.niki.spotify.web.service.CategoriesService
import com.niki.spotify.web.service.PlayerService
import com.niki.spotify.web.service.PlaylistsService
import com.niki.spotify.web.service.SearchService
import com.niki.spotify.web.service.TracksService
import com.niki.spotify.web.service.UsersService
import okhttp3.Interceptor

class SpotifyApi {
    private var accessToken = ""

    companion object {
        private const val BASE_URL = "https://api.spotify.com/v1/"
    }

    val albumsService
            by lazy { createService<AlbumsService>(BASE_URL, createAuthInterceptor()) }
    val artistsService
            by lazy { createService<ArtistsService>(BASE_URL, createAuthInterceptor()) }
    val categoriesService
            by lazy { createService<CategoriesService>(BASE_URL, createAuthInterceptor()) }
    val playerService
            by lazy { createService<PlayerService>(BASE_URL, createAuthInterceptor()) }
    val playlistsService
            by lazy { createService<PlaylistsService>(BASE_URL, createAuthInterceptor()) }
    val searchService
            by lazy { createService<SearchService>(BASE_URL, createAuthInterceptor()) }
    val tracksService
            by lazy { createService<TracksService>(BASE_URL, createAuthInterceptor()) }
    val usersService
            by lazy { createService<UsersService>(BASE_URL, createAuthInterceptor()) }

    private var listener: (() -> Unit)? = null

    private fun setOnErrorListener(listener: (() -> Unit)?) {
        this.listener = listener
    }

    private fun setAccess(token: String) {
        accessToken = token
    }

    private fun createAuthInterceptor() = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .apply { // 拦截器的逻辑是在每次请求时都执行
                if (accessToken.isNotBlank()) {
                    addHeader("Authorization", "Bearer $accessToken")
                } else
                    listener?.invoke()
            }
            .build()
        chain.proceed(request)
    }
}