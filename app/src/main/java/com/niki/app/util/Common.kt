package com.niki.app.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.niki.app.R
import com.niki.app.ui.LoadingDialog
import com.niki.spotify_objs.toLogString
import com.spotify.protocol.types.ListItem
import com.zephyr.base.extension.toast
import com.zephyr.base.ui.findHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

const val PRE_LOAD_NUM = 10

var appAccess = ""
var appRefresh = ""
var appLastSet = 0L
var appLoadingDialog: LoadingDialog? = null
var vibrator: Vibrator? = null

const val LOW_BITMAP_POOL_INIT_SIZE = 120
const val BITMAP_POOL_INIT_SIZE = 60
const val ITEM_POOL_INIT_SIZE = 20

fun runOnMain(block: () -> Unit) = Handler(Looper.getMainLooper()).post(block)

fun LifecycleOwner.toastM(msg: String) {
    lifecycleScope.launch(Dispatchers.Main) { msg.toast() }
}

class ListItemCallback : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem.id == newItem.id
    }
}

enum class ContentType(str: String) {
    ALBUM("album"),
    PLAYLIST("playlist"),
    SINGLE("single"),
    ARTIST("artist"),
    UNKNOWN("known")
}

/**
 * 实际上目前仅用于 song fragment 判断是否是专辑
 */
fun String.parseSpotifyId(): ContentType {
    return when {
        this.startsWith("spotify:track:") -> ContentType.SINGLE
        this.startsWith("spotify:album:") -> ContentType.ALBUM
        this.startsWith("spotify:artist:") -> ContentType.ARTIST
        this.startsWith("spotify:playlist:") -> ContentType.PLAYLIST
        else -> ContentType.UNKNOWN
    }
}

suspend fun <T> withProgressBar(block: suspend () -> T) = withContext(Dispatchers.Main) {
    appLoadingDialog?.show()
    withTimeout(20_000) {
        withContext(Dispatchers.IO) { block() }
    }
    appLoadingDialog?.hide()
}

fun Fragment.openNewFragment(tag: String, fragment: Fragment) {
    lifecycleScope.launch(Dispatchers.Main) {
        findHost()?.pushFragment(tag, fragment, R.anim.right_enter, R.anim.fade_out)
    }
}

fun Context.showThrowableInfoDialog(throwable: Throwable) {
    MaterialAlertDialogBuilder(this)
        .setTitle(throwable.message)
        .setMessage(throwable.toLogString())
        .setCancelable(false)
        .setPositiveButton("确认") { _, _ ->
        }.create()
        .show()
}

fun Context.showItemInfoDialog(item: ListItem) {
    MaterialAlertDialogBuilder(this)
        .setTitle(item.title)
        .setMessage("${item.subtitle}\n\nid: ${item.id}\n\nuri: ${item.uri}\n\nhasChildren: ${item.hasChildren}\n\nplayable: ${item.playable}")
        .setCancelable(true)
        .setPositiveButton("确认") { _, _ ->
        }.create()
        .show()
}