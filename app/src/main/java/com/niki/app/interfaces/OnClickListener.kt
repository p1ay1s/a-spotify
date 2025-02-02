package com.niki.app.interfaces

import com.spotify.protocol.types.ListItem

interface OnClickListener {
    fun onClicked(clickedItem: ListItem, position: Int)
    fun onLongClicked(clickedItem: ListItem, parentItem: ListItem)
}