package com.kalzakath.zoodle

import kotlin.math.ceil

class MoodValueHelper {
     fun getSanitisedNumber(value: Int, max: Int): Int {
        return (ceil(
            value.toDouble().div(max / 5)
        ).toInt())
    }

     fun getUnsanitisedNumber(value: Int, max: Int): Int {
        return value.times(max/ 5)
    }


     fun getEmojiMood(convertValue: String): Int {
        return when (convertValue) {
            "Ecstatic" -> R.string.mood_ecstatic
            "Happy" -> R.string.mood_happy
            "Unhappy" -> R.string.mood_unhappy
            "Terrible" -> R.string.mood_terrible
            else -> R.string.mood_average
        }
    }

    fun getEmojiFatigue(convertValue: String): Int {
        return when (convertValue) {
            "Ecstatic" -> R.string.fatigue_ecstatic
            "Happy" -> R.string.fatigue_happy
            "Unhappy" -> R.string.fatigue_unhappy
            "Terrible" -> R.string.fatigue_terrible
            else -> R.string.fatigue_average
        }
    }
}