package com.niki.app.ui

import android.view.View
import androidx.core.view.marginEnd
import com.niki.app.ContentType
import com.niki.app.ListItemCallback
import com.niki.app.SpotifyRemote
import com.niki.app.databinding.ItemPlaylistBinding
import com.niki.app.interfaces.OnClickListener
import com.niki.app.parseSpotifyId
import com.niki.util.loadRadiusBitmap
import com.spotify.protocol.types.ListItem
import com.zephyr.base.extension.getRootWidth
import com.zephyr.base.extension.setMargins
import com.zephyr.base.extension.setSize
import com.zephyr.vbclass.ui.ViewBindingListAdapter

class PlaylistAdapter : ViewBindingListAdapter<ItemPlaylistBinding, ListItem>(ListItemCallback()) {

    var isFetching = false
    var offset = 0

    private var listener: OnClickListener? = null

    fun setOnClickListener(l: OnClickListener?) {
        listener = l
    }

    companion object {
        private const val WIDTH_PERCENT = 0.4

        private const val RADIUS = 33
    }

    override fun ItemPlaylistBinding.onBindViewHolder(data: ListItem?, position: Int) {
        if (data == null) {
            root.visibility = View.INVISIBLE
            return
        } else {
            root.visibility = View.VISIBLE
        }

        val w = (root.context.getRootWidth() * WIDTH_PERCENT).toInt()

        root.setSize(width = w)
        cover.setSize(w)

        if (position == 0) {
            root.setMargins(start = root.marginEnd * 2)
        }

        val type = data.id.parseSpotifyId()

        data.imageUri.raw?.let {
            SpotifyRemote.loadLargeImage(it) { bitmap ->
                val radius = if (type == ContentType.ARTIST) Int.MAX_VALUE else RADIUS
                root.context.loadRadiusBitmap(bitmap, cover, radius)
            }
        }

        name.text = when {
            type == ContentType.PLAYLIST && data.subtitle.isNotBlank() -> data.subtitle
            type == ContentType.ALBUM -> data.title + "\n" + data.subtitle
            else -> data.title
        }

        root.setOnClickListener {
            listener?.onClicked(data)
        }

        root.setOnLongClickListener {
            listener?.onLongClicked(data)
            false
        }

        SpotifyRemote.preCacheChildren(data)
    }
}