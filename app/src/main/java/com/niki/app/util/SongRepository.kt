package com.niki.app.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.niki.spotify_objs.LOAD_BATCH_SIZE
import com.niki.spotify_objs.ListItemResult
import com.niki.spotify_objs.logS
import com.spotify.protocol.types.ListItem

/**
 * 由于此 fragment 在 显示前就进行加载, 所以不能使用 viewmodel
 */
class SongRepository {
    lateinit var item: ListItem
    private var isFetching = false
    private var hasMore = true

    private var _list = MutableLiveData<List<ListItem>>(emptyList())
    val list: LiveData<List<ListItem>>
        get() = _list

    var currentOffset = 0
        private set
    private val lock = Any()

    fun reset() = synchronized(lock) {
        currentOffset = 0
        hasMore = true
        _list.value = emptyList()
    }

    /**
     * 首先从缓存获取, 如已有有效数据则回调, 否则获取后缓存并回调
     */
    fun loadData(callback: (Boolean) -> Unit) {
        synchronized(lock) {
            if (isFetching) return
            if (!hasMore) {
                logS("${item.title}: no more")
                return
            }
            isFetching = true
        }

        // 总是获取某一片段的数据
        SpotifyChildrenLoader.getChildrenOfItem(item, currentOffset) { result ->
            synchronized(lock) {
                if (result is ListItemResult.HasChildren) {
                    addList(result.list)
                    callback(true)
                } else {
                    callback(false)
                }

                isFetching = false
            }
        }
    }

    private fun addList(l: List<ListItem>) {
        val original = list.value ?: emptyList()
        _list.value = (original + l).toList()
        currentOffset = _list.value?.size ?: 0
        if (l.size != LOAD_BATCH_SIZE)
            hasMore = false
    }
}