package com.niki.app.song

import android.widget.LinearLayout
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.niki.app.App
import com.niki.app.ContentType
import com.niki.app.PRE_LOAD_NUM
import com.niki.app.databinding.FragmentSongBinding
import com.niki.app.interfaces.OnClickListener
import com.niki.app.main.MainActivity
import com.niki.app.openNewFragment
import com.niki.app.parseSpotifyId
import com.niki.app.showItemInfoDialog
import com.niki.app.song.SongFragment.Companion.ERROR_MSG
import com.niki.app.song.ui.SongAdapter
import com.niki.app.toastM
import com.niki.app.ui.ScrollTextView
import com.niki.app.util.SongRepository
import com.niki.app.util.loadLargeImage
import com.niki.spotify.remote.PlayerApi.playItemAtIndex
import com.niki.util.toBlurDrawable
import com.spotify.protocol.types.ListItem
import com.zephyr.base.extension.addLineDecoration
import com.zephyr.base.extension.addOnLoadMoreListener_V
import com.zephyr.base.extension.setMargins
import com.zephyr.base.ui.PreloadLayoutManager
import com.zephyr.vbclass.ViewBindingFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val songFragmentLock = Any()
private var isOpening = false

fun Fragment.openSongFragment(item: ListItem, callback: (Boolean) -> Unit) =
    synchronized(songFragmentLock) {
        if (isOpening) return@synchronized
        isOpening = true
        lifecycleScope.launch(Dispatchers.Main) {
            delay(800) // 如果很快响应就不显示
            if (isOpening)
                App.loadingDialog?.show()
        }
        val songFragment = SongFragment()
        songFragment.let {
            it.setListItem(item)
            it.listener = object : SongFragment.Listener {
                override fun onFetched(fragment: SongFragment) =
                    lifecycleScope.launch(Dispatchers.Main) {
                        App.loadingDialog?.hide()
                        openNewFragment(item.id, fragment)
                        callback(true)
                        isOpening = false
                    }

                override fun onError() = lifecycleScope.launch(Dispatchers.Main) {
                    App.loadingDialog?.hide()
                    com.niki.spotify.remote.logS(ERROR_MSG)
                    callback(false)
                    isOpening = false
                }
            }
            it.load()
        }
    }

class SongFragment : ViewBindingFragment<FragmentSongBinding>() {

    companion object {
        const val ERROR_MSG = "获取不到数据, 不打开 SongFragment"
    }

    interface Listener {
        fun onFetched(fragment: SongFragment): Any
        fun onError(): Any
    }

    private val repository: SongRepository by lazy { SongRepository() }

    private lateinit var item: ListItem
    var listener: Listener? = null

    private lateinit var songAdapter: SongAdapter

    fun setListItem(item: ListItem) {
        this.item = item
        repository.item = item
    }

    fun load() {
        // 重置状态
        repository.reset()
        repository.loadData {
            if (it) {
                listener?.onFetched(this)
            } else {
                listener?.onError()
            }
            listener = null
        }
    }

    override fun FragmentSongBinding.initBinding() {
        songAdapter = SongAdapter(item.id.parseSpotifyId())

        bar.apply {
            getTextView().apply {
                text = item.title + " - " + item.subtitle
//                speed = 20
//                speed = ScrollSpeed.left(300F)
                mode = ScrollTextView.ScrollMode.BOUNCE
            }
            getButton().setOnClickListener {
                App.mainActivity.get()?.popFragment()
            }
            doOnLayout {
                if (MainActivity.isEnableEdgeToEdge) {
                    bar.setMargins(top = MainActivity.statusBarHeight)
                    songAdapter.firstItemMarginTop = bar.height + MainActivity.statusBarHeight
                }
            }
        }

        if (repository.list.value.isNullOrEmpty())
            repository.loadData { }

        if (item.id.parseSpotifyId() == ContentType.ALBUM)
            loadLargeImage(item.imageUri.raw!!) { bitmap ->
                requireActivity().toBlurDrawable(bitmap) {
                    bar.background = it
                }
            }

        songAdapter.apply {
            parentItem = item
            setOnClickListener(object : OnClickListener {
                override fun onClicked(clickedItem: ListItem, position: Int) {
                    App.vibrator?.vibrate(25L)
                    when {
                        clickedItem.hasChildren ->
                            openSongFragment(clickedItem) { success ->
                                if (!success) {
                                    toastM("未知错误")
                                    playItemAtIndex(
                                        item, // 此 item 应为歌单列表 item
                                        position
                                    )
                                }
                            }

                        clickedItem.playable -> {
                            playItemAtIndex(
                                item, // 此 item 应为歌单列表 item
                                position
                            )
                        }

                        else ->
                            toastM("playable = false")
                    }
                }

                override fun onLongClicked(clickedItem: ListItem, parentItem: ListItem) {
                    App.vibrator?.vibrate(25L)
                    requireActivity().showItemInfoDialog(clickedItem)
//                    showSongDetail(
//                        Song(
//                            clickedItem.title,
//                            clickedItem.subtitle,
//                            parentItem.title,
//                            clickedItem.imageUri.raw!!,
//                            clickedItem.id,
//                            parentItem.id,
//                            parentItem.id
//                        )
//                    ) { fragment, item ->
//                        openSongFragment(item) { success ->
//                            if (!success)
//                                "无法打开".toast()
//                            else
//                                fragment.dismiss()
//                        }
//                    }
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
                repository.loadData { }
            }
        }

        repository.list.observe(this@SongFragment) { list ->
            if (list.isNotEmpty())
                songAdapter.submitList(list)
        }
    }

    override fun onDetach() {
        songAdapter.setOnClickListener(null)
        super.onDetach()
    }
}