package com.kalzakath.zoodle.model

import com.kalzakath.zoodle.data.Month

class Date (Iday: Int = 1, Imonth: Int = 1, Iyear: Int = 2020, test: Int ) {
    val day: String
    val month: Month
    val year: String

    init {
        day = if (Iday < 10 ) {
            "0$Iday"
        } else {
            Iday.toString()
        }
        month = if (Imonth == 2) {
            if (Iyear.mod(4) == 0 && Iyear.mod(100) == 0) {
                Month.FEVRIER_B
            } else {
                Month.FEVRIER_NB
            }
        } else {
            Month.from(Imonth)
        }
        year = Iyear.toString()
    }

    fun compare(other: Date): Boolean {
        if (day.toInt() != other.day.toInt()) return false
        if (month != other.month) return false
        if (year.toInt() != other.year.toInt()) return false
        return true
    }
}