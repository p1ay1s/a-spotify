package com.niki.app.ui

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.niki.app.Item
import com.niki.app.databinding.ItemPlaylistCollectionBinding
import com.niki.util.getRootHeight
import com.spotify.protocol.types.ListItem
import com.zephyr.base.extension.setMargins
import com.zephyr.base.extension.setSize
import com.zephyr.base.ui.PreloadLayoutManager
import com.zephyr.vbclass.ui.ViewBindingListAdapter

class ListenNowAdapter(private val onClick: (ListItem) -> Unit) :
    ViewBindingListAdapter<ItemPlaylistCollectionBinding, Item>(ListCallback()) {

    companion object {
        private const val MARGIN_TOP_PERCENT = 0.05
    }

    class ListCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.listItem.id == newItem.listItem.id
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.listItem.id == newItem.listItem.id
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    override fun ItemPlaylistCollectionBinding.onBindViewHolder(data: Item, position: Int) {
        if (position == 0)
            root.setMargins(top = (root.getRootHeight() * MARGIN_TOP_PERCENT).toInt())
        if (position == itemCount - 1) {
            root.setSize(height = (root.getRootHeight() * MARGIN_TOP_PERCENT).toInt())
            return
        }

        val playlistAdapter = PlaylistAdapter { index ->
            onClick(data.children[index])
        }
        val preloadLayoutManager = PreloadLayoutManager(root.context, RecyclerView.HORIZONTAL)

        title.text = data.listItem.title
        recyclerView.run {
            adapter = playlistAdapter
            layoutManager = preloadLayoutManager
        }

        playlistAdapter.submitList(data.children)
    }
}