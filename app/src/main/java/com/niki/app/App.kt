package com.niki.app

import android.app.Application
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import com.zephyr.base.appBaseUrl
import com.zephyr.base.appContext
import com.zephyr.base.log.Logger
import com.zephyr.base.log.VERBOSE

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = this
        SpotifyRemote.connectSpotify()

        appBaseUrl = "https://accounts.spotify.com/"
        // appBaseUrl = "https://api.spotify.com/v1/"

        Logger.setLogLevel(VERBOSE)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }
}