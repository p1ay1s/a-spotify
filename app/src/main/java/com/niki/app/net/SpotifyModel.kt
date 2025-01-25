package com.niki.app.net

import com.zephyr.util.ServiceBuilder
import com.zephyr.util.ServiceBuilder.requestEnqueue

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

        requestEnqueue(
            spotifyService.getLyrics("Bearer $token", trackId),
            onSuccess,
            onError
        )
    }
}