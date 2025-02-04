package com.niki.app.util

import com.niki.app.util.cache_pool.ListItemCachePool
import com.niki.spotify.remote.ContentApi
import com.niki.spotify.remote.ListItemResult
import com.niki.spotify.remote.logS
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
    private val uiJobs = hashMapOf<ListItem, MutableList<(com.niki.spotify.remote.ListItemResult) -> Unit>>()


    private fun onCache(item: ListItem, result: com.niki.spotify.remote.ListItemResult) {
        com.niki.spotify.remote.logS("[${item.title}] 缓存为 ${result::class.java.simpleName}")
        ListItemCachePool.cacheR(item.id, result) // 直接交给缓存池处理
    }

    private fun onCallback(item: ListItem, result: com.niki.spotify.remote.ListItemResult) = synchronized(lock) {
        val callbacks = uiJobs.remove(item) ?: emptyList() // 获取所有待通知的回调

        com.niki.spotify.remote.logS("[${item.title}] 回调x${callbacks.size}")
        runOnMain {
            callbacks.forEach { it.invoke(result) }
        }
    }

    /**
     * 对 item 预缓存前 20 个
     */
    fun preCacheChildren(item: ListItem) {
        val job = com.niki.spotify.remote.ContentApi.getWaitJob(item, 0) {
            onCache(item, it)
            onCallback(item, it)
        }

        job.invokeOnCompletion {
            synchronized(lock) {
                it?.logS()
                com.niki.spotify.remote.logS("[${item.title}] cache job 完成")
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
        callback: (result: com.niki.spotify.remote.ListItemResult) -> Unit
    ) {
        synchronized(lock) {
            val callbacks = uiJobs.getOrPut(item) { mutableListOf() }
            callbacks.add(callback)

            ListItemCachePool.fetchR(item.id, offset).let { result ->
                when (result) {
                    com.niki.spotify.remote.ListItemResult.Error -> return@let

                    is com.niki.spotify.remote.ListItemResult.HasChildren -> {
                        com.niki.spotify.remote.logS("[${item.title} $offset] 获取缓存x${result.list.size}")
                    }

                    com.niki.spotify.remote.ListItemResult.NoChildren -> {
                        com.niki.spotify.remote.logS("[${item.title} $offset] 获取缓存为 noChildren")
                    }
                }
                onCallback(item, result)
                return // 完成任务, 直接返回
            }

            val waitingJob = waitingJobs[item]
            waitingJob?.cancel()
            val job = com.niki.spotify.remote.ContentApi.getUIJob(item, offset) { result ->
                onCache(item, result)
                onCallback(item, result)
            }

            job.invokeOnCompletion {
                synchronized(lock) {
                    it?.logS()
                    com.niki.spotify.remote.logS(item.title + " ui job 完成")
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