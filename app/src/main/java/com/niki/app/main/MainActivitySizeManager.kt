package com.niki.app.main

import com.niki.app.databinding.ActivityMainBinding
import com.zephyr.base.extension.getScreenHeight
import com.zephyr.base.extension.getScreenWidth
import com.zephyr.base.extension.setMargins
import com.zephyr.base.extension.setSize

class MainActivitySizeManager(var activity: MainActivity?) {
    var parentHeight: Int = 0
        private set

    var parentWidth: Int = 0
        private set

    var bottomNavHeight: Int = 0
        private set

    var miniPlayerRootHeight: Int = 0
        private set

    var minCoverHeight: Int = 0
        private set

    /**
     * 由于根部局为 CoordinatorLayout 许多 view 的大小难以在 xml 中设置所以统一用代码实现
     */
    fun setSizes(binding: ActivityMainBinding) = binding.run {
        if (activity == null) return@run
        parentHeight = activity!!.getScreenHeight()
        parentWidth = activity!!.getScreenWidth()

        bottomNavHeight = (parentHeight * BOTTOM_NAV_WEIGHT).toInt()
        miniPlayerRootHeight = (parentHeight * MINI_PLAYER_WEIGHT).toInt()
        minCoverHeight = (miniPlayerRootHeight * MINI_COVER_SIZE).toInt()
        val hostViewHeight = parentHeight - bottomNavHeight - miniPlayerRootHeight

        hostView.setSize(height = hostViewHeight)
        bottomNavigation.setSize(height = bottomNavHeight)

        coverImageView.setMargins(top = (0.17 * parentHeight).toInt())
        trackName.setMargins(top = (0.02 * parentHeight).toInt())
        seekbar.setMargins(top = (0.02 * parentHeight).toInt())
        playButton.setMargins(top = (0.1 * parentHeight).toInt())

        line.setMargins(bottom = bottomNavHeight)
        connectButton.setMargins(end = (0.08 * parentWidth).toInt())

        miniPlayerRoot.setSize(
//            height = miniPlayerRootHeight,
            width = parentWidth - miniPlayerRootHeight
        )
    }
}