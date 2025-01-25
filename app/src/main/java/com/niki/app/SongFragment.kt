package com.niki.app

import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.niki.app.databinding.FragmentListItemBinding
import com.niki.app.ui.SongAdapter
import com.spotify.protocol.types.ListItem
import com.zephyr.base.extension.TAG
import com.zephyr.base.extension.addLineDecoration
import com.zephyr.base.extension.addOnLoadMoreListener_V
import com.zephyr.base.extension.toast
import com.zephyr.base.log.logE
import com.zephyr.base.ui.PreloadLayoutManager
import com.zephyr.vbclass.ViewBindingFragment

class SongFragment(val item: ListItem, var callback: (SongFragment) -> Unit) :
    ViewBindingFragment<FragmentListItemBinding>() {

    private var items: List<ListItem> = emptyList()
    private lateinit var songAdapter: SongAdapter

    private var isFetching = false

    private var page = 0

    init {
        fetchDatas {
            if (it)
                callback(this)
            else
                "获取失败".toast()
        }
    }

    inner class SongAdapterListenerImpl : SongAdapter.SongAdapterListener {
        override fun onPlayMusic(item: ListItem, position: Int) {
            vibrator?.vibrate(25L)
            if (item.hasChildren)
                openNewListItemFragment(item)
            else
                SpotifyRemote.playPlaylistWithIndex(item, position)
        }

        override fun onMoreClicked(song: ListItem) {
            vibrator?.vibrate(25L)
            MaterialAlertDialogBuilder(requireActivity())
                .setTitle(song.title)
                .setMessage("${song.subtitle}\nchildren: ${song.hasChildren}\nid: ${song.id}\nplayable: ${song.playable}")
                .setCancelable(true)
                .setPositiveButton("确认") { _, _ ->
                }.create()
                .show()
        }

    }

    override fun FragmentListItemBinding.initBinding() {
        songAdapter = SongAdapter()
        songAdapter.setSongAdapterListener(SongAdapterListenerImpl())

        recyclerView.run {
            adapter = songAdapter
            layoutManager = PreloadLayoutManager(requireActivity(), RecyclerView.VERTICAL)
            addLineDecoration(requireActivity(), LinearLayout.VERTICAL)
            addOnLoadMoreListener_V(1) {
                fetchDatas {
                    songAdapter.submitList(items)
                }
            }
        }

//        setupItemTouchHelper(songAdapter, recyclerView)

        if (items.isEmpty()) {
            fetchDatas {
                if (it)
                    songAdapter.submitList(items)
            }
        } else {
            songAdapter.submitList(items)
        }
    }

    private fun fetchDatas(callback: (Boolean) -> Unit) {
        if (isFetching) return
        isFetching = true

        // 首先尝试从缓存中读取
        val cachedItems = ItemCachePool.fetch(item.id)
        if (!cachedItems.isNullOrEmpty() && cachedItems.size > items.size) {
            logE(TAG, "${item.id} 成功读取了缓存")
            items = cachedItems.toMutableList()
            callback(true)
            isFetching = false
            return
        }

        // 如果缓存中没有，则从网络获取
        SpotifyRemote.getChildOfItem(item, page, LOAD_COUNTS_PER_TIME) {
            if (it.isNotEmpty()) {
                page++
                items = items + it

                // 将获取的数据缓存起来
                ItemCachePool.cache(item.id, items.toMutableList())

                callback(true)
            } else {
                callback(false)
            }
            isFetching = false
        }
    }
}