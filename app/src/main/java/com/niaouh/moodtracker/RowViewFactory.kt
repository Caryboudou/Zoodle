package com.niaouh.moodtracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.niaouh.moodtracker.model.FilterEntryModel
import com.niaouh.moodtracker.model.WeekFilterEntryModel

class RowViewFactory {

    fun createView(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder: RowViewHolder = when (viewType) {
            FilterEntryModel().viewType -> FilterViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.filter_entry_layout, parent, false)
            )
            WeekFilterEntryModel().viewType -> WeekFilterViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.week_entry_layout, parent, false)
            )
            else -> MoodViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.mood_entry_layout, parent, false)
            )
        }
        return holder
    }
}