package com.niki.app.util

import com.niki.app.util.cache_pool.ListItemCachePool
import com.niki.spotify_objs.ContentApi
import com.niki.spotify_objs.ListItemResult
import com.niki.spotify_objs.logS
import com.spotify.protocol.types.ListItem
import kotlinx.coroutines.Job

//interface Model {
//    fun preCacheChildren(item: ListItem)
//
//    fun getChildrenOfItem(
//        item: ListItem,
//        offset: Int,
//        size: Int,
//        callback: (result: ListItemResult) -> Unit
//    )
//
//    fun onCache(item: ListItem, result: ListItemResult)
//
//    fun onCallback(item: ListItem, result: ListItemResult)
//}

object SpotifyChildrenLoader {
    private val lock = Any()

    private val waitingJobs = hashMapOf<ListItem, Job>()
    private val uiJobs = hashMapOf<ListItem, MutableList<(ListItemResult) -> Unit>>()


    private fun onCache(item: ListItem, result: ListItemResult) {
        logS("[${item.title}] 缓存为 ${result::class.java.simpleName}")
        ListItemCachePool.cacheR(item.id, result) // 直接交给缓存池处理
    }

    private fun onCallback(item: ListItem, result: ListItemResult) = synchronized(lock) {
        val callbacks = uiJobs.remove(item) ?: emptyList() // 获取所有待通知的回调

        logS("[${item.title}] 回调x${callbacks.size}")
        runOnMain {
            callbacks.forEach { it.invoke(result) }
        }
    }

    /**
     * 对 item 预缓存前 20 个
     */
    fun preCacheChildren(item: ListItem) {
        val job = ContentApi.getWaitJob(item, 0) {
            onCache(item, it)
            onCallback(item, it)
        }

        job.invokeOnCompletion {
            synchronized(lock) {
                it?.logS()
                logS("[${item.title}] cache job 完成")
                waitingJobs.remove(item)
            }
        }

        synchronized(lock) {
            waitingJobs[item] = job
            job.start()
        }
    }

    /**
     * 为 ui 获取数据
     */
    fun getChildrenOfItem(
        item: ListItem,
        offset: Int,
        callback: (result: ListItemResult) -> Unit
    ) {
        synchronized(lock) {
            val callbacks = uiJobs.getOrPut(item) { mutableListOf() }
            callbacks.add(callback)

            ListItemCachePool.fetchR(item.id, offset).let { result ->
                when (result) {
                    ListItemResult.Error -> return@let

                    is ListItemResult.HasChildren -> {
                        logS("[${item.title} $offset] 获取缓存x${result.list.size}")
                    }

                    ListItemResult.NoChildren -> {
                        logS("[${item.title} $offset] 获取缓存为 noChildren")
                    }
                }
                onCallback(item, result)
                return // 完成任务, 直接返回
            }

            val waitingJob = waitingJobs[item]
            waitingJob?.cancel()
            val job = ContentApi.getUIJob(item, offset) { result ->
                onCache(item, result)
                onCallback(item, result)
            }

            job.invokeOnCompletion {
                synchronized(lock) {
                    it?.logS()
                    logS(item.title + " ui job 完成")
                }
            }
            job.start()
        }
    }
}

///**
// * 软引用回调封装, 只会回调一次 (在主线程)
// */
//private class WeakCallback<T>(callback: (T) -> Unit) {
//    private var reference: WeakReference<(T) -> Unit>? = WeakReference(callback)
//
//    fun invoke(result: T) = runOnMain {
//        reference?.get()?.invoke(result)
//        reference = null
//    }
//}