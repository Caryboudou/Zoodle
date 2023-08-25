package com.kalzakath.zoodle

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

object Settings {
    object Default {
        const val moodMode: Int = MoodModes.NUMBERS
        const val fatigueMode: Int = FatigueModes.NUMBERS
        const val moodMax: Int = 5
        const val fatigueMax: Int = 5
        const val notificationTime = "20:00"
        const val notificationAct = false
        const val medicationName = "Ritaline"
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
    var notificationTime = Default.notificationTime
    var notificationAct = Default.notificationAct
    var medicationName = Default.medicationName

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
