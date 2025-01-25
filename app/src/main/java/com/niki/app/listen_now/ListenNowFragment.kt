package com.niki.app.listen_now

import androidx.recyclerview.widget.RecyclerView
import com.niki.app.SpotifyRemote
import com.niki.app.databinding.FragmentListenNowBinding
import com.niki.app.ui.ListenNowAdapter
import com.zephyr.base.ui.PreloadLayoutManager
import com.zephyr.vbclass.ViewBindingFragment

class ListenNowFragment : ViewBindingFragment<FragmentListenNowBinding>() {

    private lateinit var listenNowAdapter: ListenNowAdapter

    override fun FragmentListenNowBinding.initBinding() {

        SpotifyRemote.run {
            listenNowAdapter = ListenNowAdapter {
                playPlaylist(it)
            }

            connected.observe(this@ListenNowFragment) { connected ->
                if (connected) {
                    getItemListNonNull {
                        listenNowAdapter.submitList(it)
                    }
                }
            }
        }

        recyclerView.run {
            adapter = listenNowAdapter
            layoutManager = PreloadLayoutManager(requireActivity(), RecyclerView.VERTICAL)
        }
    }
}