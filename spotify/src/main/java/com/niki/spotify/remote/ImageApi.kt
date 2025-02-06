package com.niki.spotify.remote

import android.graphics.Bitmap
import com.niki.spotify.remote.RemoteManager.remote
import com.niki.util.getPlaceholderBitmap
import com.spotify.protocol.types.Image
import com.spotify.protocol.types.ImageUri
import kotlinx.coroutines.sync.Semaphore

object ImageApi {

    private val semaphore = Semaphore(MAX_CALL_COUNT) // 最多并发获取的内容数量上限

    fun loadImage(uri: String, size: Image.Dimension? = null): Bitmap {
        doNotRunThisOnMain()
        val errorBitmap = getPlaceholderBitmap(0)
        val bitmap = if (size != null)
            remote?.imagesApi?.getImage(ImageUri(uri), size)?.get()
        else
            remote?.imagesApi?.getImage(ImageUri(uri))?.get()
        return bitmap ?: errorBitmap
    }

    fun loadImage(uri: String, size: Image.Dimension, callback: (Bitmap) -> Unit) {
        val errorBitmap = getPlaceholderBitmap(0)
        remote?.imagesApi?.getImage(ImageUri(uri), size)
            ?.get {
                it?.let(callback) ?: callback(errorBitmap)
            }
    }
}