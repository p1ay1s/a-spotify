package com.niki.app.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.niki.app.databinding.LayoutMiniPlayerBinding
import com.zephyr.base.extension.getRootHeight
import com.zephyr.base.extension.getRootWidth

class MiniPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    companion object {
        private const val MINI_COVER_SIZE = 0.8F // 占 mini player 大小的百分比, 用于计算 root 的宽度

    }

    init {
        val layoutInflater = LayoutInflater.from(context)
        val binding = LayoutMiniPlayerBinding.inflate(layoutInflater)

        val w = context.getRootWidth()
        val h = context.getRootHeight()

        val miniCoverLength = (h * MINI_COVER_SIZE).toInt()
        val rootWidth = w - miniCoverLength
        binding.root.post {
            binding.root.updateLayoutParams {
                width = rootWidth
                height = miniCoverLength
            }
        }
    }
}