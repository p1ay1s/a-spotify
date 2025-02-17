package com.niki.app.main

import android.annotation.SuppressLint
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.view.View
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.niki.app.App
import com.niki.app.AppTokenHelper
import com.niki.app.R
import com.niki.app.TokenEffect
import com.niki.app.TokenIntent
import com.niki.app.databinding.ActivityMainBinding
import com.niki.app.listen_now.ListenNowFragment
import com.niki.app.search.SearchFragment
import com.niki.app.showMDDialog
import com.niki.app.ui.LoadingDialog
import com.niki.app.ui.LoadingSeekbar.Companion.SEEKBAR_MAX
import com.niki.app.util.FragmentTags
import com.niki.app.util.getSeekBarProgress
import com.niki.app.util.loadLargeImage
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

@SuppressLint("SetTextI18n")
class MainActivity : ViewBindingActivity<ActivityMainBinding>() {
    companion object {
        const val COVER_RADIUS = 20

        const val BOTTOM_NAV_WEIGHT = 0.115
        const val MINI_PLAYER_WEIGHT = 0.08
        const val HOST_VIEW_WEIGHT = 1 - BOTTOM_NAV_WEIGHT - MINI_PLAYER_WEIGHT

        const val MINI_COVER_SIZE = 0.8F // 占 mini player 高度的百分比

        const val COVER_SCALE_K = -1.3F

        const val TWO_PRESSES_TO_EXIT_APP_TIME = 3000

        var statusBarHeight = 0
        var navigationBarHeight = 0

        var parentHeight = 0
        var parentWidth = 0

        var bottomNavHeight = 0
        var miniPlayerHeight = 0
        var hostViewHeight = 0

        var minCoverLength = 0

        var isEnableEdgeToEdge = false
    }

    private val playerBehavior
        get() = BottomSheetBehavior.from(binding.player)

    private val mainViewModel by lazy { ViewModelProvider(this)[MainViewModel::class.java] }

    private lateinit var authManager: SpotifyAuthManager

    private lateinit var bottomSheetCallbackImpl: BottomSheetCallbackImpl // 需要用同一监听器来取消监听

    private var seekbarJob: Job? = null

    @RequiresApi(Build.VERSION_CODES.R)
    override fun ActivityMainBinding.initBinding() {
        isEnableEdgeToEdge = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            useFullScreen()

        App.mainActivity = WeakReference(this@MainActivity)
        App.loadingDialog = LoadingDialog(this@MainActivity)

        playerApi = PlayerApi
        lifecycleOwner = this@MainActivity

        bottomSheetCallbackImpl = BottomSheetCallbackImpl()

        MainActivityParamsManager.setLayoutParams(binding) {
            bottomSheetCallbackImpl.onSlide(player, 0.0F) // 手动复位
        }

        authManager = SpotifyAuthManager().apply {
            initLauncher()
            setCallback { result -> // spotify 授权完成后的回调
                val response = AuthorizationClient.getResponse(result.resultCode, result.data)
                response.run {
                    when (type) {
                        AuthorizationResponse.Type.CODE
                        -> AppTokenHelper.sendIntent(TokenIntent.GetWithCode(code))

                        AuthorizationResponse.Type.TOKEN -> {}

                        else -> "授权失败: ${type.name}".toast()
                    }
                }
            }
        }

        seekbar.setOnSeekBarChangeListener(OnSeekListenerImpl())

        initFragments()

        bottomNavigation.setOnItemSelectedListener { item ->
            hostView.switchHost(item.itemId, R.anim.fade_in, R.anim.fade_out)
            true
        }

        connectButton.setOnClickListener {
            authManager.authenticate()
        }

        playerBehavior.apply {
            isHideable = false
            state = BottomSheetBehavior.STATE_COLLAPSED
            addBottomSheetCallback(bottomSheetCallbackImpl)
        }

        player.setOnClickListener {
            if (playerBehavior.state != BottomSheetBehavior.STATE_EXPANDED)
                playerBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        startSeekbarStateCheckJob()

        registerObservers()
    }

    override fun onDestroy() {
        seekbarJob?.cancel()
        authManager.release()
        playerBehavior.removeBottomSheetCallback(bottomSheetCallbackImpl)
        binding.seekbar.setOnSeekBarChangeListener(null)
        super.onDestroy()
    }

    private fun initFragments() {
        binding.hostView.apply {
            fragmentManager = supportFragmentManager
            addHost(R.id.index_me)
            addHost(
                R.id.index_search,
                FragmentTags.SEARCH,
                SearchFragment()
            )
            addHost(
                R.id.index_listen_now,
                FragmentTags.LISTEN_NOW,
                ListenNowFragment()
            )
        }
    }

    /**
     * 注册所有观察者
     */
    private fun registerObservers() = binding.apply {
        RemoteManager.isConnected.observe(this@MainActivity) {
            if (it)
                PlayerApi.startListen()
            connectButton.visibility = if (it) View.INVISIBLE else View.VISIBLE
        }

        PlayerApi.coverUrl.observe(this@MainActivity) {
            loadLargeImage(it) { bitmap ->
                if (playerBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
                    adjustCoverByOffset(0F) // 让图片立即复位

                loadRadiusBitmap(bitmap, coverImageView, COVER_RADIUS)
                toBlurDrawable(bitmap) { blurDrawable ->
                    val transitionDrawable =
                        TransitionDrawable(arrayOf(player.background, blurDrawable))
                    player.background = null
                    player.background = transitionDrawable
                    transitionDrawable.startTransition(450)
                }
            }
        }

        PlayerApi.isPaused.observe(this@MainActivity) { isPaused -> // 暂停图片切换
            val res = if (isPaused) R.drawable.ic_play else R.drawable.ic_pause
            playButton.setImageResource(res)
            miniPlayerRoot.binding.miniPlayButton.setImageResource(res)
        }

        PlayerApi.isLoading.observe(this@MainActivity) { // seekbar 加载中
            seekbar.isLoading = it
        }

        AppTokenHelper.collectEffect(lifecycleScope) { e ->
            when (e) {
                TokenEffect.CodeNeeded -> authManager.authenticate()
                is TokenEffect.RequestError ->
                    showMDDialog("error", "${e.error.code}\n${e.error.msg}")
            }
        }

        AppTokenHelper.observeState(lifecycleScope) {
            map {
                it.isAvailable // 细化观察的属性
            }.collect { v ->
                if (v) {
//                    "web-api ok".toast()
//                    mainViewModel.test()
                }
            }
        }
    }

    private fun startSeekbarStateCheckJob() {
        seekbarJob?.cancel()
        seekbarJob = lifecycleScope.launch(Dispatchers.IO) {
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
        isEnableEdgeToEdge = true
    }

    /**
     * 调整 bottom nav, line 和 connect button 的 TranslationY
     */
    private fun adjustTranslationYs(slideOffset: Float) = binding.run {
        if (slideOffset !in 0.0F..1.0F)
            return

        // 这些参数的计算只要画个图 (slide offset 关于 translation 的图像) 即可得出
        val navY = bottomNavHeight * slideOffset * 2 // 导航栏的偏移量
        val connectButtonY = parentHeight * -(0.8F * slideOffset + 0.23F) // 大概让按钮跟随 behavior 移动

        bottomNavigation.translationY = navY
        line.translationY = navY
        connectButton.translationY = connectButtonY
    }

    /**
     * 调整 cover 的大小和位置
     *
     * 用一个 [0, 1] 区间内的数设置 cover 的大小及位置
     *
     * pivot 计算原理: 以 cover 左上角为原点, 求完全缩小和原始尺寸的 cover 的右上角、左下角坐标的交点即为 pivot 点
     */
    private fun adjustCoverByOffset(slideOffset: Float) = binding.coverImageView.run {
        if (slideOffset !in 0.0F..1.0F)
            return@run

        val coverHeight = height // cover 的宽高
        if (coverHeight == 0) return@run // 若未加载出图片则返回

        val minTopMargin =
            (miniPlayerHeight - minCoverLength) / 2F  // 最小化 cover 的顶部 margin
        val minLeftMargin = 0.1F * miniPlayerHeight // 左边 margin

        val minScale = minCoverLength.toFloat() / coverHeight // 使封面宽高到达最小的 scale 因子

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

        popFragment()
    }

    fun popFragment() {
        val pair = binding.hostView.getActiveHost()?.peek() ?: return
        when (pair.first) {
            FragmentTags.LISTEN_NOW -> twoClicksToExit()

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
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    adjustCoverByOffset(0.0F)
                    miniPlayerRoot.visibility = View.VISIBLE
                }
            }
        }

        /**
         * [0, 1] 表示介于折叠和展开状态之间, [-1, 0] 介于隐藏和折叠状态之间, 此处由于禁止 hide 所以只会取值在[0, 1]
         *
         * 此处 slideOffset 完全可以当作一个百分数来看待
         */
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (slideOffset !in 0.0F..1.0F)
                return

            if (slideOffset in 0.0F..0.005F) // 小播放器可见性
                binding.miniPlayerRoot.visibility = View.VISIBLE
            else
                binding.miniPlayerRoot.visibility = View.INVISIBLE

            binding.shadeForMiniPlayer.alpha = 1 - slideOffset * 15 // 使遮罩逐渐消失, 让背景显现
            adjustTranslationYs(slideOffset)
            adjustCoverByOffset(slideOffset)
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
            PlayerApi.run {
                val percent = mainViewModel.notedProgress / SEEKBAR_MAX
                val time = (percent * duration.value!!)
                seekTo(time)
                lifecycleScope.launch {
                    delay(170)
                    mainViewModel.allowAutoSetProgress = true // 缓冲一下, 以免闪烁
                }
            }
        }
    }
}