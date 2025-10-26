package com.niaouh.moodtracker.utils

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar

object ResUtil {

    fun getString(resources: Resources, resourceId: Int): String {
        return resources.getString(resourceId)
    }

    fun getFormattedString(resources: Resources, resourceId: Int, vararg textsToFormat: String?): String {
        return String.format(
            resources.getString(resourceId),
            *textsToFormat
        )
    }

    fun getDateStringFR(year: Int, month: Int, day:Int) : String {
        val monthStr = if (month < 10) "0${month}"
            else month.toString()
        val dayStr = if (day < 10) "0${day}"
            else day.toString()
        return "$dayStr/$monthStr/$year"
    }
    fun getDateStringFR(date: LocalDateTime) : String {
        return getDateStringFR(date.year, date.monthValue, date.dayOfMonth)
    }
    fun getDateStringFR(date: LocalDate) : String {
        return getDateStringFR(date.year, date.monthValue, date.dayOfMonth)
    }
    fun getDateStringFR(calendar: Calendar) : String {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)+1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return getDateStringFR(year, month, day)
    }
    fun getDateStringEN(date: LocalDateTime) : String {
        val year = date.year.toString()
        val month = if (date.month.value < 10) "0${date.month.value}"
            else date.month.value.toString()
        val day = if (date.dayOfMonth < 10) "0${date.dayOfMonth}"
            else date.dayOfMonth.toString()
        return "$year-$month-$day"
    }

    fun getTimeStringFR(hour:Int, minute: Int) : String {
        val hourStr = if (hour < 10) "0${hour}"
            else hour.toString()
        val minuteStr = if (minute < 10) "0${minute}"
            else minute.toString()
        return hourStr+"h"+minuteStr
    }
    fun getTimeStringFR(date: LocalDateTime) : String {
        return getTimeStringFR(date.hour, date.minute)
    }
    fun getTimeStringFR(date: LocalTime) : String {
        return getTimeStringFR(date.hour, date.minute)
    }
    fun getTimeStringFR(calendar: Calendar) : String {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return getTimeStringFR(hour, minute)
    }
    fun getTimeStringEN(hour:Int, minute: Int) : String {
        val hourStr = if (hour < 10) "0${hour}"
            else hour.toString()
        val minuteStr = if (minute < 10) "0${minute}"
            else minute.toString()
        return hourStr+":"+minuteStr
    }
    fun getTimeStringEN(date: LocalDateTime) : String {
        return getTimeStringEN(date.hour, date.minute)
    }
    fun getTimeStringEN(date: LocalTime) : String {
        return getTimeStringEN(date.hour, date.minute)
    }
    fun getTimeStringEN(calendar: Calendar) : String {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return getTimeStringEN(hour, minute)
    }

    fun getLocalDateTimeFromStringFR(date: String, time: String): LocalDateTime {
        val slippedDate = date.split("/")
        val day = slippedDate[0].toInt()
        val month = slippedDate[1].toInt()
        val year = slippedDate[2].toInt()

        val slippedTime = time.split("h")
        val hour = slippedTime[0].toInt()
        val minute = slippedTime[1].toInt()
        return LocalDateTime.of(year, month, day, hour, minute)
    }

    fun getMonthNameFR(month: Int) : String {
        val m = when (month) {
            1 -> "janvier"
            2 -> "février"
            3 -> "mars"
            4 -> "avril"
            5 -> "mai"
            6 -> "juin"
            7 -> "juillet"
            8 -> "août"
            9 -> "septembre"
            10 -> "octobre"
            11 -> "novembre"
            12 -> "décembre"
            else -> ""
        }
        return m
    }

    fun getMonthNameFRMaj(month: Int) : String {
        val m = when (month) {
            1 -> "Janvier"
            2 -> "Février"
            3 -> "Mars"
            4 -> "Avril"
            5 -> "Mai"
            6 -> "Juin"
            7 -> "Juillet"
            8 -> "Août"
            9 -> "Septembre"
            10 -> "Octobre"
            11 -> "Novembre"
            12 -> "Décembre"
            else -> ""
        }
        return m
    }

    fun getDayNameFR(day: Int) : String {
        val d = when (day) {
            1 -> "Lundi"
            2 -> "Mardi"
            3 -> "Mercredi"
            4 -> "Jeudi"
            5 -> "Vendredi"
            6 -> "Samedi"
            7 -> "Dimanche"
            else -> ""
        }
        return d
    }

    fun getColor(context: Context?, resourceId: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context!!.getColor(resourceId)
        } else {
            context!!.resources.getColor(resourceId)
        }
    }

    fun getColorAsColorStateList(resources: Resources, resourceId: Int): ColorStateList {
        return ColorStateList.valueOf(resources.getColor(resourceId))
    }

    fun getBoolean(resources: Resources, resourceId: Int): Boolean {
        return resources.getBoolean(resourceId)
    }

    fun getDrawable(context: Context?, resourceId: Int): Drawable? {
        return context!!.resources.getDrawable(resourceId, context.theme)
    }

    fun getDimenDp(resources: Resources, resourceId: Int): Int {
        return (resources.getDimension(resourceId) / resources.displayMetrics.density).toInt()
    }
}
