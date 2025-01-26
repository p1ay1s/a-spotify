package com.niki.app

import android.graphics.Bitmap
import com.niki.util.CachePool
import com.spotify.protocol.types.ListItem

object LowBitmapCachePool : CachePool<String, Bitmap>(120)
object BitmapCachePool : CachePool<String, Bitmap>(60)
object ItemCachePool : CachePool<String, MutableList<ListItem>>(20)