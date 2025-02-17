package com.niki.app.main

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.niki.app.App
import com.niki.app.databinding.ActivityMainBinding
import com.niki.app.main.MainActivity.Companion.BOTTOM_NAV_WEIGHT
import com.niki.app.main.MainActivity.Companion.HOST_VIEW_WEIGHT
import com.niki.app.main.MainActivity.Companion.MINI_COVER_SIZE
import com.niki.app.main.MainActivity.Companion.MINI_PLAYER_WEIGHT
import com.niki.app.main.MainActivity.Companion.bottomNavHeight
import com.niki.app.main.MainActivity.Companion.hostViewHeight
import com.niki.app.main.MainActivity.Companion.minCoverLength
import com.niki.app.main.MainActivity.Companion.miniPlayerHeight
import com.niki.app.main.MainActivity.Companion.parentHeight
import com.niki.app.main.MainActivity.Companion.parentWidth
import com.zephyr.base.extension.calculateNavigationBarHeight
import com.zephyr.base.extension.calculateStatusBarHeight
import com.zephyr.base.extension.getRootHeight
import com.zephyr.base.extension.getRootWidth
import com.zephyr.base.extension.setMargins
import com.zephyr.base.extension.setSize

@RequiresApi(Build.VERSION_CODES.R)
object MainActivityParamsManager {

    /**
     * 由于根部局为 CoordinatorLayout 许多 view 的大小难以在 xml 中设置所以统一用代码实现
     */
    fun setLayoutParams(binding: ActivityMainBinding, onComplete: (() -> Unit) = {}) = binding.run {
        root.post {
            val activity = App.mainActivity.get() ?: return@post

            val winHeight = activity.getRootHeight() // window 高度
            val statusBarHeight = activity.calculateStatusBarHeight() // 状态栏高度
            val navigationBarHeight = activity.calculateNavigationBarHeight() // 底部导航栏高度

            MainActivity.statusBarHeight = statusBarHeight
            MainActivity.navigationBarHeight = navigationBarHeight
            parentHeight = if (MainActivity.isEnableEdgeToEdge) // 在启用了 edge to edge 后这样才能正确获取屏幕高度
                winHeight + statusBarHeight + navigationBarHeight
            else
                winHeight

            parentWidth = root.context.getRootWidth()

            bottomNavHeight = (parentHeight * BOTTOM_NAV_WEIGHT).toInt()
            miniPlayerHeight = (parentHeight * MINI_PLAYER_WEIGHT).toInt()
            hostViewHeight = (parentHeight * HOST_VIEW_WEIGHT).toInt()

            minCoverLength = (miniPlayerHeight * MINI_COVER_SIZE).toInt()

            hostView.setSize(height = hostViewHeight)
            bottomNavigation.setSize(height = bottomNavHeight)
            BottomSheetBehavior.from(binding.player).peekHeight = bottomNavHeight + miniPlayerHeight

            coverImageView.setMargins(top = (0.17 * parentHeight).toInt())
            trackName.setMargins(top = (0.02 * parentHeight).toInt())
            seekbar.setMargins(top = (0.02 * parentHeight).toInt())
            playButton.setMargins(top = (0.1 * parentHeight).toInt())

            line.setMargins(bottom = bottomNavHeight)
            connectButton.setMargins(end = (0.08 * parentWidth).toInt())
            onComplete()
        }
    }
}