package com.niki.app.util.cache_pool

import com.niki.app.util.ITEM_POOL_INIT_SIZE
import com.niki.spotify.remote.ListItemResult
import com.niki.util.CachePool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ListItemCachePool : CachePool<ListItemCachePool.Key, com.niki.spotify.remote.ListItemResult>(ITEM_POOL_INIT_SIZE) {
    private const val DEFAULT_OFFSET = 0

    fun cacheR(id: String, data: com.niki.spotify.remote.ListItemResult) {
        scope.launch(Dispatchers.IO) {
            synchronized(lock) {
                val offset = (data as? com.niki.spotify.remote.ListItemResult.HasChildren)?.offset ?: DEFAULT_OFFSET
                val key = Key(id, offset)
                super.cache(key, data)
            }
        }
    }

    fun fetchR(id: String): com.niki.spotify.remote.ListItemResult {
        val key = Key(id, DEFAULT_OFFSET)
        return super.fetch(key) ?: return com.niki.spotify.remote.ListItemResult.Error
    }

    fun fetchR(id: String, offset: Int): com.niki.spotify.remote.ListItemResult {
        val first: com.niki.spotify.remote.ListItemResult = fetchR(id)
        if (first is com.niki.spotify.remote.ListItemResult.NoChildren && offset != 0) // 如果被标记过无子但又对其他 offset 进行缓存
            throw Exception("对 offset 0 标记过 no children 但又缓存")

        val key = Key(id, offset)
        return super.fetch(key) ?: return com.niki.spotify.remote.ListItemResult.Error
    }

    data class Key(
        val id: String,
        val offset: Int
    )
}

