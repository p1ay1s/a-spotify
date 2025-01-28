package com.niki.app

import android.graphics.Bitmap
import com.niki.app.util.BitmapCachePool
import com.niki.app.util.ItemCachePool
import com.niki.app.util.LOAD_BATCH_SIZE
import com.niki.app.util.LowBitmapCachePool
import com.niki.app.util.noChildListItem
import com.niki.spotify_objs.ContentApi
import com.niki.spotify_objs.ImageApi
import com.niki.spotify_objs.PlayerApi
import com.niki.spotify_objs.logS
import com.spotify.protocol.types.Image
import com.spotify.protocol.types.ListItem
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.lang.ref.WeakReference


/**
 * seekbar progress
 */
fun getSeekBarProgress(): Int {
    val state = PlayerApi.getPlayerState()
    state.run {
        val progress = if (this?.track != null)
            (playbackPosition * MainActivity.SEEKBAR_MAX / track.duration).toInt()
        else
            0
        return progress
    }
}

/**
 * 尝试从缓存池获取 bitmap, 获取失败再请求, 最后缓存
 */
fun loadSmallImage(uri: String, callback: (Bitmap) -> Unit) {
    LowBitmapCachePool.fetch(uri)
        ?.let(callback)
        ?: ImageApi.loadImage(uri, Image.Dimension.X_SMALL) {
            callback(it)
            if (it.width == Image.Dimension.X_SMALL.value)
                LowBitmapCachePool.cache(uri, it)
        }
}

/**
 * 尝试从缓存池获取 bitmap, 获取失败再请求, 最后缓存
 */
fun loadLargeImage(uri: String, callback: (Bitmap) -> Unit) {
    BitmapCachePool.fetch(uri)
        ?.let(callback)
        ?: ImageApi.loadImage(uri, Image.Dimension.LARGE) {
            callback(it)
            if (it.width == Image.Dimension.LARGE.value)
                BitmapCachePool.cache(uri, it)
        }
}


private val preCacheSemaphore = Semaphore(3) // 最多并发获取的内容数量上限
private val getChildrenSemaphore = Semaphore(3) // 最多并发获取的内容数量上限
private val preCacheLock = Any()


object ChildrenContentManager {
    enum class WorkState(value: Int) {
        NON(-1),
        WAITING(0),
        FETCHING(1),
        CACHING(2),
        NOTIFYING(3),
        DONE(4)
    }

    private val preCacheSemaphore = Semaphore(3) // 最多并发获取的内容数量上限

    private val lock = Any()

    //    private var jobs: MutableList<Job> = mutableListOf()
    private val uiWaitingRequests =
        mutableMapOf<ListItem, MutableList<WeakCallback<List<ListItem>?>>>()

    private val stateMap = hashMapOf<ListItem, WorkState>()
    private val jobMap = hashMapOf<ListItem, Job>()

    private fun getJob(item: ListItem): Job? {
        return jobMap[item]
    }

    private fun getState(item: ListItem): WorkState {
        return stateMap[item] ?: WorkState.NON
    }

    /**
     * 预缓存
     *
     * 若获取到空的 list(size == 0) 则标记其为无子项
     */
    fun preCacheChildren(item: ListItem) = GlobalScope.launch(Dispatchers.IO) {
        if (getJob(item) != null) return@launch

        val job = getPreCacheJob(item)

        jobMap[item] = job // 能到这必然是空的
        job.start()
    }

    /**
     * 提供给 ui 使用
     */
    fun getChildrenOfItem(
        item: ListItem,
        offset: Int,
        size: Int,
        callback: (List<ListItem>?) -> Unit
    ) {
//        logS("高优先级获取, 标记使用频次")

        ItemCachePool.fetch(item.id)?.let { cached ->
            callback(cached)
            return // 完成任务, 直接返回
        }

        ContentApi.getChildrenOfItem(item, offset, size, callback)

//        addCallback(item, callback)
//
//        val existingJob = getJob(item)
//        val state = getState(item)
//
//        when {
//            // 说明已经在执行, 此时可以让 ui 请求等待
//            existingJob != null && state in WorkState.FETCHING..WorkState.CACHING -> return
//
//            // 错过通知
//            state == WorkState.NOTIFYING -> ItemCachePool.fetch(item.id)?.let { cached ->
//                callback(cached)
//                return
//            }
//
//            // 取消 job, 然后主动获取 list, 最后缓存
//            else -> GlobalScope.launch(Dispatchers.IO) {
//                existingJob?.cancel()
//
//                val uiJob = getUIJob(item, offset, size)
//                jobMap[item] = uiJob
//                stateMap[item] = false
//                uiJob.start()
//            }
//        }
    }

    /**
     * 获取一个预加载任务, 但不启动
     */
    private suspend fun getPreCacheJob(item: ListItem): Job = coroutineScope {
        val job = launch(start = CoroutineStart.LAZY) {
//            stateMap[item] = true
//            preCacheSemaphore.withPermit {
//                ensureActive()
//                if (!isActive) return@withPermit
//
//                stateMap[item] = false
//
//                performAllWorks(item, 0, LOAD_BATCH_SIZE)
//            }
        }
        job.invokeOnCompletion { jobMap.remove(item) } // 被 cancel 是否会调用??? TODO
        job
    }

    /**
     * 获取一个 ui 任务, 不启动
     */
    private suspend fun getUIJob(item: ListItem, offset: Int, size: Int): Job = coroutineScope {
        val job = launch(start = CoroutineStart.LAZY) {
//            stateMap[item] = false

            ensureActive()
            if (!isActive) return@launch

            performAllWorks(item, offset, size)
        }
        job.invokeOnCompletion { jobMap.remove(item) }
        job
    }

    /**
     * 获取 list, 并进行缓存和回调通知
     */
    private fun performAllWorks(item: ListItem, offset: Int, size: Int) {
        val startTime = System.currentTimeMillis()
        val items = ContentApi.getChildrenOfItem(item, offset, size)
            ?: return // null 直接返回
        val endTime = System.currentTimeMillis()
        val time = (endTime - startTime) / 1000.0

        cacheItems(item, items, time)
        notifyCallbacks(item, items)
    }

    /**
     * 处理缓存事件
     */
    private fun cacheItems(item: ListItem, items: List<ListItem>?, time: Double) {
        if (items == null)
            return
        if (items.isEmpty()) {
            if (time > 5) // 防止标记错误
                logS("${item.title} 用时过长 ($time s), 不标记")
            else {
                logS("${item.title} 被标记为空, 用时 $time s")
                ItemCachePool.cache(item.id, mutableListOf(noChildListItem))
            }
        } else {
            logS("${item.title} 被缓存, 用时: $time s")
            ItemCachePool.cache(item.id, items.toMutableList())
        }
    }

    /**
     * 执行所有回调, 并清理集合
     */
    private fun notifyCallbacks(item: ListItem, result: List<ListItem>?) {
        val callbacks: List<WeakCallback<List<ListItem>?>>
        synchronized(lock) {
            callbacks = uiWaitingRequests.remove(item) ?: emptyList() // 获取所有待通知的回调

            logS("一并执行 ${callbacks.size} 个回调")
            callbacks.forEach { it.invoke(result) }
        }
    }

    private fun addCallback(item: ListItem, callback: (List<ListItem>?) -> Unit) {
        val list = uiWaitingRequests[item]
        if (list != null)
            list.add(WeakCallback(callback))
        else
            uiWaitingRequests[item] = mutableListOf(WeakCallback(callback))
    }

    class WeakCallback<T>(callback: (T?) -> Unit) {
        private val reference = WeakReference(callback)

        fun invoke(result: T?) {
            reference.get()?.invoke(result)
        }
    }

    data class JI(
        var job: Job?,
        var isWaiting: Boolean
    )
}