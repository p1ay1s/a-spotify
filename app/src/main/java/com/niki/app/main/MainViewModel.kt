package com.niki.app.main

import androidx.lifecycle.ViewModel
import com.niki.app.net.handleRequest
import com.niki.app.net.web_api.SpotifyApi
import com.niki.spotify.remote.logS
import com.niki.spotify.web.LIMIT

class MainViewModel : ViewModel() {

    var allowAutoSetProgress = true

    var notedProgress = 0

    var lastBackPressedTimeSet = -1L

    val api: SpotifyApi by lazy { SpotifyApi() }

    fun test() {
        api.playerService.getRecentlyPlayedTracks(
            mapOf(LIMIT to "50")
        ).handleRequest(
            onSuccess = { data ->
                data?.items?.forEach {
                    it?.track?.run {
                        logS(name + " " + album?.name)
                    }
                }
            }
        )
    }
}