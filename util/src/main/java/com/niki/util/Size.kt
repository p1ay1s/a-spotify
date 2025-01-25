package com.niki.util

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsets
import androidx.annotation.RequiresApi

fun View.getRootWidth(): Int {
    return resources.displayMetrics.widthPixels
}

fun View.getRootHeight(): Int {
    return resources.displayMetrics.heightPixels
}

@RequiresApi(Build.VERSION_CODES.R)
fun Activity.calculateStatusBarHeight(): Int {
    val windowMetrics = windowManager.currentWindowMetrics
    val insets = windowMetrics.windowInsets
        .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
    return insets.top
}

@RequiresApi(Build.VERSION_CODES.R)
fun Activity.calculateNavigationBarHeight(): Int {
    val windowMetrics = windowManager.currentWindowMetrics
    val insets = windowMetrics.windowInsets
        .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
    return insets.bottom
}

@RequiresApi(Build.VERSION_CODES.R)
fun Activity.getScreenHeight(): Int {
    val windowMetrics = windowManager.currentWindowMetrics
    return windowMetrics.bounds.height()
}

@RequiresApi(Build.VERSION_CODES.R)
fun Activity.getScreenWidth(): Int {
    val windowMetrics = windowManager.currentWindowMetrics
    return windowMetrics.bounds.width()
}