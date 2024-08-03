package com.niaouh.moodtracker.interfaces

import java.time.LocalDateTime

interface RowEntryModel {
    var date: LocalDateTime
    var key: String
    var viewType: Int
}