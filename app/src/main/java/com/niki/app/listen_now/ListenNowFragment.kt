package com.niki.app.listen_now

import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.niki.app.databinding.FragmentListenNowBinding
import com.niki.app.interfaces.OnClickListener
import com.niki.app.listen_now.ui.PlaylistCollectionAdapter
import com.niki.app.util.PRE_LOAD_NUM
import com.niki.app.util.openSongFragment
import com.niki.app.util.showItemInfoDialog
import com.niki.app.util.vibrator
import com.niki.spotify_objs.PlayerApi
import com.spotify.protocol.types.ListItem
import com.zephyr.base.extension.TAG
import com.zephyr.base.extension.addOnLoadMoreListener_V
import com.zephyr.base.log.logE
import com.zephyr.base.ui.PreloadLayoutManager
import com.zephyr.vbclass.ViewBindingFragment

class ListenNowFragment : ViewBindingFragment<FragmentListenNowBinding>() {

    companion object {
        const val RE_FETCH_TIME = 5 * 60 * 1000 // 5 min
    }

    private lateinit var collectionAdapter: PlaylistCollectionAdapter
    private lateinit var viewmodel: ListenNowViewModel

    override fun FragmentListenNowBinding.initBinding() {
        viewmodel = ViewModelProvider(requireActivity())[ListenNowViewModel::class.java]

        collectionAdapter = PlaylistCollectionAdapter()

        viewmodel.contentList.observe(this@ListenNowFragment) { list ->
            viewmodel.shouldLoad = false
            collectionAdapter.submitList(list)
        }

        recyclerView.run {
            adapter = collectionAdapter
            layoutManager = PreloadLayoutManager(
                requireActivity(), RecyclerView.VERTICAL,
                PRE_LOAD_NUM
            )
            addOnLoadMoreListener_V(-1) {
                if (!viewmodel.shouldLoad) return@addOnLoadMoreListener_V
                viewmodel.shouldLoad = true
                viewmodel.fetch()
            }
        }

        collectionAdapter.setOnClickListener(object : OnClickListener {
            override fun onClicked(item: ListItem) {
                if (viewmodel.isOpening) return
                viewmodel.isOpening = true
                vibrator?.vibrate(25L)
                openSongFragment(item) { success ->
                    if (item.playable && !success) // 当 item 可播放并且无法打开歌单 fragment 时播放它
                        PlayerApi.play(item)
                    viewmodel.isOpening = false
                }
            }

            override fun onLongClicked(item: ListItem) {
                vibrator?.vibrate(25L)
                requireActivity().showItemInfoDialog(item)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        if (System.currentTimeMillis() - viewmodel.lastExitTime >= RE_FETCH_TIME) {
            logE(TAG, "达到刷新时长, 获取数据")
            viewmodel.fetch()
        }
    }

    override fun onPause() {
        super.onPause()
        viewmodel.lastExitTime = System.currentTimeMillis()
    }
}