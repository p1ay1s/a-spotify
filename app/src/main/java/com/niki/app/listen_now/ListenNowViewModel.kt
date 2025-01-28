package com.niki.app.listen_now

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.niki.app.SpotifyRemote
import com.spotify.protocol.types.ListItem

class ListenNowViewModel : ViewModel() {

    var isOpening = false
    var shouldLoad = false
    var lastExitTime = -1L

    private var isFetching = false

    private var _contentList: MutableLiveData<List<ListItem>> = MutableLiveData(emptyList())
    val contentList: LiveData<List<ListItem>>
        get() = _contentList

    init {
        lastExitTime = System.currentTimeMillis()

        SpotifyRemote.isConnected.observeForever {
            if (it) fetch()
        }
    }

    fun fetch() {
        if (isFetching) return
        isFetching = true
        SpotifyRemote.getContentList { list ->
            _contentList.value = list
            isFetching = false
        }
    }
}