package com.niki.app.listen_now

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niki.spotify_objs.ContentApi
import com.niki.spotify_objs.RemoteManager
import com.spotify.protocol.types.ListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        RemoteManager.isConnected.observeForever {
            if (it) fetch()
        }
    }

    fun fetch() {
        if (isFetching) return
        isFetching = true
        ContentApi.getContentList { list ->
            viewModelScope.launch(Dispatchers.Main) {
                list?.let { _contentList.value = it }
            }
            isFetching = false
        }
    }
}