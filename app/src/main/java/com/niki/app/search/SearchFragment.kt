package com.niki.app.search

import androidx.lifecycle.lifecycleScope
import com.niki.app.App
import com.niki.app.AppTokenHelper
import com.niki.app.databinding.FragmentSearchBinding
import com.niki.app.net.handleRequest
import com.niki.app.net.web_api.SpotifyApi
import com.niki.app.showInputDialog
import com.niki.app.showMDDialog
import com.niki.spotify.web.LIMIT
import com.zephyr.vbclass.ViewBindingFragment
import kotlinx.coroutines.flow.map

class SearchFragment : ViewBindingFragment<FragmentSearchBinding>() {
    private val api: SpotifyApi by lazy { SpotifyApi() }

    private var ok = false
    private var isSearching = false

    override fun FragmentSearchBinding.initBinding() {

        AppTokenHelper.observeState(lifecycleScope) {
            map {
                it.isAvailable // 细化观察的属性
            }.collect { v ->
                if (v)
                    ok = true
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && ok)
            requireActivity().showInputDialog("搜索", "要搜什么?") { c ->
                val str = c.toString()
                if (str.isNotBlank())
                    search(str)
            }
    }

    private fun search(q: String) {
        if (isSearching) return
        isSearching = true
        App.loadingDialog?.show()

        api.searchService.searchAlbums(
            q,
            mapOf(
                LIMIT to "3"
            )
        ).handleRequest(
            onSuccess = { data ->
                val album = data?.albums?.items?.get(0)
                requireActivity().showMDDialog(
                    album?.name ?: "error",
                    album?.release_date ?: "error"
                )
            },
            onFinish = {
                App.loadingDialog?.dismiss()
                isSearching = false
            }
        )
    }
}