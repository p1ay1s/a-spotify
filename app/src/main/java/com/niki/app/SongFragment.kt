package com.niki.app

import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.niki.app.databinding.FragmentListItemBinding
import com.niki.app.ui.SongAdapter
import com.niki.util.toBlurDrawable
import com.spotify.protocol.types.ListItem
import com.zephyr.base.extension.addLineDecoration
import com.zephyr.base.extension.addOnLoadMoreListener_V
import com.zephyr.base.ui.PreloadLayoutManager
import com.zephyr.vbclass.ViewBindingFragment
import kotlinx.coroutines.launch

class SongFragment(private val item: ListItem, private var listener: Listener?) :
    ViewBindingFragment<FragmentListItemBinding>() {

    companion object {
        const val ERROR_MSG = "空数据, 无法打开 SongFragment"
    }

    interface Listener {
        fun onFetched(fragment: SongFragment)
        fun onError(e: Exception)
    }

    private var items: List<ListItem> = emptyList()
    private lateinit var songAdapter: SongAdapter
    private var isFetching = false
    private var currentOffset = 0

    init {
        initializeData()
    }

    private fun initializeData() {
        // 重置状态
        currentOffset = 0
        items = emptyList()

        fetchData { success ->
            if (success) {
                listener?.onFetched(this)
            } else {
                listener?.onError(Exception(ERROR_MSG))
            }
        }
    }

    override fun FragmentListItemBinding.initBinding() {
        if (item.id.parseSpotifyId() == ContentType.ALBUM)
            SpotifyRemote.loadLargeImage(item.imageUri.raw!!) { bitmap ->
                requireActivity().toBlurDrawable(bitmap) {
                    root.background = it
                }
            }

        songAdapter = SongAdapter(item.id.parseSpotifyId()).apply {
            setListener(object : SongAdapter.Listener {
                override fun onClicked(item: ListItem, position: Int) {
                    vibrator?.vibrate(25L)
                    if (!item.playable) {
                        toastM("playable = false")
                    } else if (!item.hasChildren) {
                        SpotifyRemote.playPlaylistWithIndex(
                            this@SongFragment.item, // 此 item 应为歌单列表 item
                            position
                        )
                    } else {
                        openNewListItemFragment(item) { success ->
                            if (!success) toastM("未知错误")
                        }
                    }
                }

                override fun onLongClicked(item: ListItem) {
                    vibrator?.vibrate(25L)
                    requireActivity().showItemInfo(item)
                }
            })
        }

        recyclerView.apply {
            adapter = songAdapter
            layoutManager = PreloadLayoutManager(
                requireActivity(), RecyclerView.VERTICAL,
                PRE_LOAD_NUM
            )
            addLineDecoration(requireActivity(), LinearLayout.VERTICAL)
            addOnLoadMoreListener_V(1) {
                fetchData { success ->
                    if (success)
                        songAdapter.submitList(items.toList())
                }
            }
        }

        loadInitialData()
    }

    private fun loadInitialData() {
        if (items.isEmpty()) {
            fetchData { success ->
                if (success) {
                    songAdapter.submitList(items.toList())
                }
            }
        } else {
            songAdapter.submitList(items.toList())
        }
    }

    private fun fetchData(callback: (Boolean) -> Unit) {
        if (isFetching) return
        isFetching = true

        // 尝试从缓存获取数据
        val cachedItems = ItemCachePool.fetch(item.id)

        if (cachedItems?.contains(getNoChildListItem) == true) {
            callback(false)
            isFetching = false
            return
        }

        if (!cachedItems.isNullOrEmpty() && cachedItems.size > items.size) {
            items = cachedItems
            currentOffset = cachedItems.size
            callback(true)
            isFetching = false
            return
        }

        lifecycleScope.launch {
            SpotifyRemote.getChildrenOfItem(
                item,
                currentOffset,
                LOAD_BATCH_SIZE,
                5000L
            ) { newItems ->
                if (!isResumed && items.isNotEmpty()) {
                    // Fragment 不在前台且已有数据，忽略新数据
                    isFetching = false
                    return@getChildrenOfItem
                }

                if (newItems == null) {
                    callback(false)
                    return@getChildrenOfItem
                }

                if (newItems.isNotEmpty()) {
                    currentOffset += newItems.size
                    items += newItems
                    ItemCachePool.cache(item.id, items.toMutableList())
                    callback(true)
                } else {
                    callback(false)
                }
                isFetching = false
            }
        }
    }
}