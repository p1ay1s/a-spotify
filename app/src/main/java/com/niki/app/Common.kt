package com.niki.app

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.niki.app.ui.LoadingDialog
import com.niki.spotify.remote.logS
import com.niki.spotify.remote.toLogString
import com.spotify.protocol.types.ListItem
import com.zephyr.base.extension.toast
import com.zephyr.base.ui.findHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val PRE_LOAD_NUM = 10

//@Volatile
//var appAccess = ""
//    set(value) {
//        field = value
//        logS(" \n\nAuthorization\nBearer $field\n\n ")
//    }
//
//@Volatile
//var appRefresh = ""
//
//@Volatile
//var appOFD = 3600L
//
//@Volatile
//var appLastSet = 0L
//
//var appLoadingDialog: LoadingDialog? = null
//var vibrator: Vibrator? = null

const val LOW_BITMAP_POOL_INIT_SIZE = 120
const val BITMAP_POOL_INIT_SIZE = 60
const val ITEM_POOL_INIT_SIZE = 20

fun Context.showInputDialog(
    title: String = "",
    msg: String = "",
    hint: String = "",
    onConfirmed: ((CharSequence?) -> Unit) = {},
) {

    val editText = EditText(this).also {
        it.hint = hint
    }

    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(msg)
        .setCancelable(true)
        .setView(editText)
        .setPositiveButton("确认") { _, _ ->
            onConfirmed(editText.text)
        }
//        .setNegativeButton("取消") { _, _ ->
//            onCancel()
//        }
//        .setOnCancelListener {
//            onCancel()
//        }
        .create().show()
}

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

enum class ContentType(val value: String) {
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

fun Fragment.openNewFragment(tag: String, fragment: Fragment) {
    lifecycleScope.launch(Dispatchers.Main) {
        findHost()?.pushFragment(tag, fragment, R.anim.right_enter, R.anim.fade_out)
    }
}

fun Context.showThrowableInfoDialog(throwable: Throwable) {
    showMDDialog(throwable.message ?: "", throwable.toLogString())
}

fun Context.showItemInfoDialog(item: ListItem) {
    val msg =
        ("${item.subtitle}\n\nid: ${item.id}\n\nuri: ${item.uri}\n\nhasChildren: ${item.hasChildren}\n\nplayable: ${item.playable}")
    showMDDialog(item.title, msg)
}

fun Context.showMDDialog(title: String, msg: String) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(msg)
        .setCancelable(true)
        .setPositiveButton("确认") { _, _ ->
        }.create()
        .show()
}