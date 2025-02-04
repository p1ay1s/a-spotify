package com.niki.spotify.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.niki.spotify.remote.RemoteManager.remote
import com.spotify.protocol.types.ListItem
import com.spotify.protocol.types.PlayerState
import kotlin.math.roundToInt

object PlayerApi {
    private var _isPaused = MutableLiveData(false) // 播放器暂停
    val isPaused: LiveData<Boolean> get() = _isPaused

    private var _playbackPosition = MutableLiveData(-1L) // 当前播放位置 (毫秒)
    val playbackPosition: LiveData<Long> get() = _playbackPosition

    private var _playbackSpeed = MutableLiveData(1F)// 播放速度
    val playbackSpeed: LiveData<Float> get() = _playbackSpeed

    private var _coverUrl = MutableLiveData("") // 封面 url
    val coverUrl: LiveData<String> get() = _coverUrl

    private var _trackName = MutableLiveData("-")// 歌名
    val trackName: LiveData<String> get() = _trackName

    private var _artistName = MutableLiveData("-") // 艺术家名
    val artistName: LiveData<String> get() = _artistName

    private var _albumName = MutableLiveData("") // 专辑名
    val albumName: LiveData<String> get() = _albumName

    private var _duration = MutableLiveData(0L) // 歌曲时长
    val duration: LiveData<Long> get() = _duration

    private var _isLoading = MutableLiveData(false) // dev
    val isLoading: LiveData<Boolean> get() = _isLoading

    /**
     * 同步获取 progress 值 (0 - 100), 轮询获取
     */
    fun getProgress(): Int {
        doNotRunThisOnMain()
        val state = getPlayerState()
        state.run {
            val progress = if (this?.track != null)
                (playbackPosition.toDouble() / track.duration).roundToInt()
            else
                0
            return progress
        }
    }

    fun getPlayerState(callback: (PlayerState?) -> Unit) {
        remote?.playerApi?.playerState?.get(callback)
    }

    fun getPlayerState(): PlayerState? {
        doNotRunThisOnMain()
        return remote?.playerApi?.playerState?.get()
    }

    private fun setOnPlayerStateChangeCallback(callback: ((PlayerState?) -> Unit)?) {
        remote?.playerApi?.subscribeToPlayerState()?.setEventCallback(callback)
    }

    fun startListen() {
        setOnPlayerStateChangeCallback { state ->
            if (state == null)
                return@setOnPlayerStateChangeCallback

            _isPaused.checkAndSet(state.isPaused) // 是否暂停
            _playbackPosition.checkAndSet(state.playbackPosition) // 播放位置-毫秒
            _playbackSpeed.checkAndSet(state.playbackSpeed) // 播放速度

            _isLoading.checkAndSet(state.isLoading())

            state.track?.let {
                _trackName.checkAndSet(it.name)
                _artistName.checkAndSet(it.artist.name)
                _albumName.checkAndSet(it.album.name)
                _duration.checkAndSet(it.duration)
                _coverUrl.checkAndSet(it.imageUri.raw)
            }
        }
    }

    fun endListen() {
        setOnPlayerStateChangeCallback(null)
    }

    /**
     * 根据播放情况选择播放和暂停
     */
    fun switchState() {
        if (isPaused.value == false)
            pause()
        else
            play()
    }

    /**
     * 根据传入参数的不同调用不同的播放 api
     */
    fun play(item: ListItem? = null) {
        if (item == null)
            remote?.playerApi?.resume()
        else if (item.hasChildren)
            remote?.contentApi?.playContentItem(item)
        else
            remote?.playerApi?.play(item.uri)
    }

    fun pause() {
        remote?.playerApi?.pause()
    }

    /**
     * 上一曲
     */
    fun previous() {
        remote?.playerApi?.skipPrevious()
    }

    /**
     * 下一曲
     */
    fun next() {
        remote?.playerApi?.skipNext()
    }

    /**
     * 播放 item 内的第 index 首歌曲
     */
    fun playItemAtIndex(item: ListItem, index: Int) {
//        if (item.uri.endsWith(":collection")) 可能会永远播放第一首, 这是 spotify 的问题
        remote?.playerApi?.skipToIndex(item.uri, index)
    }

    fun seekTo(position: Long) {
        remote?.playerApi?.seekTo(position)
    }
}