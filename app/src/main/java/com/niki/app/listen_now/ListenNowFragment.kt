package com.niki.app.listen_now

import androidx.recyclerview.widget.RecyclerView
import com.niki.app.SpotifyRemote
import com.niki.app.databinding.FragmentListenNowBinding
import com.niki.app.openNewListItemFragment
import com.niki.app.ui.CollectionAdapter
import com.niki.app.vibrator
import com.zephyr.base.ui.PreloadLayoutManager
import com.zephyr.vbclass.ViewBindingFragment

class ListenNowFragment : ViewBindingFragment<FragmentListenNowBinding>() {

    private lateinit var collectionAdapter: CollectionAdapter

    override fun FragmentListenNowBinding.initBinding() {

        SpotifyRemote.run {
            collectionAdapter = CollectionAdapter { item ->
                vibrator?.vibrate(25L)
                openNewListItemFragment(item)
            }

            connected.observe(this@ListenNowFragment) { connected ->
                if (connected) {
                    getItemListNonNull {
                        collectionAdapter.submitList(it)
                    }
                }
            }
        }

        recyclerView.run {
            adapter = collectionAdapter
            layoutManager = PreloadLayoutManager(requireActivity(), RecyclerView.VERTICAL)
        }
    }
}