package com.niki.app.net

import com.niki.app.net.new.SpotifyApi
import com.niki.spotify_objs.logS
import com.zephyr.util.net.ServiceBuilder
import com.zephyr.util.net.handleResult
import com.zephyr.util.net.requestEnqueue


class SpotifyModel {
    val spotifyService: SpotifyService by lazy { ServiceBuilder.create<SpotifyService>() }

    var accessToken: String? = null
        private set

    fun setAccessToken(token: String) {
        accessToken = token
    }

    inline fun getLyrics(
        trackId: String,
        crossinline onSuccess: (LyricsResponse?) -> Unit,
        crossinline onError: (Int?, String) -> Unit
    ) {
        val token = accessToken ?: run {
            onError(401, "Access token not set")
            return
        }

        spotifyService.getLyrics("Bearer $token", trackId)
            .requestEnqueue {
                it.handleResult(onSuccess, onError)
            }
    }

    fun a(token: String) {
        SpotifyApi.run {
            accessToken = token
            logS("token is: $token")
            service.getMe()
                .requestEnqueue {
//                    it.handleResult(
//                        onSuccess = {},
//                        onError = { _, _ -> }
//                    )
                }
            service.getMyPlaylists()
                .requestEnqueue { }
        }
    }

//    fun a(token: String) {
//        val api = SpotifyApi()
//
//
//        api.setAccessToken(token)
//
//        val spotify: kaaes.spotify.webapi.android.SpotifyService = api.service
//
//        spotify.getAlbum("2dIGnmEIy1WZIcZCFSj6i8", object : Callback<Album?> {
//            override fun success(t: Album?, response: Response?) {
//                t
//            }
//
//            override fun failure(error: RetrofitError?) {
//                error
//            }
//        })
//    }
}