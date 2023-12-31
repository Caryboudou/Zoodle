package com.kalzakath.zoodle

import android.graphics.Canvas
import android.graphics.Color
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.kalzakath.zoodle.model.MoodEntryModel

class SwipeHelperCallback(val adaptor: ItemTouchHelperAdaptor): ItemTouchHelper.Callback() {
    interface ItemTouchHelperAdaptor {
        fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
        fun onItemDismiss(position: Int)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
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
        val itemView = viewHolder.itemView

        itemView.setBackgroundColor(0)

        itemView.background.setBounds(
            itemView.left,
            itemView.top + 11,
            itemView.left + dX.toInt(),
            itemView.bottom - 11
        )

        itemView.background.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun onMoved(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        fromPos: Int,
        target: RecyclerView.ViewHolder,
        toPos: Int,
        x: Int,
        y: Int
    ) {
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        adaptor.onItemDismiss(viewHolder.adapterPosition)
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        if (viewHolder.itemViewType == MoodEntryModel().viewType) {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlags = ItemTouchHelper.RIGHT
            return makeMovementFlags(dragFlags, swipeFlags)
        }
        return 0
    }
}