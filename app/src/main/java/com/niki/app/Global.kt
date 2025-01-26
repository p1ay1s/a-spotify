package com.niki.app

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.niki.app.ui.LoadingDialog
import com.spotify.protocol.types.ImageUri
import com.spotify.protocol.types.ListItem
import com.zephyr.base.extension.TAG
import com.zephyr.base.extension.toast
import com.zephyr.base.log.logE
import com.zephyr.base.ui.findHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Semaphore

const val LOAD_BATCH_SIZE = 20
const val PRE_LOAD_NUM = 10

const val CLIENT_ID = "729ad520a3964dc3b020c0db30bfccb7"
const val CLIENT_SECRET = "31a0f20ea9bd42418b973a83b83a2c7f"
const val REDIRECT_URI = "https://open.spotify.com/"

var appAccess = ""
var appRefresh = ""
var appLastSet = 0L
var appLoadingDialog: LoadingDialog? = null

const val NO_CHILD_SIGNAL = "NO_CHILD_SIGNAL"

fun LifecycleOwner.toastM(msg: String) {
    lifecycleScope.launch(Dispatchers.Main) { msg.toast() }
}

suspend fun <T> MutableLiveData<T?>.checkAndSet(value: T?) =
    withContext(Dispatchers.Main) {
        if (value != this@checkAndSet.value && value != null)
            this@checkAndSet.value = value
    }

val noChildListItem: ListItem
    get() = ListItem(
        NO_CHILD_SIGNAL,
        NO_CHILD_SIGNAL,
        ImageUri(NO_CHILD_SIGNAL),
        NO_CHILD_SIGNAL,
        NO_CHILD_SIGNAL,
        true,
        false
    )

class ListItemCallback : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem.id == newItem.id
    }
}

enum class ContentType(name: String) {
    ALBUM("album"),
    PLAYLIST("playlist"),
    SINGLE("single"),
    ARTIST("artist"),
    UNKNOWN("known")
}

fun String.parseSpotifyId(): ContentType {
    return when {
        this.startsWith("spotify:track:") -> ContentType.SINGLE
        this.startsWith("spotify:album:") -> ContentType.ALBUM
        this.startsWith("spotify:artist:") -> ContentType.ARTIST
        this.startsWith("spotify:playlist:") -> ContentType.PLAYLIST
        else -> ContentType.UNKNOWN
    }
}

fun Throwable.log(tag: String) {
    logE(tag, "${message}\n${cause}\n${stackTraceToString()}")
}

suspend fun Semaphore.withPermit(block: suspend () -> Unit) = withContext(Dispatchers.IO) {
    acquire() // 获取信号量
    logE("withPermit", "获取到信号量")
    try {
        async {
            block() // 执行操作
        }.await()
    } finally {
        release() // 确保释放信号量
        logE("withPermit", "释放信号量")
    }
}

fun Fragment.openNewListItemFragment(item: ListItem, callback: (Boolean) -> Unit) {
    appLoadingDialog?.show()
    SongFragment(item, object : SongFragment.Listener {
        override fun onFetched(fragment: SongFragment) {
            appLoadingDialog?.dismiss()
            openNewFragment(item.id, fragment)
            lifecycleScope.launch(Dispatchers.Main) {
                delay(500) // 避免同时打开几个
                callback(true)
            }
        }

        override fun onError(e: Exception) {
            appLoadingDialog?.dismiss()
            e.log(TAG)
            if (e.message != SongFragment.ERROR_MSG)
                requireActivity().showThrowableInfo(e)
            callback(false)
        }
    })
}

fun Fragment.openNewFragment(tag: String, fragment: Fragment) {
    lifecycleScope.launch(Dispatchers.Main) {
        findHost()?.pushFragment(tag, fragment, R.anim.right_enter, R.anim.fade_out)
    }
}

fun Fragment.openNewFragment(tag: String, clazz: Class<out Fragment>) {
    openNewFragment(tag, clazz.getDeclaredConstructor().newInstance())
}

fun Context.showThrowableInfo(throwable: Throwable) {
    MaterialAlertDialogBuilder(this)
        .setTitle(throwable.message)
        .setMessage("${throwable.cause}\n${throwable.stackTraceToString()}")
        .setCancelable(false)
        .setPositiveButton("确认") { _, _ ->
        }.create()
        .show()
}

fun Context.showItemInfo(item: ListItem) {
    MaterialAlertDialogBuilder(this)
        .setTitle(item.title)
        .setMessage("${item.subtitle}\nchildren: ${item.hasChildren}\nid: ${item.id}\nplayable: ${item.playable}")
        .setCancelable(true)
        .setPositiveButton("确认") { _, _ ->
        }.create()
        .show()
}