package com.niki.app.ui

import androidx.recyclerview.widget.RecyclerView
import com.niki.app.LOAD_BATCH_SIZE
import com.niki.app.ListItemCallback
import com.niki.app.SpotifyRemote
import com.niki.app.databinding.ItemPlaylistCollectionBinding
import com.niki.app.interfaces.OnClickListener
import com.spotify.protocol.types.ListItem
import com.zephyr.base.extension.addOnLoadMoreListener_H
import com.zephyr.base.extension.getRootHeight
import com.zephyr.base.extension.setMargins
import com.zephyr.base.log.logE
import com.zephyr.base.ui.PreloadLayoutManager
import com.zephyr.vbclass.ui.ViewBindingListAdapter

class CollectionAdapter :
    ViewBindingListAdapter<ItemPlaylistCollectionBinding, ListItem>(ListItemCallback()) {

    private val TAG = this::class.java.simpleName

    private var listener: OnClickListener? = null

    fun setOnClickListener(l: OnClickListener?) {
        listener = l
    }

    companion object {
        private const val MARGIN_TOP_PERCENT = 0.05
    }

    override fun submitList(list: List<ListItem?>?) {
        val l = list?.toMutableList()
        l?.add(null) // 插入一条空数据使得最后的有效 item 不会被挡住
        super.submitList(l?.toList())
    }

    override fun ItemPlaylistCollectionBinding.onBindViewHolder(data: ListItem?, position: Int) {
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


        val playlistAdapter = PlaylistAdapter()

        playlistAdapter.setOnClickListener(listener)

        playlistAdapter.fetchDatas(data)


        val preloadLayoutManager = PreloadLayoutManager(root.context, RecyclerView.HORIZONTAL)

        title.setMargins(top = h)
        title.text = data.title
        recyclerView.run {
            adapter = playlistAdapter
            layoutManager = preloadLayoutManager
            addOnLoadMoreListener_H(1) {
                playlistAdapter.fetchDatas(data)
            }
        }
    }

    private fun PlaylistAdapter.fetchDatas(data: ListItem?) {
        if (isFetching || data == null)
            return
        isFetching = true
        SpotifyRemote.getChildOfItem(data, offset, LOAD_BATCH_SIZE) { list ->
            if (list.isNotEmpty()) {
                offset += list.size
                submitList(list)
            } else {
                logE(TAG, "${data.title} 加载完")
            }
            isFetching = false
        }
    }
}