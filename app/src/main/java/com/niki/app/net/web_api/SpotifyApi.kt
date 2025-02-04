package com.niki.app.net.web_api

import com.niki.app.net.createService
import com.niki.app.util.appAccess
import com.niki.app.net.web_api.services.AlbumsService
import com.niki.app.net.web_api.services.ArtistsService
import com.niki.app.net.web_api.services.AudioFeaturesService
import com.niki.app.net.web_api.services.BrowseService
import com.niki.app.net.web_api.services.FollowService
import com.niki.app.net.web_api.services.LibraryService
import com.niki.app.net.web_api.services.PlaylistsService
import com.niki.app.net.web_api.services.ProfilesService
import com.niki.app.net.web_api.services.SearchService
import com.niki.app.net.web_api.services.TracksService
import com.niki.app.net.web_api.services.UserService
import okhttp3.Interceptor

class SpotifyApi {
    companion object {
        private const val BASE_URL = "https://api.spotify.com/v1/"
    }

//    private val clazz = kaaes.spotify.webapi.android.SpotifyService::class.java

    //    val service by lazy { createService<SService>(BASE_URL, createAuthInterceptor()) }
    val profilesService by lazy {
        createService<ProfilesService>(
            BASE_URL,
            createAuthInterceptor()
        )
    }
    val playlistsService by lazy {
        createService<PlaylistsService>(
            BASE_URL,
            createAuthInterceptor()
        )
    }
    val albumsService
            by lazy { createService<AlbumsService>(BASE_URL, createAuthInterceptor()) }
    val artistsService
            by lazy { createService<ArtistsService>(BASE_URL, createAuthInterceptor()) }
    val tracksService
            by lazy { createService<TracksService>(BASE_URL, createAuthInterceptor()) }
    val browseService
            by lazy { createService<BrowseService>(BASE_URL, createAuthInterceptor()) }
    val libraryService
            by lazy { createService<LibraryService>(BASE_URL, createAuthInterceptor()) }
    val followService
            by lazy { createService<FollowService>(BASE_URL, createAuthInterceptor()) }
    val searchService
            by lazy { createService<SearchService>(BASE_URL, createAuthInterceptor()) }
    val audioFeaturesService
            by lazy { createService<AudioFeaturesService>(BASE_URL, createAuthInterceptor()) }
//    val recommendationsService
//            by lazy { createService<RecommendationsService>(BASE_URL, createAuthInterceptor()) }
    val userService
            by lazy { createService<UserService>(BASE_URL, createAuthInterceptor()) }
    private var listener: (() -> Unit)? = null


    private fun setOnErrorListener(listener: (() -> Unit)?) {
        this.listener = listener
    }

    private fun createAuthInterceptor() = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .apply { // 拦截器的逻辑是在每次请求时都执行
                if (appAccess.isNotBlank()){
                    addHeader("Authorization", "Bearer $appAccess")
                }
                else
                    listener?.invoke()
            }
            .build()
        chain.proceed(request)
    }
}