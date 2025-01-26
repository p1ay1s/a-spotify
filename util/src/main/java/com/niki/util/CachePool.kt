package com.niki.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class CachePool<ID, D>(private val initSize: Int) {
    protected val TAG = this::class.java.simpleName
    private val scope = CoroutineScope(Dispatchers.IO)

    // 使用 LinkedHashMap 并重写 removeEldestEntry 方法
    private val cachePools = object : LinkedHashMap<ID, D>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<ID, D>?): Boolean {
            return size > this@CachePool.size
        }
    }

    var size: Int = -1
        get() = cachePools.size
        set(value) {
            if (value <= 0) return
            field = value // LinkedHashMap 的 removeEldestEntry 会自动处理池的数量
        }

    fun cache(id: ID, data: D) = scope.launch(Dispatchers.IO) {
        cachePools[id] = data
    }

    fun fetch(id: ID): D? {
        // 获取池时也会更新其最近使用状态
        return cachePools[id]
    }
}