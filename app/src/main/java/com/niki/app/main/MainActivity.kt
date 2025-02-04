package com.niki.app.main

import android.annotation.SuppressLint
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.view.View
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.niki.app.DevApiTest
import com.niki.app.GetSpotifyCode
import com.niki.app.GetTokensWithCode
import com.niki.app.NetViewModel
import com.niki.app.R
import com.niki.app.TokenInitOk
import com.niki.app.TokenRequestError
import com.niki.app.TokensRefreshed
import com.niki.app.databinding.ActivityMainBinding
import com.niki.app.listen_now.ListenNowFragment
import com.niki.app.ui.LoadingDialog
import com.niki.app.util.Fragments
import com.niki.app.util.appLoadingDialog
import com.niki.app.util.getSeekBarProgress
import com.niki.app.util.loadLargeImage
import com.niki.app.util.showMDDialog
import com.niki.spotify.remote.PlayerApi
import com.niki.spotify.remote.RemoteManager
import com.niki.util.Point
import com.niki.util.getIntersectionPoint
import com.niki.util.loadRadiusBitmap
import com.niki.util.toBlurDrawable
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.zephyr.base.extension.TAG
import com.zephyr.base.extension.toast
import com.zephyr.base.log.logE
import com.zephyr.vbclass.ViewBindingActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
class MainActivity : ViewBindingActivity<ActivityMainBinding>() {

    companion object {
        private const val SEEKBAR_SCALE = 15.0 // 进度条的细腻程度, 越大越细腻

        const val SEEKBAR_MAX = SEEKBAR_SCALE * 17

        const val BOTTOM_NAV_WEIGHT = 0.115
        const val MINI_PLAYER_WEIGHT = 0.08

        const val MINI_COVER_SIZE = 0.8F // 占 mini player 高度的百分比

        private const val COVER_SCALE_K = -1.3F

        const val TWO_PRESSES_TO_EXIT_APP_TIME = 3000
    }

    private val playerBehavior
        get() = BottomSheetBehavior.from(binding.player)

    private val netViewModel by lazy { ViewModelProvider(this)[NetViewModel::class.java] }
    private val mainViewModel by lazy { ViewModelProvider(this)[MainViewModel::class.java] }

    private lateinit var authManager: SpotifyAuthManager
    private lateinit var sizeManager: MainActivitySizeManager

    private lateinit var bottomSheetCallbackImpl: BottomSheetCallbackImpl // 需要用同一监听器来取消监听

    override fun ActivityMainBinding.initBinding() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            useFullScreen()

        appLoadingDialog = LoadingDialog(this@MainActivity)

        playerApi = com.niki.spotify.remote.PlayerApi
        lifecycleOwner = this@MainActivity

        sizeManager = MainActivitySizeManager(this@MainActivity)
        sizeManager.setSizes(binding)

        authManager = SpotifyAuthManager(this@MainActivity)
        authManager.setCallback { result -> // spotify 授权完成后的回调
            val response = AuthorizationClient.getResponse(result.resultCode, result.data)
            response.run {
                when (type) {
                    AuthorizationResponse.Type.CODE
                    -> netViewModel.sendIntent(GetTokensWithCode(code))

                    AuthorizationResponse.Type.TOKEN -> {}

                    else -> "授权失败: ${type.name}".toast()
                }
            }
        }

        seekbar.setOnSeekBarChangeListener(OnSeekListenerImpl())
        seekbar.max = SEEKBAR_MAX.toInt()

        hostView.apply {
            fragmentManager = supportFragmentManager
            addHost(R.id.index_me)
            addHost(R.id.index_search)
            addHost(
                R.id.index_listen_now,
                Fragments.LISTEN_NOW,
                ListenNowFragment()
            )
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            hostView.switchHost(item.itemId, R.anim.fade_in, R.anim.fade_out)
            true
        }


        floatButton.setOnClickListener {
            authManager.authenticate()
        }

        bottomSheetCallbackImpl = BottomSheetCallbackImpl()

        playerBehavior.apply {
            isHideable = false
            state = BottomSheetBehavior.STATE_COLLAPSED
            peekHeight = sizeManager.bottomNavHeight + sizeManager.miniPlayerHeight
            addBottomSheetCallback(bottomSheetCallbackImpl)
            bottomSheetCallbackImpl.onSlide(player, 0.0F) // 手动复位

        }

        player.setOnClickListener {
            if (playerBehavior.state != BottomSheetBehavior.STATE_EXPANDED)
                playerBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        startSeekbarStateCheckJob()

        com.niki.spotify.remote.RemoteManager.isConnected.observe(this@MainActivity) {
            if (it)
                com.niki.spotify.remote.PlayerApi.startListen()
            floatButton.visibility = if (it) View.INVISIBLE else View.VISIBLE
        }

        com.niki.spotify.remote.PlayerApi.coverUrl.observe(this@MainActivity) {
            loadLargeImage(it) { bitmap ->
                if (playerBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
                    adjustCover(0F) // 让图片立即复位

                loadRadiusBitmap(bitmap, cover, 35)
                toBlurDrawable(bitmap) { blurDrawable ->
                    val transitionDrawable =
                        TransitionDrawable(arrayOf(player.background, blurDrawable))
                    player.background = null
                    player.background = transitionDrawable
                    transitionDrawable.startTransition(450)
                }
            }
        }

        com.niki.spotify.remote.PlayerApi.isPaused.observe(this@MainActivity) { isPaused -> // 暂停图片切换
            val res = if (isPaused) R.drawable.ic_play else R.drawable.ic_pause
            play.setImageResource(res)
            miniPlay.setImageResource(res)
        }

        com.niki.spotify.remote.PlayerApi.isLoading.observe(this@MainActivity) { // seekbar 加载中
            seekbar.isLoading = it
        }

        lifecycleScope.launch {
            netViewModel.uiEffectFlow.collect { effect ->
                when (effect) {
                    TokensRefreshed -> {}
                    GetSpotifyCode -> authManager.authenticate()
                    is TokenRequestError -> showMDDialog(
                        "TokenRequestError",
                        "${effect.code}\n${effect.msg}"
                    )

                    TokenInitOk -> {
                        netViewModel.sendIntent(DevApiTest)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        authManager.release()
        sizeManager.activity = null
        playerBehavior.removeBottomSheetCallback(bottomSheetCallbackImpl)
        binding.seekbar.setOnSeekBarChangeListener(null)
        super.onDestroy()
    }

    private fun startSeekbarStateCheckJob() {
        lifecycleScope.launch(Dispatchers.IO) {
            while (true) {
                if (mainViewModel.allowAutoSetProgress) {
                    val progress = getSeekBarProgress()
                    binding.seekbar.progress = progress
                }
                delay(50)
            }
        }
    }

    private fun useFullScreen() {
        // 应用全屏时, 用户仍然可以从屏幕顶部下拉唤出状态栏, 此行代码实现当用户唤出状态栏后, 自动隐藏状态栏
        WindowCompat.getInsetsController(window, window.decorView).systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        enableEdgeToEdge()
    }

    /**
     * 用一个 [0, 1] 区间内的数设置 cover 的大小及位置
     *
     * pivot 计算原理: 以 cover 左上角为原点, 求完全缩小和原始尺寸的 cover 的右上角、左下角坐标的交点即为 pivot 点
     */
    private fun adjustCover(slideOffset: Float) = binding.cover.run {
        val coverHeight = height // cover 的宽高
        if (coverHeight == 0) return@run // 若未加载出图片则返回

        val minTopMargin =
            (sizeManager.miniPlayerHeight - sizeManager.minCoverHeight) / 2F  // 最小化 cover 的顶部 margin
        val minLeftMargin = 0.1F * sizeManager.miniPlayerHeight // 左边 margin

        val minScale = sizeManager.minCoverHeight.toFloat() / coverHeight // 使封面宽高到达最小的 scale 因子

        // (1 - t) * C + t * A 当 slide offset 约为 0 时结果为 minScale, 约为 1 时结果也为 1, 作用是限定 cover 的尺寸
        // 也可以使用其他非线性的公式, 会更灵动
        var scale = (1 - slideOffset) * minScale + slideOffset
        val k = COVER_SCALE_K * slideOffset * slideOffset - COVER_SCALE_K * slideOffset + 1
        scale *= k

        scaleX = scale
        scaleY = scale

        /*
        以 bottom sheet behavior 的 **左上角** 为参考系:

            a : 右上角, c : 左下角
            []

                b : 右上角, d : 左下角
                -------------
                |           |
                |   cover   |
                |           |
                |           |
                -------------
         */
        val pivotPoint = getIntersectionPoint( // 求完全展开和完全收缩的方形对应的点(用于计算 pivot)
            Point(
                minLeftMargin + minScale * coverHeight,
                minTopMargin
            ), // a - coverHeight 其实是 coverWidth, 两者等大
            Point(right.toFloat(), top.toFloat()), // b
            Point(minLeftMargin, minTopMargin + minScale * coverHeight), // c
            Point(left.toFloat(), bottom.toFloat()) // d
        ) // 此处得到的坐标并不是以 cover 的左上角为原点, 需要再计算

        pivotPoint?.let {
            val pivot = Point(pivotPoint.x - left, pivotPoint.y - top)
            pivotX = pivot.x
            pivotY = pivot.y
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val pair = binding.hostView.getActiveHost()?.peek()

        if (playerBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            playerBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            return
        }

        if (pair == null) {
            twoClicksToExit()
            return
        }

        when (pair.first) {
            Fragments.LISTEN_NOW -> twoClicksToExit()

            else -> {
                val success = binding.hostView.getActiveHost()?.popFragment(
                    R.anim.fade_in,
                    R.anim.right_exit
                )
                logE(TAG, "pop fragment result: $success")
            }
        }
    }

    // 双击退出应用的逻辑
    private fun twoClicksToExit() {
        val time = System.currentTimeMillis() - mainViewModel.lastBackPressedTimeSet

        if (time <= TWO_PRESSES_TO_EXIT_APP_TIME) {
            finishAffinity()
        } else {
            mainViewModel.lastBackPressedTimeSet = System.currentTimeMillis()
            toast("再次点击退出")
        }
    }


    /**
     * bottom sheet behavior 监听器
     *
     * 绑定播放器和导航栏(播放器展开时导航栏收缩)
     */
    inner class BottomSheetCallbackImpl : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            binding.apply {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        adjustCover(0.0F)
                        miniPlayer.visibility = View.VISIBLE
                    }

                    else ->
                        miniPlayer.visibility = View.INVISIBLE
                }
            }
        }

        /**
         * [0, 1] 表示介于折叠和展开状态之间, [-1, 0] 介于隐藏和折叠状态之间, 此处由于禁止 hide 所以只会取值在[0, 1]
         *
         * 此处 slideOffset 完全可以当作一个百分数来看待
         */
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (slideOffset in 0.0F..1.0F) {
                binding.run {
                    shade.alpha = 1 - slideOffset * 15 // 使遮罩逐渐消失, 让背景显现
                    val navY = sizeManager.bottomNavHeight * slideOffset * 2 // 导航栏的偏移量
                    val floatBtnY =
                        sizeManager.parentHeight * -(0.8F * slideOffset + 0.23F) // 大概让按钮跟随 behavior 移动

                    bottomNavigation.translationY = navY
                    line.translationY = navY

                    floatButton.translationY = floatBtnY
                }
            }

            adjustCover(slideOffset)
        }
    }

    /**
     * seekbar 监听器
     */
    inner class OnSeekListenerImpl : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser)
                mainViewModel.notedProgress = progress
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            mainViewModel.allowAutoSetProgress = false
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            com.niki.spotify.remote.PlayerApi.run {
                val percent = mainViewModel.notedProgress / SEEKBAR_MAX
                val time = (percent * duration.value!!).toLong()
                seekTo(time)
                lifecycleScope.launch {
                    delay(170)
                    mainViewModel.allowAutoSetProgress = true // 缓冲一下, 以免闪烁
                }
            }
        }
    }
}