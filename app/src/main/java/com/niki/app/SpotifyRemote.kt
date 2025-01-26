//@file:Suppress("FunctionName")
package com.niki.app

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.spotify.android.appremote.api.ContentApi
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Image
import com.spotify.protocol.types.ImageUri
import com.spotify.protocol.types.ListItem
import com.spotify.protocol.types.ListItems
import com.spotify.protocol.types.PlayerState
import com.zephyr.base.appContext
import com.zephyr.base.extension.toast
import com.zephyr.base.log.logE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 主要负责向 spotify 获取数据
 */
object SpotifyRemote {
    private val remote: SpotifyAppRemote?
        get() {
            val r = App.spotifyAppRemote
            if (r == null || !r.isConnected)
                App.onFailureCallback?.invoke(null)
            return r
        }

    private val isAvailable: Boolean
        get() = remote != null

    private val TAG = this::class.java.simpleName
    private val scope = CoroutineScope(Dispatchers.IO)

    private var _isPaused = MutableLiveData(false) // 正在播放
    val isPaused: LiveData<Boolean>
        get() = _isPaused

    private var _playbackPosition = MutableLiveData(-1L) // 播放位置 (毫秒)
    val playbackPosition: LiveData<Long>
        get() = _playbackPosition

    private var _playbackSpeed = MutableLiveData(1F)// 播放速度
    val playbackSpeed: LiveData<Float>
        get() = _playbackSpeed

    private var _coverUrl = MutableLiveData("") // 封面 url
    val coverUrl: LiveData<String>
        get() = _coverUrl

    private var _trackName = MutableLiveData("-")// 歌名
    val trackName: LiveData<String>
        get() = _trackName

    private var _artistName = MutableLiveData("-") // 艺术家名
    val artistName: LiveData<String>
        get() = _artistName

    private var _albumName = MutableLiveData("") // 专辑名
    val albumName: LiveData<String>
        get() = _albumName

    private var _duration = MutableLiveData(-1L) // 歌曲时长
    val duration: LiveData<Long>
        get() = _duration

    private var _progress = MutableLiveData(0) // 歌曲时长
    val progress: LiveData<Int>
        get() = _progress

    private var _isLoading = MutableLiveData(false) // dev
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private var _connected = MutableLiveData(false) // dev
    val connected: LiveData<Boolean>
        get() = _connected


    private var awakeConnectionJob: Job? = null

    init {
        startAwakeConnectionJob()

        connected.observeForever { isConnected ->
            if (isConnected)
                subscribeToState()
        }
    }

    private fun startAwakeConnectionJob() {
        awakeConnectionJob?.cancel()
        awakeConnectionJob = scope.launch(Dispatchers.Main) {
            while (true) {
                val a = isAvailable
                if (a)
                    (appContext as? App)?.connectSpotify() // 此操作需在主线程进行
                _connected.checkAndSet(a)
                delay(300)
            }
        }
    }

    fun loadLowImage(uri: String, callback: (Bitmap) -> Unit) {
        val fetchedBitmap = LowBitmapCachePool.fetch(uri)
        if (fetchedBitmap != null) {
            logE(TAG, "$uri: 获取了缓存图片")
            callback(fetchedBitmap)
            return
        }
        remote?.imagesApi?.getImage(ImageUri(uri), Image.Dimension.X_SMALL)
            ?.setResultCallback {
                LowBitmapCachePool.cache(uri, it)
                callback(it)
            }
    }

    fun loadImage(uri: String, callback: (Bitmap) -> Unit) {
        val fetchedBitmap = BitmapCachePool.fetch(uri)
        if (fetchedBitmap != null) {
            logE(TAG, "$uri: 获取了缓存图片")
            callback(fetchedBitmap)
            return
        }
        remote?.imagesApi?.getImage(ImageUri(uri), Image.Dimension.LARGE)
            ?.setResultCallback {
                BitmapCachePool.cache(uri, it)
                callback(it)
            }
    }

    fun subscribeToState() {
        remote?.playerApi?.subscribeToPlayerState()?.setEventCallback { state ->
            scope.launch(Dispatchers.Main) {
                _isPaused.checkAndSet(state.isPaused) // 是否暂停
                _playbackPosition.checkAndSet(state.playbackPosition) // 播放位置（毫秒）
                _playbackSpeed.checkAndSet(state.playbackSpeed) // 播放速度

                _isLoading.checkAndSet(state.isLoading())

                state.track?.let {
                    _trackName.checkAndSet(it.name)
                    _artistName.checkAndSet(it.artist.name)
                    _albumName.checkAndSet(it.album.name)
                    _duration.checkAndSet(it.duration)
                    _coverUrl.checkAndSet(it.imageUri.raw)

                    _progress.checkAndSet((playbackPosition.value!! * MainActivity.SEEKBAR_MAX / duration.value!!).toInt())
                }
            }
        }
    }

    private fun PlayerState.isLoading(): Boolean {
        return (playbackSpeed <= 0 && !isPaused && playbackPosition <= 80)
    }

    fun switchState() {
        if (isPaused.value == false) {
            pause()
        } else {
            play()
        }
    }

    fun play(item: ListItem? = null) {
        if (item == null)
            remote?.playerApi?.resume()
        else if (item.hasChildren)
            remote?.contentApi?.playContentItem(item)
        else if (!item.uri.isNullOrEmpty())
            remote?.playerApi?.play(item.uri)
    }

    fun pause() {
        remote?.playerApi?.pause()
    }

    fun previous() {
        remote?.playerApi?.skipPrevious()
    }

    fun next() {
        remote?.playerApi?.skipNext()
    }

    private suspend fun <T> MutableLiveData<T?>.checkAndSet(value: T?) =
        withContext(Dispatchers.Main) {
            if (value != this@checkAndSet.value)
                this@checkAndSet.value = value
        }

    private fun getContents(callback: (ListItems) -> Unit) {
        remote?.contentApi?.getRecommendedContentItems(ContentApi.ContentType.DEFAULT) // 使用不同的 type 得到的结果似乎是相同的
            ?.setResultCallback { callback(it) }
    }

    fun getContentList(callback: (List<ListItem>) -> Unit) {
        getContents {
            val list = mutableListOf<ListItem>()
            it.items.forEach { item -> // 直接 toList 貌似不行
                list.add(item)
            }
            callback(list)
        }
    }

    fun playPlaylistWithIndex(item: ListItem, index: Int) {
        if (item.uri.endsWith(":collection")) {
            scope.launch(Dispatchers.Main) {
                "可能会永远播放第一首, 这是 spotify 的问题".toast()
            }
        }
        remote?.playerApi?.skipToIndex(item.uri, index)
    }

    fun seekTo(position: Long) {
        remote?.playerApi?.seekTo(position)
    }

    /**
     * 由 adapter item 进行渲染时预加载, 可以及时加载数据
     */
    fun preCacheChildren(item: ListItem) {
        getChildOfItem(item, 0, LOAD_BATCH_SIZE) { items ->
            if (items.isNotEmpty()) {
                logE(TAG, "pre cached ${item.id}")
                ItemCachePool.cache(item.id, items.toMutableList())
            } else {
                logE(TAG, "pre signaled ${item.id}")
                ItemCachePool.cache(item.id, mutableListOf(getNoChildListItem))
            }
        }
    }

    fun getChildOfItem(item: ListItem, offset: Int, size: Int, callback: (List<ListItem>) -> Unit) {
        scope.launch { callback(getChildOfItem(item, offset, size)) }
    }

    // 获取种类中的歌单
    private suspend fun getChildOfItem(
        item: ListItem,
        offset: Int = 0,
        size: Int = LOAD_BATCH_SIZE
    ): List<ListItem> = coroutineScope {
        var list = listOf<ListItem>()
        async {
            remote?.contentApi?.getChildrenOfItem(item, size, offset)
                ?.setResultCallback { listItems ->
                    list = listItems.items.toList()
                }?.await() // 这是关键,,,
        }.await()
        logE(TAG, "${item.title} hasChildren: ${item.hasChildren} size: ${list.size}")
        list
    }
}

//        scope.launch(Dispatchers.IO) {
//            async {
//                耗时操作
//            }.await()