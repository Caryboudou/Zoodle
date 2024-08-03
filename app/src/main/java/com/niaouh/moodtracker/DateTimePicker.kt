package com.niaouh.moodtracker

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import com.niaouh.moodtracker.model.MoodEntryModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class DateTimePicker {

    private val calendar
            : Calendar = Calendar.getInstance(TimeZone.getDefault())

    fun show(context: Context) {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)

            onUpdateListener?.invoke(calendar)
        }

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)

                TimePickerDialog(
                    context,
                    timeSetListener,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            }

        DatePickerDialog(
            context, dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    var onUpdateListener: ((Calendar)->Unit)? = null
}
class DatePicker {
    private val calendar
            : Calendar = Calendar.getInstance(TimeZone.getDefault())

    fun show(context: Context, date: LocalDateTime? = null) {
        var year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH)
        var day = calendar.get(Calendar.DAY_OF_MONTH)
        if (date != null) {
            year = date.year
            month = date.monthValue - 1
            day = date.dayOfMonth
        }

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, y, m, d ->
                calendar.set(Calendar.YEAR, y)
                calendar.set(Calendar.MONTH, m)
                calendar.set(Calendar.DAY_OF_MONTH, d)

                onUpdateListener?.invoke(calendar)
            }

        DatePickerDialog(
            context, dateSetListener,
            year,
            month,
            day
        ).show()
    }

    fun show(context: Context, date: LocalDate? = null) {
        var year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH)
        var day = calendar.get(Calendar.DAY_OF_MONTH)
        if (date != null) {
            year = date.year
            month = date.monthValue - 1
            day = date.dayOfMonth
        }

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, y, m, d ->
                calendar.set(Calendar.YEAR, y)
                calendar.set(Calendar.MONTH, m)
                calendar.set(Calendar.DAY_OF_MONTH, d)

                onUpdateListener?.invoke(calendar)
            }

        DatePickerDialog(
            context, dateSetListener,
            year,
            month,
            day
        ).show()
    }

    var onUpdateListener: ((Calendar)->Unit)? = null
}

class TimePicker {
    private val calendar
            : Calendar = Calendar.getInstance(TimeZone.getDefault())

    fun show(context: Context, time: LocalDateTime) {
        val hour = time.hour
        val minute = time.minute

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, h, m ->
        calendar.set(Calendar.HOUR_OF_DAY, h)
        calendar.set(Calendar.MINUTE, m)
        onUpdateListener?.invoke(calendar)
        }

        TimePickerDialog(
            context,
            timeSetListener,
            hour,
            minute,
            true
        ).show()
    }

    fun show(context: Context, time: LocalTime? = null) {
        var hour = calendar.get(Calendar.HOUR_OF_DAY)
        var minute = calendar.get(Calendar.MINUTE)
        if (time != null) {
            hour = time.hour
            minute = time.minute
        }

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, h, m ->
        calendar.set(Calendar.HOUR_OF_DAY, h)
        calendar.set(Calendar.MINUTE, m)
        onUpdateListener?.invoke(calendar)
        }

        TimePickerDialog(
            context,
            timeSetListener,
            hour,
            minute,
            true
        ).show()
    }

    var onUpdateListener: ((Calendar)->Unit)? = null
}