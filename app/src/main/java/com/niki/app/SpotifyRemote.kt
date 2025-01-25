package com.niki.app

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    val remote: SpotifyAppRemote?
        get() {
            val r = App.spotifyAppRemote
            if (r == null || !r.isConnected)
                App.onFailureCallback?.invoke(null)
            return r
        }

    val isAvailable: Boolean
        get() = remote?.isConnected ?: false


    private val TAG = this::class.java.simpleName
    val scope = CoroutineScope(Dispatchers.IO)

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


    private var refreshInfosJob: Job? = null
    private var awakeConnectionJob: Job? = null

    fun startRefreshInfosJob() {
        refreshInfosJob?.cancel()
        refreshInfosJob = scope.launch(Dispatchers.IO) {
            while (true) {
                _connected.checkAndSet(remote != null)
                refreshInfos()
                delay(15)
            }
        }
    }

    fun startAwakeConnectionJob() {
        awakeConnectionJob?.cancel()
        awakeConnectionJob = scope.launch(Dispatchers.IO) {
            while (true) {
                withContext(Dispatchers.Main) {
                    if (!isAvailable)
                        (appContext as? App)?.connectSpotify() // 此操作需在主线程进行
                }
                delay(1000)
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

    private fun refreshInfos() {
        remote?.playerApi?.playerState?.setResultCallback { state ->
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

    fun play(uri: String? = null) {
        if (uri.isNullOrEmpty()) {
            remote?.playerApi?.resume()
        } else {
            remote?.playerApi?.play(uri)
        }
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

    fun getListItems(callback: (ListItems) -> Unit) {
        remote?.contentApi?.getRecommendedContentItems("")
            ?.setResultCallback { callback(it) }
    }

    fun getListItemList(callback: (List<ListItem>) -> Unit) {
        getListItems {
            val list = mutableListOf<ListItem>()
            it.items.forEach { item ->
                list.add(item)
            }
            callback(list)
        }
    }

    /**
     * 获取 List 对象, 提供给 adapter
     */
    fun getItemListNonNull(callback: (List<Item>) -> Unit) {
        getListItemList { filterListItem(it) { items -> callback(items) } }
    }

    // 播放歌单
    fun playPlaylist(item: ListItem) {
        remote?.contentApi?.playContentItem(item)
    }

    fun playPlaylistWithIndex(item: ListItem, index: Int) {
        if (item.uri.endsWith(":collection"))
            "可能会永远播放第一首, 这是 spotify 的问题".toast()
        remote?.playerApi?.skipToIndex(item.uri, index)
    }

    fun seekTo(position: Long) {
        remote?.playerApi?.seekTo(position)
    }

    fun getChildOfItem(item: ListItem, page: Int, size: Int, callback: (List<ListItem>) -> Unit) {
        scope.launch { callback(getChildOfItem(item, page, size)) }
    }

    // 获取种类中的歌单
    private suspend fun getChildOfItem(
        item: ListItem,
        page: Int = 0,
        size: Int = LOAD_COUNTS_PER_TIME
    ): List<ListItem> = coroutineScope {
        var list = listOf<ListItem>()
        async {
            remote?.contentApi?.getChildrenOfItem(item, size, page * size)
                ?.setResultCallback { listItems ->
                    list = listItems.items.toList()
                }?.await() // 这是关键,,,
        }.await()
        logE(TAG, "${item.title} hasChildren: ${item.hasChildren} size: ${list.size}")
        list
    }

    private fun filterListItem(list: List<ListItem>, callback: (List<Item>) -> Unit) {
        scope.launch(Dispatchers.IO) {
            val resultList = mutableListOf<Item>()

            async {
                list.forEach { listItem ->
                    val item = Item(listItem)
                    val children = getChildOfItem(listItem)
                    if (children.isNotEmpty()) {
                        item.children = children
                        resultList.add(item)
                    }
                }
            }.await()

            withContext(Dispatchers.Main) {
                logE(TAG, "parent size: " + resultList.size.toString())
                callback(resultList)
            }
        }
    }
}

class Item(val listItem: ListItem) {
    var children: List<ListItem> = emptyList()
}