package com.niki.app.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.niki.app.LOAD_BATCH_SIZE
import com.niki.app.ListItemCallback
import com.niki.app.PRE_LOAD_NUM
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class CollectionAdapter :
    ViewBindingListAdapter<ItemPlaylistCollectionBinding, ListItem>(ListItemCallback()) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val removeMutex = Mutex()
    private val pendingRemovals = mutableSetOf<ListItem>()

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
        if (data == null) {
            root.visibility = View.INVISIBLE
            return
        } else {
            root.visibility = View.VISIBLE
        }

        val h = (root.context.getRootHeight() * MARGIN_TOP_PERCENT).toInt()

        if (position == 0)
            root.setMargins(top = h)
        else
            root.setMargins(top = 0)

        val playlistAdapter = PlaylistAdapter()

        playlistAdapter.setOnClickListener(listener)

        playlistAdapter.fetchDatas(data)

        val preloadLayoutManager =
            PreloadLayoutManager(root.context, RecyclerView.HORIZONTAL, PRE_LOAD_NUM)

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
        SpotifyRemote.getChildrenOfItem(data, offset, LOAD_BATCH_SIZE) { list ->
            if (list == null) {
                return@getChildrenOfItem
            }

            if (list.isNotEmpty()) {
                offset += list.size
                submitList(list)
            } else {
                if (currentList.isEmpty()) {
                    removeItemSafely(data)
                }
                logE(TAG, "${data.title} 加载完毕")
            }
            isFetching = false
        }
    }

    private fun removeItemSafely(item: ListItem) = scope.launch {
        removeMutex.withLock {
            pendingRemovals.add(item) // 标记项
            val currentItems = currentList.toMutableList()
            val filteredItems = currentItems.filterNot { it in pendingRemovals } // 筛选未被标记项

            withContext(Dispatchers.Main) {
                submitList(filteredItems) {
                    pendingRemovals.remove(item) // 在提交成功后移除该项
                    logE(TAG, "空数据 ${item.title} 已移除")
                }
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        scope.cancel()
    }
}