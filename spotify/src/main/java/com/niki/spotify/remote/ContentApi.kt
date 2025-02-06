package com.niki.spotify.remote

import com.niki.spotify.remote.RemoteManager.remote
import com.spotify.android.appremote.api.ContentApi
import com.spotify.protocol.types.ListItem
import com.spotify.protocol.types.ListItems
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout

object ContentApi {

    private val semaphore = Semaphore(MAX_CALL_COUNT) // 最多并发获取的内容数量上限

    @JvmName("toListItemResult1")
    private fun List<ListItem>?.toListItemResult(offset: Int = 0): ListItemResult {
        return toListItemResult(this, offset)
    }

    @JvmName("toListItemResult2")
    private fun toListItemResult(list: List<ListItem>?, offset: Int = 0): ListItemResult {
        return when {
            list == null ->
                (ListItemResult.Error)

            list.isEmpty() ->
                (ListItemResult.NoChildren)

            else -> {
                val isFull = (list.size == LOAD_BATCH_SIZE)
                (ListItemResult.HasChildren(offset, isFull, list))
            }
        }
    }

    fun getListItems(): ListItems? {
        doNotRunThisOnMain()
        val listItems = remote?.contentApi
            ?.getRecommendedContentItems(ContentApi.ContentType.DEFAULT) // 使用不同的 type 得到的结果似乎是相同的
            ?.get()
        return listItems
    }

    fun getListItems(callback: (ListItems?) -> Unit) {
        remote?.contentApi
            ?.getRecommendedContentItems(ContentApi.ContentType.DEFAULT) // 使用不同的 type 得到的结果似乎是相同的
            ?.get(callback)
    }

    fun getContentList(callback: (List<ListItem>?) -> Unit) {
        getListItems { callback(it?.toList()) }
    }

    fun getContentList(): ListItemResult {
        doNotRunThisOnMain()
        val list = getListItems()?.toList()
        return list.toListItemResult()
    }

    /**
     * 获取 listItem 中的歌单
     *
     * 当返回空 list 则表示获取成功但无数据, 如果返回空则表示错误
     */
    private fun getChildrenOfItem(
        item: ListItem,
        offset: Int,
        size: Int
    ): ListItemResult {
        doNotRunThisOnMain()
        if (!item.hasChild()) return ListItemResult.NoChildren

        val list: List<ListItem>? = remote?.contentApi
            ?.getChildrenOfItem(item, size, offset)
            ?.get()?.toList()

        return list.toListItemResult(offset)
    }

    /**
     * 获取 listItem 中的歌单
     *
     * 当返回空 list 则表示获取成功但无数据, 如果返回空则表示错误
     */
    fun getChildrenOfItem(
        item: ListItem,
        offset: Int,
        size: Int,
        callback: (ListItemResult) -> Unit
    ) = spotifyScope.launch(Dispatchers.IO) {
        if (!item.hasChild()) {
            callback(ListItemResult.NoChildren)
            return@launch
        }

        remote?.contentApi
            ?.getChildrenOfItem(item, size, offset)
            ?.get {
                val list = it?.toList()
                callback(list.toListItemResult(offset))
            }
    }

    /**
     * 有并发限制的 job
     *
     * 统一请求 $LOAD_BATCH_SIZE 个
     */
    fun getWaitJob(
        item: ListItem,
        offset: Int,
        callback: suspend (ListItemResult) -> Unit
    ): Job = spotifyScope.async(context = Dispatchers.IO, start = CoroutineStart.LAZY) {
        semaphore.withPermit {
            ensureActive()
            if (!isActive) return@async

            val list = withTimeout(GET_CHILDREN_TIMEOUT) {
                getChildrenOfItem(item, offset, LOAD_BATCH_SIZE)
            }

            async {
                callback(list)
            }.await()
        }
    }

    /**
     * 为 ui 获取的 job, 不设置并发限制
     *
     * 统一请求 $LOAD_BATCH_SIZE 个
     */
    fun getUIJob(
        item: ListItem,
        offset: Int,
        callback: suspend (ListItemResult) -> Unit
    ): Job = spotifyScope.async(context = Dispatchers.IO, start = CoroutineStart.LAZY) {
        ensureActive()
        if (!isActive) return@async

        val list = withTimeout(GET_CHILDREN_TIMEOUT) {
            getChildrenOfItem(item, offset, LOAD_BATCH_SIZE)
        }

        async {
            callback(list)
        }.await()
    }
}