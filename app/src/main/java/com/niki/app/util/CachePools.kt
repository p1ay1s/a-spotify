package com.niki.app.util

import android.graphics.Bitmap
import com.niki.util.CachePool
import com.spotify.protocol.types.ListItem

object LowBitmapCachePool : CachePool<String, Bitmap>(LOW_BITMAP_POOL_INIT_SIZE)
object BitmapCachePool : CachePool<String, Bitmap>(BITMAP_POOL_INIT_SIZE)
object ItemCachePool : CachePool<String, MutableList<ListItem>>(ITEM_POOL_INIT_SIZE)