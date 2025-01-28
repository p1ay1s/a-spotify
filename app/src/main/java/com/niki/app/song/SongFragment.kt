package com.niki.app.song

import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.niki.app.SpotifyRemote
import com.niki.app.databinding.FragmentListItemBinding
import com.niki.app.song.ui.SongAdapter
import com.niki.app.util.ContentType
import com.niki.app.util.ItemCachePool
import com.niki.app.util.LOAD_BATCH_SIZE
import com.niki.app.util.PRE_LOAD_NUM
import com.niki.app.util.noChildListItem
import com.niki.app.util.openSongFragment
import com.niki.app.util.parseSpotifyId
import com.niki.app.util.showItemInfoDialog
import com.niki.app.util.toastM
import com.niki.app.util.vibrator
import com.niki.util.toBlurDrawable
import com.spotify.protocol.types.ListItem
import com.zephyr.base.extension.addLineDecoration
import com.zephyr.base.extension.addOnLoadMoreListener_V
import com.zephyr.base.ui.PreloadLayoutManager
import com.zephyr.vbclass.ViewBindingFragment

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
    private var isOpening = false

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
            listener = null
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
                        SpotifyRemote.playItemAtIndex(
                            this@SongFragment.item, // 此 item 应为歌单列表 item
                            position
                        )
                    } else {
                        if (isOpening) return
                        isOpening = true
                        openSongFragment(item) { success ->
                            if (!success) toastM("未知错误")
                            isOpening = false
                        }
                    }
                }

                override fun onLongClicked(item: ListItem) {
                    vibrator?.vibrate(25L)
                    requireActivity().showItemInfoDialog(item)
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

        loadInitialDataToAdapter()
    }

    private fun loadInitialDataToAdapter() {
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

        if (cachedItems?.contains(noChildListItem) == true) {
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

        SpotifyRemote.getChildrenOfItem(
            item,
            currentOffset,
            LOAD_BATCH_SIZE
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