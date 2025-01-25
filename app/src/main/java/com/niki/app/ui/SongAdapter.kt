package com.niki.app.ui

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import com.niki.app.SpotifyRemote
import com.niki.app.databinding.ItemSongBinding
import com.niki.util.loadRadiusBitmap
import com.spotify.protocol.types.ListItem
import com.zephyr.base.extension.getRootHeight
import com.zephyr.base.extension.setMargins
import com.zephyr.base.extension.setSize
import com.zephyr.vbclass.ui.ViewBindingListAdapter

class SongAdapter @JvmOverloads constructor(
    private val showDetails: Boolean = true,
    private val showImage: Boolean = true
) : ViewBindingListAdapter<ItemSongBinding, ListItem>(StrCallback()) {

    companion object {
        private const val MARGIN_TOP_PERCENT = 0.05
    }

    interface SongAdapterListener {
        fun onPlayMusic(item: ListItem, position: Int)
        fun onMoreClicked(song: ListItem)
    }

    private var listener: SongAdapterListener? = null

    fun setSongAdapterListener(l: SongAdapterListener?) {
        listener = l
    }

    class StrCallback : DiffUtil.ItemCallback<ListItem>() {
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem == newItem
        }
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
                listener?.onPlayMusic(data, position)
            }

            setOnLongClickListener {
                listener?.onMoreClicked(data)
                false // 若 true -> 还会触发 onclick
            }

            setSize(height = (0.08 * context.getRootHeight()).toInt())
        }

        if (showImage) {
            SpotifyRemote.loadLowImage(data.imageUri.raw!!) { bitmap ->
                root.context.loadRadiusBitmap(bitmap, cover)
            }
        } else
            cover.visibility = View.GONE

        songName.text = data.title

        if (showDetails)
            songDetails.text = data.subtitle

        more.setOnClickListener {
            listener?.onMoreClicked(data)
        }
    }
}