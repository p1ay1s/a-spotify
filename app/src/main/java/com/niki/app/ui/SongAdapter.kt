package com.niki.app.ui

import android.view.View
import com.niki.app.ContentType
import com.niki.app.ListItemCallback
import com.niki.app.SpotifyRemote
import com.niki.app.databinding.ItemSongBinding
import com.niki.util.loadRadiusBitmap
import com.spotify.protocol.types.ListItem
import com.zephyr.base.extension.getRootHeight
import com.zephyr.base.extension.setMargins
import com.zephyr.base.extension.setSize
import com.zephyr.vbclass.ui.ViewBindingListAdapter

class SongAdapter(private val type: ContentType) :
    ViewBindingListAdapter<ItemSongBinding, ListItem>(ListItemCallback()) {

    companion object {
        private const val MARGIN_TOP_PERCENT = 0.05
    }

    interface Listener {
        fun onClicked(item: ListItem, position: Int)
        fun onLongClicked(item: ListItem)
    }

    private var listener: Listener? = null

    fun setListener(l: Listener?) {
        listener = l
    }

    override fun ItemSongBinding.onBindViewHolder(data: ListItem?, position: Int) {
        val h = (root.context.getRootHeight() * MARGIN_TOP_PERCENT).toInt()

        if (position == 0)
            root.setMargins(top = 2 * h)
        else
            root.setMargins(top = 0)

        if (data == null) {
            root.visibility = View.INVISIBLE
            return
        }
        root.visibility = View.VISIBLE

        root.run {
            setOnClickListener {
                listener?.onClicked(data, position)
            }

            setOnLongClickListener {
                listener?.onLongClicked(data)
                false // 若 true -> 还会触发 onclick
            }

            setSize(height = (0.08 * context.getRootHeight()).toInt())
        }

        if (type == ContentType.ALBUM) {
            songDetails.visibility = View.GONE
            songDetails.text = ""
            cover.visibility = View.GONE
        } else {
            songDetails.visibility = View.VISIBLE
            songDetails.text = data.subtitle
            SpotifyRemote.loadLowImage(data.imageUri.raw!!) { bitmap ->
                root.context.loadRadiusBitmap(bitmap, cover)
            }
        }

        songName.text = data.title

        more.setOnClickListener {
            listener?.onLongClicked(data)
        }
    }
}