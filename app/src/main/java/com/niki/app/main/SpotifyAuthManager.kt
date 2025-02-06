package com.niki.app.main

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.niki.spotify.remote.logS
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse

/**
 * 管理主活动向 spotify 请求连接的事务
 */
class SpotifyAuthManager(private var activity: AppCompatActivity?) {
    private var isWorking = false
    private var authCallback: ((ActivityResult) -> Unit)? = null
    private var launcher: ActivityResultLauncher<Intent>? = null

    private val scopes = arrayOf(
//        "ugc-image-upload",
        "user-read-playback-state",
        "user-modify-playback-state",
        "user-read-currently-playing",
//        "app-remote-control",
        "streaming",
        "playlist-read-private",
//        "playlist-read-collaborative",
        "playlist-modify-private",
        "playlist-modify-public",
//        "user-follow-modify",
//        "user-follow-read",
        "user-read-playback-position",
        "user-top-read",
        "user-read-recently-played",
        "user-library-modify",
        "user-library-read",
//        "user-read-email",
//        "user-read-private",
//        "user-soa-link",
//        "user-soa-unlink",
//        "soa-manage-entitlements",
//        "soa-manage-partner",
//        "soa-create-partner"
    )

    init {
        try {
            launcher = activity?.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                authCallback?.invoke(result)
                isWorking = false
            }
        } catch (t: Throwable) {
            t.logS()
            release()
        }
    }

    fun setCallback(callback: ((ActivityResult) -> Unit)?) {
        authCallback = callback
    }

    /**
     * 释放引用
     */
    fun release() {
        launcher = null
        authCallback = null
        activity = null
        isWorking = false
    }

    /**
     * 拉起 spotify 客户端进行授权
     */
    fun authenticate() {
        if (isWorking) return
        isWorking = true
        val intent = buildAuthRequest()
        launcher?.launch(intent) ?: { isWorking = false }
    }


    private fun buildAuthRequest(): Intent {
        val builder = AuthorizationRequest.Builder(
            com.niki.spotify.remote.CLIENT_ID,
            AuthorizationResponse.Type.CODE, // TOKEN
            com.niki.spotify.remote.REDIRECT_URI
        )

        val request = builder.setScopes(scopes).build()
        val intent = AuthorizationClient.createLoginActivityIntent(activity, request)

        return intent
    }
}