package com.niki.app.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.niki.app.R
import com.zephyr.base.extension.getRootWidth
import com.zephyr.base.extension.setSize

class SquareImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var sizePercent: Float = 0f

    init {
        context.obtainStyledAttributes(attrs, R.styleable.SquareImageView).run {
            sizePercent = getFloat(R.styleable.SquareImageView_sizeToPercentOfWidth, 0f)
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