package com.niki.app.main

import com.niki.app.main.MainActivity.Companion.BOTTOM_NAV_WEIGHT
import com.niki.app.main.MainActivity.Companion.MINI_COVER_SIZE
import com.niki.app.main.MainActivity.Companion.MINI_PLAYER_WEIGHT
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

    var miniPlayerHeight: Int = 0
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
        miniPlayerHeight = (parentHeight * MINI_PLAYER_WEIGHT).toInt()
        minCoverHeight = (miniPlayerHeight * MINI_COVER_SIZE).toInt()
        val hostViewHeight = parentHeight - bottomNavHeight - miniPlayerHeight

        hostView.setSize(height = hostViewHeight)
        bottomNavigation.setSize(height = bottomNavHeight)

        cover.setSize((0.7 * parentWidth).toInt())
        cover.setMargins(top = (0.17 * parentHeight).toInt())
        trackName.setMargins(top = (0.02 * parentHeight).toInt())
        seekbar.setMargins(top = (0.02 * parentHeight).toInt())
        play.setMargins(top = (0.1 * parentHeight).toInt())

        line.setMargins(bottom = bottomNavHeight)
        floatButton.setMargins(end = (0.08 * parentWidth).toInt())

        miniPlayer.setSize(
            height = miniPlayerHeight,
            width = parentWidth - miniPlayerHeight
        )
        main.post { // 确保有效地设置大小
            seekbar.setSize(width = cover.width)
            miniPlay.run {
                setSize(width)
            }
            miniNext.run {
                setSize(width)
            }
        }
    }
}