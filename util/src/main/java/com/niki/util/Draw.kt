package com.niki.util

import android.graphics.drawable.Drawable


fun Drawable?.copy(): Drawable? = this?.constantState?.newDrawable()?.mutate()