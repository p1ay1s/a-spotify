package com.niki.app.ui

import android.content.Context
import android.util.AttributeSet
import com.niki.app.R
import com.zephyr.base.extension.getRootWidth
import com.zephyr.base.extension.setSize
import com.zephyr.base.ui.RippleButton

class RButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RippleButton(context, attrs, defStyleAttr) {
    private var sizePercent: Float = 0f

    init {
        context.obtainStyledAttributes(attrs, R.styleable.RButton).run {
            sizePercent = getFloat(R.styleable.RButton_sizeToPercentOfWidth, 0f)
            recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (sizePercent > 0) {
            val w = context.getRootWidth()
            val size = (w * sizePercent).toInt()
            val newMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
            super.onMeasure(newMeasureSpec, newMeasureSpec)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    fun setSizeToPercent(size: Float) = post {
        val w = context.getRootWidth()
        val mSize = (w * size).toInt()
        setSize(mSize)
    }
}