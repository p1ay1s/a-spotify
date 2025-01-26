package com.niki.app.listen_now

import androidx.recyclerview.widget.RecyclerView
import com.niki.app.PRE_LOAD_NUM
import com.niki.app.SpotifyRemote
import com.niki.app.databinding.FragmentListenNowBinding
import com.niki.app.interfaces.OnClickListener
import com.niki.app.openNewListItemFragment
import com.niki.app.showItemInfo
import com.niki.app.ui.CollectionAdapter
import com.niki.app.vibrator
import com.spotify.protocol.types.ListItem
import com.zephyr.base.ui.PreloadLayoutManager
import com.zephyr.vbclass.ViewBindingFragment

class ListenNowFragment : ViewBindingFragment<FragmentListenNowBinding>() {

    private lateinit var collectionAdapter: CollectionAdapter

    private var isOpening = false

    override fun FragmentListenNowBinding.initBinding() {

        SpotifyRemote.run {
            collectionAdapter = CollectionAdapter()

            collectionAdapter.setOnClickListener(object : OnClickListener {
                override fun onClicked(item: ListItem) {
                    if (isOpening) return
                    isOpening = true
                    vibrator?.vibrate(25L)
                    openNewListItemFragment(item) { success ->
                        if (item.playable && !success) // 当 item 可播放并且无法打开歌单 fragment 时播放它
                            play(item)
                        isOpening = false
                    }
                }

                override fun onLongClicked(item: ListItem) {
                    vibrator?.vibrate(25L)
                    requireActivity().showItemInfo(item)
                }

            })
            isConnected.observe(this@ListenNowFragment) { connected ->
                if (!connected)
                    return@observe
                getContentList {
                    collectionAdapter.submitList(it)
                }
            }
        }

        recyclerView.run {
            adapter = collectionAdapter
            layoutManager = PreloadLayoutManager(
                requireActivity(), RecyclerView.VERTICAL,
                PRE_LOAD_NUM
            )
        }
    }
}