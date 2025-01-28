package com.niki.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class CachePool<ID, D>(initSize: Int) {
    protected val TAG = this::class.java.simpleName
    protected val scope = CoroutineScope(Dispatchers.IO)

    var size: Int = -1
        get() = cachePools.size
        set(value) {
            if (value <= 0) return
            field = value // LinkedHashMap 的 removeEldestEntry 会自动处理池的数量
        }

    init {
        size = initSize
    }

    val used: Int
        get() {
            val u = cachePools.filter { it.value != null }.size
            return u
        }

    // 使用 LinkedHashMap 并重写 removeEldestEntry 方法
    protected val cachePools = object : LinkedHashMap<ID, D>(16, 0.75f, false) {

//        accessOrder = false (默认值)：按插入顺序维护条目顺序
//        accessOrder = true：按访问顺序维护（LRU策略）

        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<ID, D>?): Boolean {
            return size > this@CachePool.size
        }
    }

    open fun cache(id: ID, data: D) = scope.launch(Dispatchers.IO) {
        cachePools[id] = data
    }

    open fun fetch(id: ID): D? {
        // 获取池时也会更新其最近使用状态
        return cachePools[id]
    }

    // 删减, 保留 percent% 的数据
    open fun trim(percent: Int) {
        val targetSize = (size * percent / 100).coerceAtLeast(1)
        trimTo(targetSize)
    }

    // 删减, 保留 count 最多个数据
    open fun trimTo(count: Int) {
        while (size > count) {
            val eldestKey = cachePools.keys.firstOrNull()
            eldestKey?.let { cachePools.remove(it) }
        }
    }

    open fun clear() {
        cachePools.clear()
    }
}