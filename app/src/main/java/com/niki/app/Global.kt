package com.niki.app

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.spotify.protocol.types.ListItem
import com.zephyr.base.ui.findHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val LOAD_COUNTS_PER_TIME = 20

const val CLIENT_ID = "729ad520a3964dc3b020c0db30bfccb7"
const val CLIENT_SECRET = "31a0f20ea9bd42418b973a83b83a2c7f"
const val REDIRECT_URI = "https://open.spotify.com/"

var appAccess = ""
var appRefresh = ""
var appLastSet = 0L

fun Fragment.openNewListItemFragment(item: ListItem) {
    SongFragment(item) {
        openNewFragment(item.id, it)
    }
}

fun Fragment.openNewFragment(tag: String, fragment: Fragment) {
    lifecycleScope.launch(Dispatchers.Main) {
        findHost()?.pushFragment(tag, fragment, R.anim.right_enter, R.anim.fade_out)
    }
}

fun Fragment.openNewFragment(tag: String, clazz: Class<out Fragment>) {
    openNewFragment(tag, clazz.getDeclaredConstructor().newInstance())
}
