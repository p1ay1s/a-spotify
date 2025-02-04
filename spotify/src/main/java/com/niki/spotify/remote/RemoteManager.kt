package com.niki.spotify.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.zephyr.base.appContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 主要负责向 spotify 获取数据
 *
 * 作用类似一个 viewmodel, 但是用得非常广泛所以写成对象
 *
 * 规则: 不使用 suspend
 */
object RemoteManager : Connector.ConnectionListener {
    private var isRemoteConnecting = false

    var remote: SpotifyAppRemote? = null // spotify remote 实例
        get() {
            if (field != null) {
                if (!field!!.isConnected)
                    field = null
            }
            return field
        }
        private set

    private val connectionParams: ConnectionParams by lazy {
        ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()
    }

    private var _isConnected = MutableLiveData(false) // remote 可用性
    val isConnected: LiveData<Boolean> get() = _isConnected

    private var keepConnectionJob: Job? = null // 定时检查 remote 可用性的 job

    init {
        startWatchConnectionJob()
    }

    fun connectSpotify() {
        if (isRemoteConnecting)
            return
        isRemoteConnecting = true
        spotifyScope.launch(Dispatchers.Main) {
            SpotifyAppRemote.connect(appContext, connectionParams, this@RemoteManager)
        }
    }

    fun disconnectSpotify() {
        remote?.let { SpotifyAppRemote.disconnect(it) }
        remote = null
    }

    override fun onConnected(appRemote: SpotifyAppRemote?) {
        remote = appRemote
        logS("remote 已连接")
        isRemoteConnecting = false
    }

    override fun onFailure(throwable: Throwable?) {
        logS("remote 连接失败")
        isRemoteConnecting = false
    }

    /**
     * 通过轮询监听 remote 状态
     */
    private fun startWatchConnectionJob() {
        keepConnectionJob?.cancel()
        keepConnectionJob = spotifyScope.launch(Dispatchers.IO) {
            while (isActive) {
                (remote != null).let { boolean -> // remote 在不可用时为 null, 所以直接将判空结果作为可用性判断
                    if (!boolean)
                        connectSpotify() // 此操作需在主线程进行
                    _isConnected.checkAndSet(boolean)
                }
                delay(CONNECTION_WATCH_DELAY)
            }
        }
    }
}