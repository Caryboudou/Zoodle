package com.niaouh.moodtracker

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.ArrayList
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

object Settings {
    object Default {
        const val moodMode: Int = MoodModes.FACES
        const val fatigueMode: Int = FatigueModes.FACES
        const val modeNote = false
        const val moodMax: Int = 5
        const val fatigueMax: Int = 5
        const val notificationTime = "20:00"
        const val notificationAct = false
        const val medicationName = "Ritaline"
        val notificationList: ArrayList<String> = arrayListOf()
    }

    object MoodModes {
        const val NUMBERS = 0
        const val FACES = 1
    }
    object FatigueModes {
        const val NUMBERS = 0
        const val FACES = 1
    }

    var moodMode: Int = Default.moodMode
    var moodMax: Int = Default.moodMax
    var fatigueMode: Int = Default.fatigueMode
    var fatigueMax: Int = Default.fatigueMax
    var modeNote = Default.modeNote
    var notificationTime = Default.notificationTime
    var notificationAct = Default.notificationAct
    var medicationName = Default.medicationName
    var notificationList = Default.notificationList

    fun setDefaultSettings() {
        for (prop in Settings::class.memberProperties) {
            val defaultVal =
                Default::class.java.declaredFields.find { it.name == prop.name }
            if (defaultVal != null) {
                if (prop is KMutableProperty<*>) {
                    prop.setter.call(Settings, defaultVal.get(Default))
                }
            }
        }
    }
}
