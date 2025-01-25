package com.niki.app.ui

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.niki.app.Item
import com.niki.app.databinding.ItemPlaylistCollectionBinding
import com.spotify.protocol.types.ListItem
import com.zephyr.base.extension.addOnLoadMoreListener_H
import com.zephyr.base.extension.getRootHeight
import com.zephyr.base.extension.setMargins
import com.zephyr.base.extension.toast
import com.zephyr.base.ui.PreloadLayoutManager
import com.zephyr.vbclass.ui.ViewBindingListAdapter

class CollectionAdapter(private val onClick: (ListItem) -> Unit) :
    ViewBindingListAdapter<ItemPlaylistCollectionBinding, Item?>(ListCallback()) {

    companion object {
        private const val MARGIN_TOP_PERCENT = 0.05
    }

    class ListCallback : DiffUtil.ItemCallback<Item?>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.listItem.id == newItem.listItem.id
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.listItem.id == newItem.listItem.id
        }
    }

    private var page = 0

    override fun submitList(list: List<Item?>?) {
        val l = list?.toMutableList()
        l?.add(null) // 插入一条空数据使得最后的有效 item 不会被挡住
        super.submitList(l?.toList())
    }

    override fun ItemPlaylistCollectionBinding.onBindViewHolder(data: Item?, position: Int) {
        val h = (root.context.getRootHeight() * MARGIN_TOP_PERCENT).toInt()

        if (position == 0)
            root.setMargins(top = h)
        else
            root.setMargins(top = 0)

        if (data == null) {
            title.text = ""
            recyclerView.adapter = null
            recyclerView.layoutManager = null // 避免复用

            return
        }

        val playlistAdapter = PlaylistAdapter { index ->
            onClick(data.children[index])
        }
        val preloadLayoutManager = PreloadLayoutManager(root.context, RecyclerView.HORIZONTAL)

        title.setMargins(top = h)
        title.text = data.listItem.title
        recyclerView.run {
            adapter = playlistAdapter
            layoutManager = preloadLayoutManager
            addOnLoadMoreListener_H(1) {
                "load more".toast()
            }
        }

        playlistAdapter.submitList(data.children)
    }
}