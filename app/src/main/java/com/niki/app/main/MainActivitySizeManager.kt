package com.niki.app.main

import android.view.View
import com.niki.app.databinding.ActivityMainBinding
import com.niki.app.main.MainActivity.Companion.bottomNavHeight
import com.niki.app.main.MainActivity.Companion.hostViewHeight
import com.niki.app.main.MainActivity.Companion.minCoverLength
import com.niki.app.main.MainActivity.Companion.miniPlayerHeight
import com.niki.app.main.MainActivity.Companion.parentHeight
import com.niki.app.main.MainActivity.Companion.parentWidth
import com.zephyr.base.extension.getRootHeight
import com.zephyr.base.extension.getRootWidth
import com.zephyr.base.extension.setMargins
import com.zephyr.base.extension.setSize

object MainActivitySizeManager {

    /**
     * 由于根部局为 CoordinatorLayout 许多 view 的大小难以在 xml 中设置所以统一用代码实现
     */
    fun setSizes(binding: ActivityMainBinding) = binding.run {
        parentHeight = root.context.getRootHeight()
        parentWidth = root.context.getRootWidth()

        bottomNavHeight = (parentHeight * BOTTOM_NAV_WEIGHT).toInt()
        miniPlayerHeight = (parentHeight * MINI_PLAYER_WEIGHT).toInt()
        hostViewHeight = (parentHeight * HOST_VIEW_WEIGHT).toInt()

        minCoverLength = (miniPlayerHeight * MINI_COVER_SIZE).toInt()

        hostView.resize(height = hostViewHeight)
        bottomNavigation.resize(height = bottomNavHeight)

        coverImageView.setMargins(top = (0.17 * parentHeight).toInt())
        trackName.setMargins(top = (0.02 * parentHeight).toInt())
        seekbar.setMargins(top = (0.02 * parentHeight).toInt())
        playButton.setMargins(top = (0.1 * parentHeight).toInt())

        line.setMargins(bottom = bottomNavHeight)
        connectButton.setMargins(end = (0.08 * parentWidth).toInt())
    }

    private fun View.resize(size: Int) = post { setSize(size) }

    private fun View.resize(width: Int? = null, height: Int? = null) =
        post { setSize(width, height) }
}