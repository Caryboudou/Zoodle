package com.niaouh.moodtracker

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

class WeekFilterViewHolder(itemView: View) : RowViewHolder(itemView) {
    val sepWeekFilter: TextView = itemView.findViewById(R.id.sepWeek)
    val body: ConstraintLayout = itemView.findViewById(R.id.cWeekFilter)
}