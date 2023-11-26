package com.niaouh.moodtracker

import android.app.AlertDialog
import android.app.Dialog
import android.app.SearchManager.OnCancelListener
import android.content.Context
import android.content.DialogInterface
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.niaouh.moodtracker.interfaces.RowEntryModel
import com.niaouh.moodtracker.model.MoodEntryModel

class SwipeHelperCallback(
    val context: Context,
    val view: View,
    val adaptor: ItemTouchHelperAdaptor): ItemTouchHelper.Callback() {

    interface ItemTouchHelperAdaptor {
        fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
        fun onItemDismiss(position: Int) : RowEntryModel
        fun onItemAdd(row: RowEntryModel)
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

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val row = adaptor.onItemDismiss(position)
        val date = (row as MoodEntryModel).textMoodSnackbar()
        val textSnackbar = "Entrée du $date supprimée"

        val onClickListenner = View.OnClickListener { _ -> adaptor.onItemAdd(row) }
        Snackbar.make(view,
            textSnackbar,
            Snackbar.LENGTH_SHORT)
            .setAction("ANNULER", onClickListenner)
            .show()
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        if (viewHolder.itemViewType == MoodEntryModel().viewType) {
            return makeMovementFlags(0, ItemTouchHelper.RIGHT)
        }
        return 0
    }
}