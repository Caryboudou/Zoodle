package com.niaouh.moodtracker.model

import androidx.recyclerview.widget.RecyclerView
import com.niaouh.moodtracker.FilterViewHolder
import com.niaouh.moodtracker.RowViewHolder
import com.niaouh.moodtracker.interfaces.RowEntryModel
import java.time.LocalDateTime

data class FilterEntryModel (
    var title: String = "",
    override var date: LocalDateTime = LocalDateTime.now(),
    override var key: String = "default_row_key"

        ): RowEntryModel {

    @Transient
    lateinit var viewHolder: RowViewHolder
    override var viewType: Int = 2
}

fun FilterEntryModel.bindToViewHolder(holder: RecyclerView.ViewHolder) {
    val viewHolder = holder as FilterViewHolder
    viewHolder.tvFilterTitle.text = title
}

fun FilterEntryModel.toMap(): Map<String, Any?> {
    return mapOf(
        "title" to title,
        "date" to date,
        "key" to key
    )
}