package com.niaouh.moodtracker

import com.niaouh.moodtracker.interfaces.RowEntryModel
import com.niaouh.moodtracker.model.FilterEntryModel
import com.niaouh.moodtracker.model.MoodEntryModel
import com.niaouh.moodtracker.model.WeekFilterEntryModel

open class RowEntryFactory {
    open fun create(viewType: Int): RowEntryModel {
        return when (viewType) {
            FilterEntryModel().viewType -> FilterEntryModel()
            WeekFilterEntryModel().viewType -> WeekFilterEntryModel()
            else -> MoodEntryModel()
        }
    }
}