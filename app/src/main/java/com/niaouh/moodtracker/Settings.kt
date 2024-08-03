package com.niaouh.moodtracker

import java.time.LocalTime
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
        val notificationTime: LocalTime = LocalTime.of(20,0)
        const val notificationAct = false
        const val medicationName = "Ritaline"
        val trackerList: ArrayList<String> = arrayListOf()
        const val trackerMode = false
        val notificationList: ArrayList<LocalTime> = arrayListOf()
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
    var notificationTime: LocalTime = Default.notificationTime
    var notificationAct = Default.notificationAct
    var medicationName = Default.medicationName
    var trackerList = Default.trackerList
    var trackerMode = Default.trackerMode
    var notificationList: ArrayList<LocalTime> = Default.notificationList

    fun setDefaultSettings() {
        var i = 0
        var j = 0
        for (prop in Settings::class.memberProperties) {
            i = i + 1
            val defaultVal =
                Default::class.java.declaredFields.find { it.name == prop.name }
            if (defaultVal != null) {
                if (prop is KMutableProperty<*>) {
                    j += 1
                    prop.setter.call(Settings, defaultVal.get(Default))
                }
            }
        }
    }
}

class SettingsToJson (
    val moodMode: Int = Settings.MoodModes.FACES,
    val fatigueMode: Int = Settings.FatigueModes.FACES,
    val modeNote: Boolean = false,
    val moodMax: Int = 5,
    val fatigueMax: Int = 5,
    val notificationTimeHours: Int = 20,
    val notificationTimeMinutes: Int = 0,
    val notificationAct: Boolean = false,
    val medicationName: String = "Ritaline",
    val trackerList: ArrayList<String> = arrayListOf(),
    val trackerMode: Boolean = false,
    val notificationList: ArrayList<Pair<Int,Int>> = arrayListOf()
)