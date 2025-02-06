package com.niki.app.listen_now

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niki.app.App
import com.niki.spotify.remote.ContentApi
import com.niki.spotify.remote.ListItemResult
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
    }

    fun fetch() {
        if (isFetching) return
        isFetching = true

        viewModelScope.launch(Dispatchers.Main) {
            App.loadingDialog?.show()
            val result = withContext(Dispatchers.IO) {
                ContentApi.getContentList()
            }
            App.loadingDialog?.dismiss()
            if (result is ListItemResult.HasChildren && result.list.isNotEmpty()) {
                _contentList.value = result.list
            }
            isFetching = false
        }
    }
}