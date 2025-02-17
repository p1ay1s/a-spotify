package com.niki.app.song.ui

import android.view.View
import com.niki.app.ContentType
import com.niki.app.ListItemCallback
import com.niki.app.databinding.ItemSongBinding
import com.niki.app.interfaces.OnClickListener
import com.niki.app.util.loadSmallImage
import com.niki.util.loadRadiusBitmap
import com.spotify.protocol.types.ListItem
import com.zephyr.base.extension.getRootHeight
import com.zephyr.base.extension.setMargins
import com.zephyr.base.extension.setSize
import com.zephyr.vbclass.ui.ViewBindingListAdapter

class SongAdapter(private val type: ContentType) :
    ViewBindingListAdapter<ItemSongBinding, ListItem>(ListItemCallback()) {

    lateinit var parentItem: ListItem
    private var listener: OnClickListener? = null
    var firstItemMarginTop = 0
//        set(value) {
//            field = value
//            notifyItemChanged(0)
//        }

    fun setOnClickListener(l: OnClickListener?) {
        listener = l
    }

    override fun ItemSongBinding.onBindViewHolder(data: ListItem?, position: Int) {
        if (data == null) {
            root.visibility = View.INVISIBLE
            return
        } else {
            root.visibility = View.VISIBLE
        }

        if (position == 0)
            root.setMargins(top = firstItemMarginTop)
        else
            root.setMargins(top = 0)

        root.run {
            setOnClickListener {
                listener?.onClicked(data, position)
            }

            setOnLongClickListener {
                listener?.onLongClicked(data, parentItem)
                false // 若 true -> 还会触发 onclick
            }

            setSize(height = (0.08 * context.getRootHeight()).toInt())
        }

        if (type == ContentType.ALBUM) {
            songDetails.visibility = View.GONE
            songDetails.text = ""
            coverImageView.visibility = View.GONE
        } else {
            songDetails.visibility = View.VISIBLE
            songDetails.text = data.subtitle
            loadSmallImage(data.imageUri.raw!!) { bitmap ->
                root.context.loadRadiusBitmap(bitmap, coverImageView)
            }
        }

        trackName.text = data.title

        more.setOnClickListener {
            listener?.onLongClicked(data, parentItem)
        }
    }
}