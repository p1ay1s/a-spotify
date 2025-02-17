package com.niki.app.ui

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.animation.addListener
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.niki.app.R
import com.niki.util.copy
import com.zephyr.base.extension.getRootWidth
import com.zephyr.base.extension.setSize
import kotlin.math.roundToInt

/**
 * 支持显示加载状态的 seekbar
 */
class LoadingSeekbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatSeekBar(context, attrs, defStyleAttr) {

    companion object {
        private const val SEEKBAR_SCALE = 15.0 // 进度条的细腻程度, 越大越细腻
        const val SEEKBAR_MAX = (SEEKBAR_SCALE * 17).toInt()

        private const val ANIM_DURATION = 700L
        private const val ROTATION_SENSE = 40L

        private const val START_COLOR = "#26FFFFFF"

        private const val END_COLOR = "#72FFFFFF"
    }

    var isLoading = false
        set(value) {
            if (field == value) return
            field = value
            isEnabled = !isLoading

            progress = 0
            if (value) {
                loadingAnimator?.start()
            } else {
                // 标记停止
                shouldStopAnimation = true
            }
        }

    private var loadingAnimator: ValueAnimator? = null
    private var shouldStopAnimation = false
    private var currentCycle = 0

    private var originalDrawable: Drawable? = null

    private var sizePercent: Float = 0f

    init {
        context.obtainStyledAttributes(attrs, R.styleable.LoadingSeekbar).run {
            sizePercent = getFloat(R.styleable.LoadingSeekbar_sizeToPercentOfWidth, 0f)
            recycle()
        }

        post { max = SEEKBAR_MAX }
        originalDrawable = progressDrawable.copy()

        loadingAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = ANIM_DURATION
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = FastOutSlowInInterpolator()

            addUpdateListener { animation ->
                if (animation.currentPlayTime % ANIM_DURATION < ROTATION_SENSE) { // 在动画时间接近一次时调转 view 已达到往复的效果
                    rotation =
                        if (animation.currentPlayTime % (ANIM_DURATION * 2) < ROTATION_SENSE) 0F else 180F
                }

                val progress = animation.animatedValue as Float
                setProgress((progress * max).roundToInt())

                progressDrawable?.clearColorFilter()
                progressDrawable?.setTint(
                    ArgbEvaluator().evaluate(
                        progress,
                        Color.parseColor(START_COLOR),
                        Color.parseColor(END_COLOR)
                    ) as Int
                )
            }

            // 添加动画监听器
            addListener(
                onRepeat = {
                    currentCycle++
                    // 一个完整的来回算作一个周期( 因为使用了REVERSE模式 )
                    if (shouldStopAnimation && currentCycle % 2 == 0) {
                        cancel()
                        rotation = 0F
                        shouldStopAnimation = false
                        currentCycle = 0
                        progress = 0

                        originalDrawable.copy()?.let { progressDrawable = it }
                    }
                }
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isLoading) return false // 直接消费
        return super.onTouchEvent(event)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        loadingAnimator?.cancel()
        originalDrawable = null
        loadingAnimator = null
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
