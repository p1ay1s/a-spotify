package com.niki.app.listen_now

import androidx.recyclerview.widget.RecyclerView
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

    override fun FragmentListenNowBinding.initBinding() {

        SpotifyRemote.run {
            collectionAdapter = CollectionAdapter()

            collectionAdapter.setOnClickListener(object : OnClickListener {
                override fun onClicked(item: ListItem) {
                    vibrator?.vibrate(25L)
                    openNewListItemFragment(item) { success ->
                        if (item.playable && !success)
                            play(item)
                    }
                }

                override fun onLongClicked(item: ListItem) {
                    vibrator?.vibrate(25L)
                    requireActivity().showItemInfo(item)
                }

            })
            connected.observe(this@ListenNowFragment) { connected ->
                if (!connected)
                    return@observe
                getContentList {
                    collectionAdapter.submitList(it)
                }
            }
        }

        recyclerView.run {
            adapter = collectionAdapter
            layoutManager = PreloadLayoutManager(requireActivity(), RecyclerView.VERTICAL)
        }
    }
}