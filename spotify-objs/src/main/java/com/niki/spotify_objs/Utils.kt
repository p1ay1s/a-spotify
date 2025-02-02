package com.niki.spotify_objs

import androidx.lifecycle.MutableLiveData
import com.niki.util.checkAndSetS
import com.spotify.protocol.client.CallResult
import com.spotify.protocol.error.SpotifyAppRemoteException
import com.spotify.protocol.types.ImageUri
import com.spotify.protocol.types.ListItem
import com.spotify.protocol.types.ListItems
import com.spotify.protocol.types.PlayerState
import com.zephyr.base.log.logE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

val spotifyScope by lazy { CoroutineScope(Dispatchers.IO) }


fun isOnMain(): Boolean {
    return Thread.currentThread().name.lowercase() == "main"
}

/**
 * 若在主线程执行则抛出异常
 */
fun doNotRunThisOnMain() {
    if (isOnMain())
        throw Exception("don't run this on Main")
}


fun logS(msg: String) {
    logE(SPOTIFY_REMOTE_TAG, msg)
}

fun Throwable.log(tag: String = "") {
    logE(tag, toLogString())
}

fun Throwable.logS() {
    log(SPOTIFY_REMOTE_TAG)
}

fun Throwable.toLogString(): String {
    return "${message}\n${cause}\n${stackTraceToString()}"
}

fun Throwable.isSpotifyError(): Boolean {
    return (message in SPOTIFY_ERROR_MESSAGES || this is SpotifyAppRemoteException)
}

/**
 * 自定参数
 */
fun ListItem.hasChild(): Boolean = when {
    !id.startsWith("spotify:") -> false
    id.startsWith("spotify:track:") -> false
    !playable || hasChildren -> true
    else -> true
}

/**
 * 许多 api 对 list item 处理都是直接拿出 id, 所以做这个方法
 */
fun createListItem(id: String): ListItem {
    return ListItem(id, "", ImageUri(""), "", "", true, true)
}


/**
 * 同步获取数据
 */
fun <T> CallResult<T>.get(timeout: Long? = null): T? {
    val await = if (timeout == null || timeout <= 0)
        await()
    else
        await(timeout, TimeUnit.MILLISECONDS)

    if (!await.isSuccessful) {
        val error = await.error
        if (error.isSpotifyError())
            logS("spotify error")
        error.logS()
    }

    return await.data
}

/**
 * 异步获取数据
 */
fun <T> CallResult<T>.get(callback: (T?) -> Unit) {
    var isCalled = false
    setResultCallback {
        if (isCalled) return@setResultCallback
        isCalled = true
        callback(it)
    }
    setErrorCallback {
        if (isCalled) return@setErrorCallback
        isCalled = true
        if (it.isSpotifyError())
            logS("spotify error")
        it.logS()
        callback(null)
    }
}


// 直接对 items toList 貌似不行
fun ListItems.toList(): List<ListItem> {
    val list = mutableListOf<ListItem>()
    items?.forEach {
        list.add(it)
    }
    return list
}

/**
 * 确保设置的值是非空并且不同的
 */
fun <T> MutableLiveData<T?>.checkAndSet(value: T?) = spotifyScope.launch {
    checkAndSetS(value)
}

fun PlayerState.isLoading(): Boolean {
    return (playbackSpeed <= 0 && !isPaused && playbackPosition <= 80)
}
