package com.niki.app.main

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.niki.spotify_objs.CLIENT_ID
import com.niki.spotify_objs.REDIRECT_URI
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse

class SpotifyAuthManager( var activity: AppCompatActivity?) {
    private var isWorking = false
    private var authCallback: ((ActivityResult) -> Unit)? = null

    fun setCallback(callback: ((ActivityResult) -> Unit)?) {
        authCallback = callback
    }

    /**
     * 释放引用
     */
    fun release() {
        activity = null
        authCallback = null
        isWorking = false
    }

    /**
     * 拉起 spotify 客户端进行授权
     */
    fun authenticate() {
        if (isWorking) return
        isWorking = true
        val intent = buildAuthRequest()
        getLauncher()?.launch(intent) ?: { isWorking = false }
    }

    private fun getLauncher(): ActivityResultLauncher<Intent>? {
        return activity?.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            authCallback?.invoke(result)
            isWorking = false
        }
    }

    private fun buildAuthRequest(): Intent {
        val builder = AuthorizationRequest.Builder(
            CLIENT_ID,
            AuthorizationResponse.Type.CODE, // TOKEN
            REDIRECT_URI
        )

        builder.setScopes(
            arrayOf(
                "user-read-playback-state",
                "user-modify-playback-state",
                "user-read-currently-playing",
                "streaming"
            )
        )

        val request = builder.build()
        val intent = AuthorizationClient.createLoginActivityIntent(activity, request)

        return intent
    }
}