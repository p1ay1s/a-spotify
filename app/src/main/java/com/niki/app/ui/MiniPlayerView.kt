package com.niki.app.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.niki.app.databinding.LayoutMiniPlayerBinding
import com.niki.spotify.remote.PlayerApi
import com.zephyr.base.extension.getRootWidth

class MiniPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    companion object {
        private const val COVER_TOKEN_SIZE = 0.95F // 占 mini player 大小的百分比, 用于计算 root 的宽度
    }

    private val layoutInflater = LayoutInflater.from(context)
    val binding = LayoutMiniPlayerBinding.inflate(layoutInflater, this, true)

    init {
        post {
            val w = context.getRootWidth()

            val placeholderLength = (height * COVER_TOKEN_SIZE).toInt()
            val rootWidth = w - placeholderLength

            binding.playerApi = PlayerApi
            binding.lifecycleOwner = this.findViewTreeLifecycleOwner()

            updateLayoutParams {
                width = rootWidth
            }
        }
    }
}