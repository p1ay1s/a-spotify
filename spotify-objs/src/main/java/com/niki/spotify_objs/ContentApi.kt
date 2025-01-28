package com.niki.spotify_objs

import com.niki.spotify_objs.RemoteManager.remote
import com.spotify.android.appremote.api.ContentApi
import com.spotify.protocol.types.ListItem
import com.spotify.protocol.types.ListItems

object ContentApi {
    fun getListItems(): ListItems? {
        checkThread()
        val listItems = remote?.contentApi
            ?.getRecommendedContentItems(ContentApi.ContentType.DEFAULT) // 使用不同的 type 得到的结果似乎是相同的
            ?.get()
        return listItems
    }

    fun getListItems(callback: (ListItems?) -> Unit) {
        remote?.contentApi
            ?.getRecommendedContentItems(ContentApi.ContentType.DEFAULT) // 使用不同的 type 得到的结果似乎是相同的
            ?.get(callback)
    }

    fun getContentList(callback: (List<ListItem>?) -> Unit) {
        getListItems { callback(it?.toList()) }
    }

    fun getContentList(): List<ListItem>? {
        checkThread()
        return getListItems()?.toList()
    }

    /**
     * 获取 listItem 中的歌单
     *
     * 当返回空 list 则表示获取成功但无数据, 如果返回空则表示错误
     */
    fun getChildrenOfItem(
        item: ListItem,
        offset: Int,
        size: Int
    ): List<ListItem>? {
        checkThread()
        val list: List<ListItem>? = remote?.contentApi
            ?.getChildrenOfItem(item, size, offset)
            ?.get()?.toList()

        return list
    }

    /**
     * 获取 listItem 中的歌单
     *
     * 当返回空 list 则表示获取成功但无数据, 如果返回空则表示错误
     */
    fun getChildrenOfItem(
        item: ListItem,
        offset: Int,
        size: Int,
        callback: (List<ListItem>?) -> Unit
    ) {
        remote?.contentApi
            ?.getChildrenOfItem(item, size, offset)
            ?.get {
                callback(it?.toList())
            }
    }
}