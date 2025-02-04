package com.niki.app

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import com.niki.app.util.cache_pool.BitmapCachePool
import com.niki.app.util.cache_pool.ListItemCachePool
import com.niki.app.util.cache_pool.LowBitmapCachePool
import com.niki.app.util.vibrator
import com.niki.spotify.remote.RemoteManager
import com.zephyr.base.appBaseUrl
import com.zephyr.base.extension.toast
import com.zephyr.base.log.ERROR
import com.zephyr.base.log.Logger
import com.zephyr.base.log.logE

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Logger.startLogger(this, ERROR)

        com.niki.spotify.remote.RemoteManager.connectSpotify()

        appBaseUrl = "https://accounts.spotify.com/"
        // appBaseUrl = "https://api.spotify.com/v1/"

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        registerComponentCallbacks(object : ComponentCallbacks2 {
            override fun onConfigurationChanged(newConfig: Configuration) {
            }


            override fun onLowMemory() {
            }

            override fun onTrimMemory(level: Int) {
                // 精细化内存压力分级处理（仅你的应用可见）
                when (level) {
                    TRIM_MEMORY_COMPLETE -> handleMemoryPressure(Level.CRITICAL) // 如果未很快找到更多内存, app 将被终止
                    TRIM_MEMORY_MODERATE -> handleMemoryPressure(Level.MODERATE) // 释放内存可以帮助系统保持其他进程在列表中的后面运行以获得更好的整体性能
                    TRIM_MEMORY_UI_HIDDEN -> handleMemoryPressure(Level.LOW) // 此时应释放 UI 的大额分配以便更好地管理内存
                    else -> Unit
                }
            }

        })
    }

    private fun handleMemoryPressure(level: Level) {
        // 根据压力等级调整缓存
        when (level) {
            Level.LOW -> {
                logE("App", "内存 - 应少占用")
                "内存 - 应少占用".toast()
                LowBitmapCachePool.trim(60)
                BitmapCachePool.trim(50)
                ListItemCachePool.trim(70)
            }

            Level.MODERATE -> {
                logE("App", "内存 - 需要释放")
                "内存 - 需要释放".toast()
                LowBitmapCachePool.trimTo(40)
                BitmapCachePool.trimTo(20)
                ListItemCachePool.trim(10)
            }

            Level.CRITICAL -> {
                logE("App", "内存 - 严重不足")
                "内存 - 严重不足".toast()
                LowBitmapCachePool.clear()
                BitmapCachePool.clear()
                ListItemCachePool.clear()
            }
        }
    }

    enum class Level { LOW, MODERATE, CRITICAL }
}