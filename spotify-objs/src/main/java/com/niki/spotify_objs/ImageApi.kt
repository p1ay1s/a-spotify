package com.niki.spotify_objs

import android.graphics.Bitmap
import com.niki.spotify_objs.RemoteManager.remote
import com.niki.util.getPlaceholderBitmap
import com.spotify.protocol.types.Image
import com.spotify.protocol.types.ImageUri

object ImageApi {

    fun loadImage(uri: String, size: Image.Dimension? = null): Bitmap {
        checkThread()
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