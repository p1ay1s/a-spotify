package com.niki.app.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ScrollTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    companion object {
        private const val FPS = 144 // 帧率
        private const val FRAME_TIME_MILLIS = 1000L / FPS

        private const val PAUSE_TIME = 1500L

        private const val BOUNCE_EXTRA_PADDING = 5
        private const val RESTART_EXTRA_PADDING = 30
    }

    // 滚动模式枚举
    enum class ScrollMode {
        BOUNCE,   // 来回弹跳
        RESTART   // 重新开始
    }

    // 配置参数
    private var offsetX = 0
    private val textBounds = Rect()

    var mode = ScrollMode.BOUNCE
    var speed = 2

    private var isScrolling = true

    private var scrollJob: Job? = null

    init {
        if (mode == ScrollMode.BOUNCE)
            speed = -speed
    }

    fun pause() {
        isScrolling = false
    }

    fun resume() {
        isScrolling = true
    }

    // 开始滚动协程
    private fun startScrollCoroutine() {
        // 获取视图的生命周期
        val lifecycleOwner = findViewTreeLifecycleOwner() ?: return

        scrollJob = lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            while (isActive) {
                if (isScrolling && shouldScroll()) {
                    updateOffset()
                    invalidate()
                }

                delay(FRAME_TIME_MILLIS)
            }
        }
    }

    private fun shouldScroll(): Boolean {
        paint.getTextBounds(text.toString(), 0, text.length, textBounds)
        return textBounds.width() > width
    }

    private suspend fun updateOffset() {
        when (mode) {
            ScrollMode.RESTART -> { // 重新开始模式
                when {
                    speed > 0 ->
                        if (offsetX >= textBounds.width() + RESTART_EXTRA_PADDING)
                            offsetX = -textBounds.width()

                    speed < 0 ->
                        if (offsetX <= -textBounds.width() - RESTART_EXTRA_PADDING)
                            offsetX = textBounds.width()
                }
            }

            ScrollMode.BOUNCE -> { // 来回弹跳模式
                if ((offsetX >= BOUNCE_EXTRA_PADDING && speed > 0) || (offsetX <= (width - textBounds.width() - BOUNCE_EXTRA_PADDING) && speed < 0)) {
                    speed = -speed
                    delay(PAUSE_TIME)
                }
            }
        }
        offsetX += speed
    }

    override fun onDraw(canvas: Canvas) {
        if (!shouldScroll()) {
            super.onDraw(canvas) // 如果暴露在外面会导致两段文字
            return
        }

        canvas.run {
            save() // 裁剪绘制区域
            clipRect(0, 0, width, height)
            drawText( // 绘制滚动文本
                text.toString(),
                offsetX.toFloat(),
                calculateBaseline(),
                paint
            )
            restore()
        }
    }

    private fun calculateBaseline(): Float {
        val fontMetrics = paint.fontMetrics
        return (height - fontMetrics.bottom - fontMetrics.top) / 2 // textview 高度 - 文本高度即空余部分的高度, 再 / 2 就是垂直居中时下面的 margin
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startScrollCoroutine() // 启动协程
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scrollJob?.cancel() // 及时取消协程
    }
}