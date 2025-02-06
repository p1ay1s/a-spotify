package com.niki.app

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.niki.app.ui.LoadingDialog
import com.niki.app.util.cache_pool.BitmapCachePool
import com.niki.app.util.cache_pool.ListItemCachePool
import com.niki.app.util.cache_pool.LowBitmapCachePool
import com.niki.spotify.remote.RemoteManager
import com.niki.spotify.remote.logS
import com.zephyr.base.log.Logger
import com.zephyr.base.log.VERBOSE
import com.zephyr.base.log.logE
import kotlinx.coroutines.runBlocking

class App : Application() {
    companion object {
        @Volatile
        var accessToken = ""
            set(value) {
                field = value
                logS("{\nAuthorization\nBearer $field\n}")
            }

        @Volatile
        var refreshToken = ""

        @Volatile
        var OFDSeconds = 3600L

        @Volatile
        var lastSet = 0L

        var loadingDialog: LoadingDialog? = null
        var vibrator: Vibrator? = null

        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_OFD = "out_of_date"
        private const val KEY_LAST_TOKEN_SET = "last_token_set"

        val prefAccessToken = stringPreferencesKey(KEY_ACCESS_TOKEN)
        val prefRefreshToken = stringPreferencesKey(KEY_REFRESH_TOKEN)
        val prefOFD = longPreferencesKey(KEY_OFD)
        val prefLastTokenSet = longPreferencesKey(KEY_LAST_TOKEN_SET)
    }

    override fun onCreate() {
        super.onCreate()
        Logger.startLogger(this, VERBOSE)
        RemoteManager.connectSpotify()

//        AppTokenHelper.observeState(GlobalScope) {
//            map {
//                it.isAvailable // 细化观察的属性
//            }.collect {
//
//            }
//        }

        runBlocking {
            AppTokenHelper.loadPrefs().await()
        }
        AppTokenHelper.checkTokens()
        AppTokenHelper.startCheckJob()

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        registerMemoryPressureCallbacks()
    }


    private fun registerMemoryPressureCallbacks() {
        registerComponentCallbacks(object : ComponentCallbacks2 {
            override fun onConfigurationChanged(newConfig: Configuration) {}

            override fun onLowMemory() {
                handleMemoryPressure(Level.LOW)
            }

            // 精细化内存压力分级处理-仅本应用可见
            override fun onTrimMemory(level: Int) {

                when (level) {
                    TRIM_MEMORY_COMPLETE -> handleMemoryPressure(Level.CRITICAL) // 如果未很快找到更多内存, app 将被终止
                    TRIM_MEMORY_MODERATE -> handleMemoryPressure(Level.MODERATE) // 释放内存可以帮助系统保持其他进程在列表中的后面运行以获得更好的整体性能
                    TRIM_MEMORY_UI_HIDDEN -> handleMemoryPressure(Level.LOW) // 此时应释放 UI 的大额分配以便更好地管理内存
                    else -> Unit
                }
            }
        })
    }

    /**
     * 根据压力等级调整缓存
     */
    private fun handleMemoryPressure(level: Level) {
        when (level) {
            Level.LOW -> {
                logE("App", "内存 - 应少占用")
                LowBitmapCachePool.trim(60)
                BitmapCachePool.trim(50)
                ListItemCachePool.trim(70)
            }

            Level.MODERATE -> {
                logE("App", "内存 - 需要释放")
                LowBitmapCachePool.trimTo(40)
                BitmapCachePool.trimTo(20)
                ListItemCachePool.trim(10)
            }

            Level.CRITICAL -> {
                logE("App", "内存 - 严重不足")
                LowBitmapCachePool.clear()
                BitmapCachePool.clear()
                ListItemCachePool.clear()
            }
        }
    }

    enum class Level { LOW, MODERATE, CRITICAL }
}