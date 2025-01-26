package com.niki.app

import android.app.Application
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.zephyr.base.appBaseUrl
import com.zephyr.base.appContext
import com.zephyr.base.log.Logger
import com.zephyr.base.log.VERBOSE
import com.zephyr.base.log.logE

class App : Application() {

    companion object {
        private var isConnecting = false

        private val connectionParams: ConnectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        var spotifyAppRemote: SpotifyAppRemote? = null
            private set

        var onConnectedCallback: ((SpotifyAppRemote?) -> Unit)? = null
            private set
        var onFailureCallback: ((Throwable?) -> Unit)? = null
            private set

        fun setOnConnectedCallback(callback: (SpotifyAppRemote?) -> Unit) {
            onConnectedCallback = callback
        }

        fun setOnFailureCallback(callback: (Throwable?) -> Unit) {
            onFailureCallback = callback
        }
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        connectSpotify()
        appBaseUrl = "https://accounts.spotify.com/"

        // appBaseUrl = "https://api.spotify.com/v1/"
        Logger.setLogLevel(VERBOSE)
    }

    fun connectSpotify() {
        if (isConnecting)
            return
        isConnecting = true
        SpotifyAppRemote.connect(
            appContext,
            connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(appRemote: SpotifyAppRemote?) {
                    onConnectedCallback?.invoke(appRemote)
                    spotifyAppRemote = appRemote
                    logE("APP", "remote 已连接")
                    isConnecting = false
                }

                override fun onFailure(throwable: Throwable?) {
                    onFailureCallback?.invoke(throwable)
                    logE("APP", "remote 连接失败")
                    isConnecting = false
                }
            })
    }

    fun disconnectSpotify() {
        spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback(null)
        spotifyAppRemote?.let { SpotifyAppRemote.disconnect(it) }
        spotifyAppRemote = null
    }
}