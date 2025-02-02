package com.niki.app.util

import android.graphics.Bitmap
import com.niki.app.MainActivity
import com.niki.app.util.cache_pool.BitmapCachePool
import com.niki.app.util.cache_pool.LowBitmapCachePool
import com.niki.spotify_objs.ImageApi
import com.niki.spotify_objs.PlayerApi
import com.spotify.protocol.types.Image


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