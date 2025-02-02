package com.niki.spotify_objs

import com.spotify.protocol.types.ListItem

/**
 * 设计用于表示单次获取的结果
 */
sealed class ListItemResult {
    data object Error : ListItemResult()
    data object NoChildren : ListItemResult()
    class HasChildren(
        val offset: Int,
        val isFull: Boolean,
        val list: List<ListItem>
    ) : ListItemResult()
}