package com.niki.app.interfaces

import com.spotify.protocol.types.ListItem

interface OnClickListener {
    fun onClicked(item: ListItem)
    fun onLongClicked(item: ListItem)
}