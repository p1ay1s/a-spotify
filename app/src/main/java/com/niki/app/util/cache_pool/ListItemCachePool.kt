package com.niki.app.util.cache_pool

import com.niki.app.util.ITEM_POOL_INIT_SIZE
import com.niki.spotify_objs.ListItemResult
import com.niki.util.CachePool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ListItemCachePool : CachePool<ListItemCachePool.Key, ListItemResult>(ITEM_POOL_INIT_SIZE) {
    private const val DEFAULT_OFFSET = 0

    fun cacheR(id: String, data: ListItemResult) {
        scope.launch(Dispatchers.IO) {
            synchronized(lock) {
                val offset = (data as? ListItemResult.HasChildren)?.offset ?: DEFAULT_OFFSET
                val key = Key(id, offset)
                super.cache(key, data)
            }
        }
    }

    fun fetchR(id: String): ListItemResult {
        val key = Key(id, DEFAULT_OFFSET)
        return super.fetch(key) ?: return ListItemResult.Error
    }

    fun fetchR(id: String, offset: Int): ListItemResult {
        val first: ListItemResult = fetchR(id)
        if (first is ListItemResult.NoChildren && offset != 0) // 如果被标记过无子但又对其他 offset 进行缓存 
            throw Exception("对 offset 0 标记过 no children 但又缓存")

        val key = Key(id, offset)
        return super.fetch(key) ?: return ListItemResult.Error
    }

    data class Key(
        val id: String,
        val offset: Int
    )
}

