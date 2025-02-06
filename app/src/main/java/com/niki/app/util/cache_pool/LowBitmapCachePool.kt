package com.niki.app.util.cache_pool

import android.graphics.Bitmap
import com.niki.app.LOW_BITMAP_POOL_INIT_SIZE
import com.niki.util.CachePool

object LowBitmapCachePool : CachePool<String, Bitmap>(LOW_BITMAP_POOL_INIT_SIZE)