////@file:Suppress("FunctionName")
//package com.niki.app
//
//import android.graphics.Bitmap
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import com.niki.app.util.BitmapCachePool
//import com.niki.app.util.CLIENT_ID
//import com.niki.app.util.ItemCachePool
//import com.niki.app.util.LOAD_BATCH_SIZE
//import com.niki.app.util.LowBitmapCachePool
//import com.niki.app.util.REDIRECT_URI
//import com.niki.app.util.log
//import com.niki.app.util.noChildListItem
//import com.niki.app.util.withPermit
//import com.niki.spotify_objs.checkAndSet
//import com.niki.util.getPlaceholderBitmap
//import com.spotify.android.appremote.api.ConnectionParams
//import com.spotify.android.appremote.api.Connector
//import com.spotify.android.appremote.api.ContentApi
//import com.spotify.android.appremote.api.SpotifyAppRemote
//import com.spotify.protocol.client.CallResult
//import com.spotify.protocol.error.SpotifyAppRemoteException
//import com.spotify.protocol.types.Image
//import com.spotify.protocol.types.ImageUri
//import com.spotify.protocol.types.ListItem
//import com.spotify.protocol.types.ListItems
//import com.spotify.protocol.types.PlayerState
//import com.zephyr.base.appContext
//import com.zephyr.base.log.logE
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.async
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.isActive
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.lang.ref.WeakReference
//import java.util.concurrent.Semaphore
//import java.util.concurrent.TimeUnit
//
//// 播放器参数
//// {
//private var _isPaused = MutableLiveData(false) // 播放器暂停
//val isPaused: LiveData<Boolean> get() = _isPaused
//
//private var _playbackPosition = MutableLiveData(-1L) // 当前播放位置 (毫秒)
//val playbackPosition: LiveData<Long> get() = _playbackPosition
//
//private var _playbackSpeed = MutableLiveData(1F)// 播放速度
//val playbackSpeed: LiveData<Float> get() = _playbackSpeed
//
//private var _coverUrl = MutableLiveData("") // 封面 url
//val coverUrl: LiveData<String> get() = _coverUrl
//
//private var _trackName = MutableLiveData("-")// 歌名
//val trackName: LiveData<String> get() = _trackName
//
//private var _artistName = MutableLiveData("-") // 艺术家名
//val artistName: LiveData<String> get() = _artistName
//
//private var _albumName = MutableLiveData("") // 专辑名
//val albumName: LiveData<String> get() = _albumName
//
//private var _duration = MutableLiveData(-1L) // 歌曲时长
//val duration: LiveData<Long> get() = _duration
//
//private var _progress = MutableLiveData(0) // seekbar 进度
//val progress: LiveData<Int> get() = _progress
//
//private var _isLoading = MutableLiveData(false) // dev
//val isLoading: LiveData<Boolean> get() = _isLoading
//// }
//
///**
// * 同步获取数据
// */
//fun <T> CallResult<T>.get(timeout: Long? = null): T? {
//    val await = if (timeout == null || timeout <= 0)
//        await()
//    else
//        await(timeout, TimeUnit.MILLISECONDS)
//
//    if (!await.isSuccessful)
//        await.error.log(TODO())
//
//    return await.data
//}
//
///**
// * 异步获取数据
// */
//fun <T> CallResult<T>.get(callback: (T?) -> Unit) {
//    var isCalled = false
//    setResultCallback {
//        if (isCalled) return@setResultCallback
//        isCalled = true
//        callback(it)
//    }
//    setErrorCallback {
//        if (isCalled) return@setErrorCallback
//        isCalled = true
//        it.log(TODO())
//        callback(null)
//    }
//}
//
//
///**
// * 主要负责向 spotify 获取数据
// *
// * 作用类似一个 viewmodel, 但是用得非常广泛所以写成对象
// *
// * 规则: 不使用 suspend
// */
//object SpotifyRemote1 : Connector.ConnectionListener {
//    private val TAG = this::class.java.simpleName
//
//    private var isRemoteConnecting = false
//
//    var remote: SpotifyAppRemote? = null // spotify remote 实例
//        get() {
//            if (field?.isConnected != true) {
//                field = null
//            }
//            return field
//        }
//        private set
//
//    private val connectionParams: ConnectionParams by lazy {
//        ConnectionParams.Builder(CLIENT_ID)
//            .setRedirectUri(REDIRECT_URI)
//            .showAuthView(true)
//            .build()
//    }
//
//    private var _isConnected = MutableLiveData(false) // remote 可用性
//    val isConnected: LiveData<Boolean> get() = _isConnected
//
//    private val scope by lazy { CoroutineScope(Dispatchers.IO) }
//    private var keepConnectionJob: Job? = null // 定时检查 remote 可用性的 job
//
//    // 待定, 解耦与 spotify 业务无关项
//    // {
//    private val preCacheSemaphore = Semaphore(3) // 最多并发获取的内容数量上限
//    private val getChildrenSemaphore = Semaphore(3) // 最多并发获取的内容数量上限
//    private val ongoingRequests =
//        mutableMapOf<ListItem, MutableList<WeakCallback<List<ListItem>?>>>()
//    private val preCacheLock = Any()
//    // }
//
//    init {
//        startWatchConnectionJob()
//
//        isConnected.observeForever { isConnected ->
//            TODO()
//            if (isConnected)
//                subscribeToState()
//        }
//    }
//
//
//    // spotify 连接
//    // {
//    fun connectSpotify() = scope.launch(Dispatchers.Main) {
//        if (isRemoteConnecting)
//            return@launch
//        isRemoteConnecting = true
//        SpotifyAppRemote.connect(appContext, connectionParams, this@SpotifyRemote)
//    }
//
//    fun disconnectSpotify() {
//        remote?.let { SpotifyAppRemote.disconnect(it) }
//        remote = null
//    }
//
//    override fun onConnected(appRemote: SpotifyAppRemote?) {
//        remote = appRemote
//        logE(TAG, "remote 已连接")
//        isRemoteConnecting = false
//    }
//
//    override fun onFailure(throwable: Throwable?) {
//        logE(TAG, "remote 连接失败")
//        isRemoteConnecting = false
//    }
//    // }
//
//
//    /**
//     * 通过轮询监听 remote 状态
//     */
//    private fun startWatchConnectionJob() {
//        keepConnectionJob?.cancel()
//        keepConnectionJob = scope.launch(Dispatchers.IO) {
//            while (isActive) {
//                val isRemoteAvailable = (remote != null) // remote 在不可用时为 null, 所以直接将判空结果作为可用性判断
//                if (!isRemoteAvailable)
//                    connectSpotify() // 此操作需在主线程进行
//                _isConnected.checkAndSet(isRemoteAvailable)
//                TODO()
//                delay(50)
//            }
//        }
//    }
//
//    /**
//     * 更新 seekbar progress 参数
//     */
//    private fun getPlayerPosition() {
//        TODO()
//        remote?.playerApi?.playerState?.setResultCallback { state ->
//            scope.launch(Dispatchers.Main) {
//                _progress.value = if (state.track != null)
//                    (state.playbackPosition * MainActivity.SEEKBAR_MAX / state.track.duration).toInt()
//                else 0
//            }
//        }
//    }
//
//    /**
//     * TODO
//     *
//     * 同步获取 seekbar progress 值, 轮询获取
//     */
//    fun getSeekBarProgress(): Int {
//        val state = getPlayerState()
//        state.run {
//            val progress = if (this?.track != null)
//                (playbackPosition * MainActivity.SEEKBAR_MAX / track.duration).toInt()
//            else
//                0
//            return progress
//        }
//    }
//
//    fun getPlayerState(): PlayerState? {
//        return remote?.playerApi?.playerState?.await()?.data
//    }
//
//    suspend fun getPlayerState(callback: (PlayerState?) -> Unit) = withContext(Dispatchers.IO) {
//        val state = getPlayerState()
//        callback(state)
//    }
//
//
//    /**
//     * 订阅播放器参数变化
//     *
//     * TODO 解耦
//     */
//    private fun subscribeToState() {
//        remote?.playerApi?.subscribeToPlayerState()?.setEventCallback { state ->
//            scope.launch(Dispatchers.Main) {
//                _isPaused.checkAndSet(state.isPaused) // 是否暂停
//                _playbackPosition.checkAndSet(state.playbackPosition) // 播放位置-毫秒
//                _playbackSpeed.checkAndSet(state.playbackSpeed) // 播放速度
//
//                _isLoading.checkAndSet(state.isLoading())
//
//                state.track?.let {
//                    _trackName.checkAndSet(it.name)
//                    _artistName.checkAndSet(it.artist.name)
//                    _albumName.checkAndSet(it.album.name)
//                    _duration.checkAndSet(it.duration)
//                    _coverUrl.checkAndSet(it.imageUri.raw)
//                }
//            }
//        }
//    }
//
//
//    // TODO
//    fun loadSmallImage(uri: String, callback: (Bitmap) -> Unit) {
//        LowBitmapCachePool.fetch(uri)
//            ?.let(callback)
//            ?: loadImage(uri, Image.Dimension.X_SMALL) {
//                if (it.width == Image.Dimension.X_SMALL.value)
//                    LowBitmapCachePool.cache(uri, it)
//                callback(it)
//            }
//    }
//
//    // TODO
//    fun loadLargeImage(uri: String, callback: (Bitmap) -> Unit) {
//        BitmapCachePool.fetch(uri)
//            ?.let(callback)
//            ?: loadImage(uri, Image.Dimension.LARGE) {
//                if (it.width == Image.Dimension.LARGE.value)
//                    BitmapCachePool.cache(uri, it)
//                callback(it)
//            }
//    }
//
//    private fun loadImage(uri: String, size: Image.Dimension, callback: (Bitmap) -> Unit) {
//        remote?.imagesApi?.getImage(ImageUri(uri), size)
//            ?.setResultCallback(callback)
//            ?.setErrorCallback {
//                val bitmap = getPlaceholderBitmap(0)
//                callback(bitmap)
//            }
//    }
//
//
//    fun switchState() {
//        if (isPaused.value == false)
//            pause()
//        else
//            play()
//    }
//
//    fun play(item: ListItem? = null) {
//        if (item == null)
//            remote?.playerApi?.resume()
//        else if (item.hasChildren)
//            remote?.contentApi?.playContentItem(item)
//        else
//            remote?.playerApi?.play(item.uri)
//    }
//
//    private fun pause() {
//        remote?.playerApi?.pause()
//    }
//
//    fun previous() {
//        remote?.playerApi?.skipPrevious()
//    }
//
//    fun next() {
//        remote?.playerApi?.skipNext()
//    }
//
//    /**
//     * 播放 item 内的第 index 首歌曲
//     */
//    fun playItemAtIndex(item: ListItem, index: Int) {
////        if (item.uri.endsWith(":collection")) 可能会永远播放第一首, 这是 spotify 的问题
//        remote?.playerApi?.skipToIndex(item.uri, index)
//    }
//
//    fun seekTo(position: Long) {
//        remote?.playerApi?.seekTo(position)
//    }
//
//
//    /**
//     * TODO
//     *
//     * 由 adapter item 进行渲染时预加载, 可以及时加载数据
//     *
//     * 标记空的 list 为无子项
//     */
//    fun preCacheChildren(item: ListItem) {
//        scope.launch(Dispatchers.IO) {
//            preCacheSemaphore.withPermit {
//                val startTime = System.currentTimeMillis()
//                val items = async {
//                    getChildrenOfItem(item, 0, LOAD_BATCH_SIZE)
//                }.await() ?: return@withPermit // null 直接返回
//
//                val timeSpent = (System.currentTimeMillis() - startTime) / 1000.0
//
//                if (items.isEmpty()) {
//                    if (timeSpent > 5)
//                        logE(TAG, "${item.title} 用时过长 ($timeSpent s), 不标记")
//                    else {
//                        logE(TAG, "${item.title} 被标记为空, 用时 $timeSpent s")
//                        ItemCachePool.cache(item.id, mutableListOf(noChildListItem))
//                    }
//                } else {
//                    logE(TAG, "${item.title} 被缓存, 用时: $timeSpent s")
//                    ItemCachePool.cache(item.id, items.toMutableList())
//                }
//            }
//        }
//    }
//
//
//    private fun getListItems(): ListItems? {
//        val listItems = remote?.contentApi
//            ?.getRecommendedContentItems(ContentApi.ContentType.DEFAULT) // 使用不同的 type 得到的结果似乎是相同的
//            ?.get()
////            ?.setResultCallback { callback(it) }?.setErrorCallback { callback(null) } ?: callback(
////            null
////        )
//        return listItems
//    }
//
//    private fun getListItems(callback: (ListItems?) -> Unit) {
//        remote?.contentApi?.getRecommendedContentItems(ContentApi.ContentType.DEFAULT) // 使用不同的 type 得到的结果似乎是相同的
//            ?.setResultCallback { callback(it) }?.setErrorCallback { callback(null) } ?: callback(
//            null
//        )
//    }
//
//    fun getContentList(callback: (List<ListItem>) -> Unit) {
//        getListItems {
//            val list = mutableListOf<ListItem>()
//            it?.items?.forEach { item -> // 直接 toList 貌似不行
//                list.add(item)
//            }
//            callback(list.toList())
//        }
//    }
//
//    /**
//     * 挂起方法
//     */
//    fun getChildrenOfItem(
//        item: ListItem,
//        offset: Int = 0,
//        size: Int = LOAD_BATCH_SIZE,
//        callback: (List<ListItem>?) -> Unit
//    ) = scope.launch(Dispatchers.IO) {
//        synchronized(preCacheLock) {
//            // 如果当前已有请求正在处理中, 直接添加回调并返回
//            if (ongoingRequests.containsKey(item)) {
//                ongoingRequests[item]?.add(WeakCallback(callback))
//                return@launch
//            }
//
//            // 第一次请求, 初始化回调列表, 并且继续获取
//            ongoingRequests[item] = mutableListOf(WeakCallback(callback))
//        }
//
//        getChildrenSemaphore.withPermit {
//            val list = getChildrenOfItem(item, offset, size)
//            withContext(Dispatchers.Main) {
//                notifyCallbacks(item, list)
//            }
//        }
//    }
//
//    /**
//     * 获取种类中的歌单
//     *
//     * 同步方法
//     *
//     * 当返回空 list 则表示获取成功但无数据, 如果返回空则表示错误
//     */
//    private fun getChildrenOfItem(
//        item: ListItem,
//        offset: Int = 0,
//        size: Int = LOAD_BATCH_SIZE
//    ): List<ListItem>? {
//        var list: List<ListItem>? = null
//        val error = remote
//            ?.contentApi
//            ?.getChildrenOfItem(item, size, offset)
//            ?.setResultCallback { listItems ->
//                list = listItems.items.toList()
//            }
//            ?.await()
//            ?.error
//
//        error?.let {
//            if (it.isSpotifyError())
//                list = emptyList()
//            else
//                it.log(TAG)
//        }
//
//        return list
//    }
//
//    /**
//     * 通知所有回调, 并清理集合
//     */
//    private fun notifyCallbacks(item: ListItem, result: List<ListItem>?) {
//        val callbacks: List<WeakCallback<List<ListItem>?>>
//        synchronized(preCacheLock) {
//            // 获取所有待通知的回调
//            callbacks = ongoingRequests.remove(item) ?: emptyList()
//        }
//
//        logE(TAG, "一并执行 ${callbacks.size} 个回调")
//        // 执行所有回调
//        callbacks.forEach { it.invoke(result) }
//    }
//}
//
//fun PlayerState.isLoading(): Boolean {
//    return (playbackSpeed <= 0 && !isPaused && playbackPosition <= 80)
//}
//
//private val SPOTIFY_ERROR_MESSAGES = listOf(
//    "Result was not delivered on time.",
//    "Timeout running com.spotify.get_children_of_item"
//)
//
//fun Throwable.isSpotifyError(): Boolean {
//    return (message in SPOTIFY_ERROR_MESSAGES || this is SpotifyAppRemoteException)
//}

