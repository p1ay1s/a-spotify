package com.niki.app.listen_now

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niki.app.util.withProgressBar
import com.niki.spotify.remote.ContentApi
import com.niki.spotify.remote.ListItemResult
import com.niki.spotify.remote.RemoteManager
import com.spotify.protocol.types.ListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListenNowViewModel : ViewModel() {

    var lastExitTime = -1L

    private var isFetching = false

    private var _contentList: MutableLiveData<List<ListItem>> = MutableLiveData(emptyList())
    val contentList: LiveData<List<ListItem>>
        get() = _contentList

    init {
        lastExitTime = System.currentTimeMillis()

        com.niki.spotify.remote.RemoteManager.isConnected.observeForever {
            if (it) fetch()
        }
    }

    fun fetch() {
        if (isFetching) return
        isFetching = true

        viewModelScope.launch {
            withProgressBar {
                val result = com.niki.spotify.remote.ContentApi.getContentList()
                if (result is com.niki.spotify.remote.ListItemResult.HasChildren && result.list.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        _contentList.value = result.list
                    }
                }
                isFetching = false
            }
        }
    }
}