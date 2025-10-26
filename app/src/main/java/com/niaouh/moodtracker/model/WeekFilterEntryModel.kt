package com.niaouh.moodtracker.model

import androidx.recyclerview.widget.RecyclerView
import com.niaouh.moodtracker.RowViewHolder
import com.niaouh.moodtracker.WeekFilterViewHolder
import com.niaouh.moodtracker.interfaces.RowEntryModel
import java.time.LocalDateTime

data class WeekFilterEntryModel (
    override var date: LocalDateTime = LocalDateTime.now(),
    override var key: String = "default_row_key"
): RowEntryModel {

    @Transient
    var viewHolder: RowViewHolder? = null
    var isVisible: Boolean = true
    override var viewType: Int = 3
}

fun WeekFilterEntryModel.bindToViewHolder(holder: RecyclerView.ViewHolder) {
    val wViewHolder = holder as WeekFilterViewHolder
    hideRow(wViewHolder)
    viewHolder = holder
}

fun WeekFilterEntryModel.hideRow(wViewHolder: WeekFilterViewHolder) {
    if (isVisible) wViewHolder.body.maxHeight = 1000
    else wViewHolder.body.maxHeight = 0
}

fun WeekFilterEntryModel.toMap(): Map<String, Any?> {
    return mapOf(
        "date" to date,
        "key" to key
    )
}