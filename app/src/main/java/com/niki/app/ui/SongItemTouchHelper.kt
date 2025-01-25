package com.niki.app.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.niki.app.R
import com.spotify.protocol.types.ListItem

class SongItemTouchHelper(
    private val adapter: SongAdapter,
    private val onCollectListener: (ListItem) -> Unit,
    private val onDeleteListener: (ListItem) -> Unit
) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        // 设置可以左右滑动
        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
        return makeMovementFlags(0, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val song = adapter.currentList[position]

        when (direction) {
            ItemTouchHelper.START -> {
                // 左滑操作（例如删除）
                onDeleteListener(song)
            }

            ItemTouchHelper.END -> {
                // 右滑操作（例如收藏）
                onCollectListener(song)
            }
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val itemView = viewHolder.itemView
            val background = drawSwipeBackground(c, itemView, dX)

            // 绘制图标
            drawSwipeIcons(c, itemView, dX)

            // 执行实际的滑动
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    private fun drawSwipeBackground(c: Canvas, itemView: View, dX: Float): RectF {
        val background = if (dX > 0) {
            // 右滑背景（收藏）
            Paint().apply { color = Color.GREEN }
        } else {
            // 左滑背景（删除）
            Paint().apply { color = Color.RED }
        }

        val backgroundRect = RectF(
            itemView.left.toFloat(),
            itemView.top.toFloat(),
            itemView.right.toFloat(),
            itemView.bottom.toFloat()
        )
        c.drawRect(backgroundRect, background)
        return backgroundRect
    }

    private fun drawSwipeIcons(c: Canvas, itemView: View, dX: Float) {
        val icon = if (dX > 0) {
            // 右滑图标（收藏）
            ContextCompat.getDrawable(itemView.context, R.drawable.ic_next)
        } else {
            // 左滑图标（删除）
            ContextCompat.getDrawable(itemView.context, R.drawable.ic_previous)
        }

        icon?.let {
            val iconMargin = (itemView.height - it.intrinsicHeight) / 2
            val iconTop = itemView.top + iconMargin
            val iconBottom = iconTop + it.intrinsicHeight

            if (dX > 0) { // 右滑
                val iconLeft = itemView.left + iconMargin
                val iconRight = iconLeft + it.intrinsicWidth
                it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            } else { // 左滑
                val iconRight = itemView.right - iconMargin
                val iconLeft = iconRight - it.intrinsicWidth
                it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            }

            it.draw(c)
        }
    }
}

//fun Fragment.setupItemTouchHelper(adapter: SongAdapter, recyclerView: RecyclerView) {
//    val callback = SongItemTouchHelper(
//        adapter,
//        onCollectListener = { song ->
//            "v".toast()
//        },
//        onDeleteListener = { song ->
//            "x".toast()
//        }
//    )
//
//    val itemTouchHelper = ItemTouchHelper(callback)
//    itemTouchHelper.attachToRecyclerView(recyclerView)
//}