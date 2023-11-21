package com.kalzakath.zoodle.model

import androidx.recyclerview.widget.RecyclerView
import com.kalzakath.zoodle.FilterViewHolder
import com.kalzakath.zoodle.MoodViewHolder
import com.kalzakath.zoodle.RowViewHolder
import com.kalzakath.zoodle.WeekFilterViewHolder
import com.kalzakath.zoodle.interfaces.RowEntryModel

data class WeekFilterEntryModel (
    override var date: String = "1990-01-01",
    override var time: String = "09:09",
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
        "time" to time,
        "key" to key
    )
}