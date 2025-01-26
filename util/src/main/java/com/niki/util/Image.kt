package com.niki.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

fun getPlaceholderBitmap(radius: Int): Bitmap {
    return getPlaceholder(radius).toBitmap()
}

fun getPlaceholder(radius: Int) = GradientDrawable().apply {
    shape = GradientDrawable.RECTANGLE
    cornerRadius = radius * 1.2F // 设置圆角半径
    setColor(Color.parseColor("#90909080")) // 设置颜色
}

fun Context.loadRadiusBitmap(bitmap: Bitmap, imageView: ImageView, radius: Int = 30) {
    Glide.with(this)
        .load(bitmap)
        .placeholder(getPlaceholder(radius))
        .transition(DrawableTransitionOptions.withCrossFade())
        .transform(CenterCrop(), RoundedCorners(radius))
        .into(imageView)
}

const val SCALE_IMAGE_SIZE = 64

fun Context.toBlurDrawable(
    bitmap: Bitmap,
    callback: ((Drawable) -> Unit) = {}
) {
    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, SCALE_IMAGE_SIZE, SCALE_IMAGE_SIZE, true)
    Glide.with(this)
        .load(resizedBitmap)
        .fitCenter()
        .transform(BlurTransformation(32))
        .into(object : CustomTarget<Drawable?>() {
            override fun onResourceReady(
                resource: Drawable,
                transition: Transition<in Drawable?>?
            ) {
                callback(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {}

            override fun onLoadFailed(errorDrawable: Drawable?) {
                super.onLoadFailed(errorDrawable)
                Log.e("", "onLoadFailed: 失败")
            }
        })
}

suspend fun calculateAverageColor(bitmap: Bitmap): String = coroutineScope {
    var avg = ""
    async {
        Palette.from(bitmap).generate().let {
            avg = it.darkMutedSwatch?.rgb.toString()
            Log.e("", "calculateAverageColor: $avg")
        }
    }.await()
    avg
}