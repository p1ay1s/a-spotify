package com.niki.app.util.cache_pool

import android.graphics.Bitmap
import com.niki.app.BITMAP_POOL_INIT_SIZE
import com.niki.util.CachePool

object BitmapCachePool : CachePool<String, Bitmap>(BITMAP_POOL_INIT_SIZE)