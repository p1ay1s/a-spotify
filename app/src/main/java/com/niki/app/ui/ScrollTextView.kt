package com.niki.app.ui

//import android.graphics.Rect
//import androidx.lifecycle.findViewTreeLifecycleOwner
//import androidx.lifecycle.lifecycleScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.isActive
//import kotlinx.coroutines.launch

//        when (mode) {
//            ScrollMode.RESTART -> { // 重新开始模式
//                when {
//                    speed > 0 ->
//                        if (offsetX >= textBounds.width() + RESTART_EXTRA_PADDING)
//                            offsetX = -textBounds.width()
//
//                    speed < 0 ->
//                        if (offsetX <= -textBounds.width() - RESTART_EXTRA_PADDING)
//                            offsetX = textBounds.width()
//                }
//            }
//
//            ScrollMode.BOUNCE -> { // 来回弹跳模式
//                if ((offsetX >= BOUNCE_EXTRA_PADDING && speed > 0) || (offsetX <= (width - textBounds.width() - BOUNCE_EXTRA_PADDING) && speed < 0)) {
//                    speed = -speed
//                    delay(PAUSE_TIME)
//                }
//            }
//        }
//        offsetX += speed


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

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

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
//        offsetX = 0数量、
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scrollJob?.cancel() // 及时取消协程
    }
}

data class ScrollSpeed(
    val value: Float,
    val direction: Direction
) {
    enum class Direction {
        LEFT, RIGHT
    }

    fun toPixels() = value * if (direction == Direction.LEFT) -1 else 1

    companion object {
        fun left(value: Float) = ScrollSpeed(value, Direction.LEFT)
        fun right(value: Float) = ScrollSpeed(value, Direction.RIGHT)
    }
}

class ScrollTextView1 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    companion object {
        private const val PAUSE_TIME = 1500L
        private const val EXTRA_PADDING = 5
    }

    // 滚动策略接口
    private interface ScrollStrategy {
        fun getEndX(textWidth: Int, viewWidth: Int, startX: Int): Int
        fun onAnimationEnd(textWidth: Int, viewWidth: Int)
    }

    // 滚动模式
    enum class ScrollMode {
        BOUNCE, RESTART
    }

    // 缓存的测量结果
    private data class MeasureCache(
        val textWidth: Int,
        val baseline: Float
    )

    private var measureCache: MeasureCache? = null
    private var offsetX = 0
    private var animator: ValueAnimator? = null
    private var isScrolling = true

    // 滚动速度，默认向左滚动
    var speed: ScrollSpeed = ScrollSpeed.left(200f)
        set(value) {
            field = value
            resetAndStartAnimation()
        }

    var mode: ScrollMode = ScrollMode.BOUNCE
        set(value) {
            field = value
            resetAndStartAnimation()
        }

    private val currentStrategy: ScrollStrategy
        get() = when (mode) {
            ScrollMode.BOUNCE -> BounceStrategy()
            ScrollMode.RESTART -> RestartStrategy()
        }

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        isHorizontalFadingEdgeEnabled = true
        setWillNotDraw(false)
    }

    private inner class BounceStrategy : ScrollStrategy {
        override fun getEndX(textWidth: Int, viewWidth: Int, startX: Int): Int {
            return if (speed.direction == ScrollSpeed.Direction.RIGHT) {
                EXTRA_PADDING
            } else {
                viewWidth - textWidth - EXTRA_PADDING
            }
        }

        override fun onAnimationEnd(textWidth: Int, viewWidth: Int) {
            speed = ScrollSpeed(
                speed.value, if (speed.direction == ScrollSpeed.Direction.LEFT)
                    ScrollSpeed.Direction.RIGHT else ScrollSpeed.Direction.LEFT
            )
            postDelayed({ if (isScrolling) startScrollAnimation() }, PAUSE_TIME)
        }
    }

    private inner class RestartStrategy : ScrollStrategy {
        override fun getEndX(textWidth: Int, viewWidth: Int, startX: Int): Int {
            return if (speed.direction == ScrollSpeed.Direction.RIGHT) {
                textWidth + EXTRA_PADDING
            } else {
                -(textWidth + EXTRA_PADDING)
            }
        }

        override fun onAnimationEnd(textWidth: Int, viewWidth: Int) {
            offsetX = if (speed.direction == ScrollSpeed.Direction.RIGHT) {
                -textWidth
            } else {
                textWidth
            }
            startScrollAnimation()
        }
    }

    private fun updateMeasureCache() {
        val text = text.toString()
        val textWidth = paint.measureText(text).toInt()
        val fontMetrics = paint.fontMetrics
        val baseline = (height - fontMetrics.bottom - fontMetrics.top) / 2
        measureCache = MeasureCache(textWidth, baseline)
    }

    private fun shouldScroll(): Boolean {
        return (measureCache?.textWidth ?: 0) > width
    }

    private fun resetAndStartAnimation() {
        animator?.cancel()
        offsetX = 0
        startScrollAnimation()
    }

    private fun startScrollAnimation() {
        val cache = measureCache ?: return
        if (!shouldScroll() || !isScrolling) return

        val strategy = currentStrategy
        val startX = offsetX
        val endX = strategy.getEndX(cache.textWidth, width, startX)

        animator?.cancel()
        animator = ValueAnimator.ofInt(startX, endX).apply {
            duration = (abs(endX - startX) * (1000f / speed.value)).toLong()
            interpolator = LinearInterpolator()

            addUpdateListener { animation ->
                offsetX = animation.animatedValue as Int
                invalidate()
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (!isScrolling) return
                    strategy.onAnimationEnd(cache.textWidth, width)
                }
            })

            start()
        }
    }

    fun pause() {
        isScrolling = false
        animator?.pause()
    }

    fun resume() {
        isScrolling = true
        animator?.resume()
    }

    override fun onDraw(canvas: Canvas) {
        val cache = measureCache ?: return
        if (!shouldScroll()) {
            super.onDraw(canvas)
            return
        }

        canvas.save()
        canvas.clipRect(0, 0, width, height)
        canvas.drawText(text.toString(), offsetX.toFloat(), cache.baseline, paint)
        canvas.restore()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateMeasureCache()
        resetAndStartAnimation()
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        post {
            updateMeasureCache()
            resetAndStartAnimation()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post {
            updateMeasureCache()
            startScrollAnimation()
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        animator = null
        super.onDetachedFromWindow()
    }
}

//
//class ScrollTextView1 @JvmOverloads constructor(
//    context: Context,
//    attrs: AttributeSet? = null,
//    defStyleAttr: Int = 0
//) : AppCompatTextView(context, attrs, defStyleAttr) {
//
//    companion object {
//        private const val FPS = 90 // 帧率
//        private const val FRAME_TIME_MILLIS = 1000L / FPS
//
//        private const val PAUSE_TIME = 1500L
//
//        private const val BOUNCE_EXTRA_PADDING = 5
//        private const val RESTART_EXTRA_PADDING = 5
//    }
//
//    // 滚动模式枚举
//    enum class ScrollMode {
//        BOUNCE,   // 来回弹跳
//        RESTART   // 重新开始
//    }
//
//    // 配置参数
//    private var offsetX = 0
//    private val textBounds = Rect()
//
//    var mode = ScrollMode.BOUNCE
//    var speed = 2
//
//    private var isScrolling = true
//    private var lastScrollTimeSet = 0L
//
//    private var scrollJob: Job? = null
//
//    init {
//        if (mode == ScrollMode.BOUNCE)
//            speed = -speed
//    }
//
//    fun pause() {
//        isScrolling = false
//    }
//
//    fun resume() {
//        isScrolling = true
//    }
//
//    // 开始滚动协程
//    private fun startScrollCoroutine() {
//        // 获取视图的生命周期
//        val lifecycleOwner = findViewTreeLifecycleOwner() ?: return
//
//        scrollJob = lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
//            while (isActive) {
//                val isTime = System.currentTimeMillis() - lastScrollTimeSet >= FRAME_TIME_MILLIS
//
//                if (isScrolling && shouldScroll() && isTime) {
//                    updateOffset()
//                    lastScrollTimeSet = System.currentTimeMillis()
//                    invalidate()
//                }
//
//                delay(FRAME_TIME_MILLIS / 4)
//            }
//        }
//    }
//
//    private fun shouldScroll(): Boolean {
//        paint.getTextBounds(text.toString(), 0, text.length, textBounds)
//        return textBounds.width() > width
//    }
//
//    private suspend fun updateOffset() {
//        when (mode) {
//            ScrollMode.RESTART -> { // 重新开始模式
//                when {
//                    speed > 0 ->
//                        if (offsetX >= textBounds.width() + RESTART_EXTRA_PADDING)
//                            offsetX = -textBounds.width()
//
//                    speed < 0 ->
//                        if (offsetX <= -textBounds.width() - RESTART_EXTRA_PADDING)
//                            offsetX = textBounds.width()
//                }
//            }
//
//            ScrollMode.BOUNCE -> { // 来回弹跳模式
//                if ((offsetX >= BOUNCE_EXTRA_PADDING && speed > 0) || (offsetX <= (width - textBounds.width() - BOUNCE_EXTRA_PADDING) && speed < 0)) {
//                    speed = -speed
//                    delay(PAUSE_TIME)
//                }
//            }
//        }
//        offsetX += speed
//    }
//
//    override fun onDraw(canvas: Canvas) {
//        if (!shouldScroll()) {
//            super.onDraw(canvas) // 如果暴露在外面会导致两段文字
//            return
//        }
//
//        canvas.run {
//            save() // 裁剪绘制区域
//            clipRect(0, 0, width, height)
//            drawText( // 绘制滚动文本
//                text.toString(),
//                offsetX.toFloat(),
//                calculateBaseline(),
//                paint
//            )
//            restore()
//        }
//    }
//
//    private fun calculateBaseline(): Float {
//        val fontMetrics = paint.fontMetrics
//        return (height - fontMetrics.bottom - fontMetrics.top) / 2 // textview 高度 - 文本高度即空余部分的高度, 再 / 2 就是垂直居中时下面的 margin
//    }
//
//    override fun onAttachedToWindow() {
//        super.onAttachedToWindow()
//        startScrollCoroutine() // 启动协程
//    }
//
//    override fun onTextChanged(
//        text: CharSequence?,
//        start: Int,
//        lengthBefore: Int,
//        lengthAfter: Int
//    ) {
//        super.onTextChanged(text, start, lengthBefore, lengthAfter)
//    }
//
//    override fun onDetachedFromWindow() {
//        super.onDetachedFromWindow()
//        scrollJob?.cancel() // 及时取消协程
//    }
//}
