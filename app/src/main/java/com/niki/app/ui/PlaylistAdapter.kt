package com.niki.app.ui

import androidx.core.view.marginEnd
import androidx.recyclerview.widget.DiffUtil
import com.niki.app.SpotifyRemote
import com.niki.app.databinding.ItemPlaylistBinding
import com.niki.util.loadRadiusBitmap
import com.spotify.protocol.types.ListItem
import com.zephyr.base.extension.getRootWidth
import com.zephyr.base.extension.setMargins
import com.zephyr.base.extension.setSize
import com.zephyr.vbclass.ui.ViewBindingListAdapter

class PlaylistAdapter(private val onClick: (Int) -> Unit) :
    ViewBindingListAdapter<ItemPlaylistBinding, ListItem>(StrCallback()) {
    companion object {
        private const val WIDTH_PERCENT = 0.4

        private const val RADIUS = 40
    }

    class StrCallback : DiffUtil.ItemCallback<ListItem>() {
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem == newItem
        }
    }

    override fun ItemPlaylistBinding.onBindViewHolder(data: ListItem?, position: Int) {
        val rootWidth = (root.context.getRootWidth() * WIDTH_PERCENT).toInt()

        root.setSize(width = rootWidth)
        cover.setSize(rootWidth)

        if (position == 0) {
            root.setMargins(start = root.marginEnd * 2)
        }

        if (data == null)
            return

        data.imageUri.raw?.let {
            SpotifyRemote.loadImage(it) { bitmap ->
                root.context.loadRadiusBitmap(bitmap, cover, RADIUS)
            }
        }

        name.text = data.title
        root.setOnClickListener {
            onClick(position)
        }
    }
}