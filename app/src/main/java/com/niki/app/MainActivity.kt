package com.niki.app

import android.annotation.SuppressLint
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.view.View
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.niki.app.databinding.ActivityMainBinding
import com.niki.app.listen_now.ListenNowFragment
import com.niki.app.ui.LoadingDialog
import com.niki.app.util.CLIENT_ID
import com.niki.app.util.Fragments
import com.niki.app.util.REDIRECT_URI
import com.niki.app.util.appLoadingDialog
import com.niki.spotify_objs.PlayerApi
import com.niki.spotify_objs.RemoteManager
import com.niki.util.Point
import com.niki.util.getIntersectionPoint
import com.niki.util.loadRadiusBitmap
import com.niki.util.toBlurDrawable
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.zephyr.base.extension.getScreenHeight
import com.zephyr.base.extension.getScreenWidth
import com.zephyr.base.extension.hideStatusBar
import com.zephyr.base.extension.setMargins
import com.zephyr.base.extension.setSize
import com.zephyr.base.extension.showStatusBar
import com.zephyr.base.extension.toast
import com.zephyr.vbclass.ViewBindingActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
class MainActivity : ViewBindingActivity<ActivityMainBinding>() {

    companion object {
        private const val RADIUS = 35

        private const val SEEKBAR_SCALE = 15.0 // 进度条的细腻程度, 越大越细腻

        const val SEEKBAR_MAX = SEEKBAR_SCALE * 17

        private const val BOTTOM_NAV_WEIGHT = 0.115
        private const val MINI_PLAYER_WEIGHT = 0.08

        private const val MINI_COVER_SIZE = 0.8F // 占 mini player 高度的百分比

        private const val COVER_SCALE_K = -1.3F

        const val TWO_PRESSES_TO_EXIT_APP_TIME = 3000
    }

    private val playerBehavior
        get() = BottomSheetBehavior.from(binding.player)

    // 各种尺寸参数
    // {
    var parentHeight: Int = 0
        private set

    var parentWidth: Int = 0
        private set

    private var bottomNavHeight: Int = 0
    private var miniPlayerHeight: Int = 0
    private var minCoverHeight: Int = 0
    // }

    private val netViewModel by lazy { ViewModelProvider(this)[NetViewModel::class.java] }
    private val mainViewModel by lazy { ViewModelProvider(this)[MainViewModel::class.java] }

    // spotify app 授权的 activity result launcher
    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val response = AuthorizationClient.getResponse(result.resultCode, result.data)
        response.run {
//            暂时不用 web api
//            when (type) {
//                AuthorizationResponse.Type.CODE ->
//                    netViewModel.getTokensWithCode(code)
//
//                AuthorizationResponse.Type.TOKEN ->
//                    netViewModel.putTokens(accessToken, "")
//
//                else -> "授权失败: ${type.name}".toast()
//            }
            mainViewModel.authClintIsRunning = false
        }
    }

    override fun ActivityMainBinding.initBinding() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            useFullScreen()

        appLoadingDialog = LoadingDialog(this@MainActivity)

        setSizes()

        playerApi = PlayerApi
        lifecycleOwner = this@MainActivity

        seekbar.setOnSeekBarChangeListener(OnSeekListenerImpl())

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

        seekbar.max = SEEKBAR_MAX.toInt()

        floatButton.setOnClickListener {
            authenticateSpotify()
        }

        playerBehavior.apply {
            val impl = BottomSheetCallbackImpl()
            addBottomSheetCallback(impl)
            impl.onSlide(player, 0.0F) // 手动复位
            player.setOnClickListener {
                if (this.state != BottomSheetBehavior.STATE_EXPANDED)
                    this.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        RemoteManager.isConnected.observe(this@MainActivity) {
            if (it)
                PlayerApi.startListen()
            floatButton.visibility = if (it) View.INVISIBLE else View.VISIBLE
        }

        // 图片加载
        PlayerApi.run {
            coverUrl.observe(this@MainActivity) {
                loadLargeImage(it) { bitmap ->
                    checkAndResetCover()
                    loadRadiusBitmap(bitmap, cover, RADIUS)
                    toBlurDrawable(bitmap) { blurDrawable ->
                        val transitionDrawable =
                            TransitionDrawable(arrayOf(player.background, blurDrawable))
                        player.background = null
                        player.background = transitionDrawable
                        transitionDrawable.startTransition(450)
                    }
                }
            }

            // 暂停图片切换
            isPaused.observe(this@MainActivity) { isPaused ->
                val res = if (isPaused) R.drawable.ic_play else R.drawable.ic_pause
                play.setImageResource(res)
                miniPlay.setImageResource(res)
            }

            // seekbar 加载中
            isLoading.observe(this@MainActivity) {
                seekbar.isLoading = it
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            while (true) {
                if (mainViewModel.allowAutoSetProgress) {
                    val progress = getSeekBarProgress()
//                    withContext(Dispatchers.Main) { TODO 试试
                    seekbar.progress = progress
//                    }
                }
                delay(50)
            }
        }

//        netViewModel.run { // 其实逻辑可能有点问题
//            startWatchTokenDateJob()
//            checkTokens { result ->
//                if (result == NetViewModel.TOKEN_FAILED)
//                    authenticateSpotify()
//            }
//        }
    }

    /**
     * 由于根部局为 CoordinatorLayout 许多 view 的大小难以在 xml 中设置所以统一用代码实现
     */
    private fun ActivityMainBinding.setSizes() {
        parentHeight = getScreenHeight()
        parentWidth = getScreenWidth()

        bottomNavHeight = (parentHeight * BOTTOM_NAV_WEIGHT).toInt()
        miniPlayerHeight = (parentHeight * MINI_PLAYER_WEIGHT).toInt()
        minCoverHeight = (miniPlayerHeight * MINI_COVER_SIZE).toInt()
        val hostViewHeight = parentHeight - bottomNavHeight - miniPlayerHeight

        hostView.setSize(height = hostViewHeight)
        bottomNavigation.setSize(height = bottomNavHeight)

        cover.setSize((0.7 * parentWidth).toInt())
        cover.setMargins(top = (0.17 * parentHeight).toInt())
        songName.setMargins(top = (0.02 * parentHeight).toInt())
        seekbar.setMargins(top = (0.02 * parentHeight).toInt())
        play.setMargins(top = (0.1 * parentHeight).toInt())

        line.setMargins(bottom = bottomNavHeight)
        floatButton.setMargins(end = (0.08 * parentWidth).toInt())

        playerBehavior.isHideable = false
        playerBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        playerBehavior.peekHeight = bottomNavHeight + miniPlayerHeight

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

    private fun useFullScreen() {
        enableEdgeToEdge()
        // 应用全屏时, 用户仍然可以从屏幕顶部下拉唤出状态栏, 此行代码实现当用户唤出状态栏后, 自动隐藏状态栏
        WindowCompat.getInsetsController(window, window.decorView).systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    /**
     * 拉起 spotify 客户端进行授权
     */
    private fun authenticateSpotify() {
        if (mainViewModel.authClintIsRunning) return
        mainViewModel.authClintIsRunning = true

        val builder = AuthorizationRequest.Builder(
            CLIENT_ID,
            AuthorizationResponse.Type.CODE, // TOKEN
            REDIRECT_URI
        )

        builder.setScopes(
            arrayOf(
                "user-read-playback-state",
                "user-modify-playback-state",
                "user-read-currently-playing",
                "streaming"
            )
        )

        val request = builder.build()
        val intent = AuthorizationClient.createLoginActivityIntent(this, request)
        activityResultLauncher.launch(intent)
    }

    /**
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

                    BottomSheetBehavior.STATE_EXPANDED -> hideStatusBar()

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
            showStatusBar()
            if (slideOffset in 0.0F..1.0F) {
                binding.run {
                    shade.alpha = 1 - slideOffset * 15 // 使遮罩逐渐消失, 让背景显现
                    val navY = bottomNavHeight * slideOffset * 2 // 导航栏的偏移量
                    val floatBtnY =
                        parentHeight * -(0.8F * slideOffset + 0.23F) // 大概让按钮跟随 behavior 移动

                    bottomNavigation.translationY = navY
                    line.translationY = navY

                    floatButton.translationY = floatBtnY
                }
            }

            adjustCover(slideOffset)
        }
    }

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
                val time = (percent * duration.value!!).toLong()
                seekTo(time)
                lifecycleScope.launch {
                    delay(100)
                    mainViewModel.allowAutoSetProgress = true // 缓冲一下, 以免闪烁
                }
            }
        }
    }

    /**
     * 用一个 [0, 1] 区间内的数设置 cover 的大小及位置
     *
     * pivot 计算原理: 以 cover 左上角为原点, 求完全缩小和原始尺寸的 cover 的右上角、左下角坐标的交点即为 pivot 点
     */
    private fun adjustCover(slideOffset: Float) = binding.cover.run {
        val coverHeight = height // cover 的宽高
        if (coverHeight == 0) return@run // 若未加载出图片则返回

        val minTopMargin = (miniPlayerHeight - minCoverHeight) / 2F  // 最小化 cover 的顶部 margin
        val minLeftMargin = 0.1F * miniPlayerHeight // 左边 margin

        val minScale = minCoverHeight.toFloat() / coverHeight // 使封面宽高到达最小的 scale 因子

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

    private fun checkAndResetCover() {
        if (playerBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
            adjustCover(0F) // 让图片立即复位
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

            else -> binding.hostView.getActiveHost()?.popFragment(
                R.anim.fade_in,
                R.anim.right_exit
            )
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
}