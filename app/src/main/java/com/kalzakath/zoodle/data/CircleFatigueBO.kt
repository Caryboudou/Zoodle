package com.kalzakath.zoodle.data

import com.kalzakath.zoodle.R
import com.kalzakath.zoodle.data.MoodDO

enum class CircleFatigueBO(
    val colorId: Int,
    val backgroundId: Int,
    val fatigueDrawable: Int
) {
    VERY_GOOD(
        R.color.colorFatigueVeryGood,
        R.drawable.fatigue_circle_very_good,
        R.drawable.ic_very_good_fatigue
    ),
    GOOD(
        R.color.colorFatigueGood,
        R.drawable.fatigue_circle_good,
        R.drawable.ic_good_fatigue
    ),
    MEDIOCRE(
        R.color.colorFatigueMediocre,
        R.drawable.fatigue_circle_medicore,
        R.drawable.ic_mediocre_fatigue
    ),
    BAD(
        R.color.colorFatigueBad,
        R.drawable.fatigue_circle_bad,
        R.drawable.ic_bad_fatigue
    ),
    VERY_BAD(
        R.color.colorFatigueVeryBad,
        R.drawable.fatigue_circle_very_bad,
        R.drawable.ic_very_bad_fatigue
    ),
    NONE(
        R.color.colorMoodNone,
        R.drawable.mood_circle_none,
        R.drawable.ic_mood_none
    );

    fun toInt(): Int {
        return when (this) {
            VERY_GOOD -> 5
            GOOD -> 4
            MEDIOCRE -> 3
            BAD -> 2
            VERY_BAD -> 1
            NONE -> 0
        }
    }

    companion object {
        fun from(fatigue: MoodDO): CircleFatigueBO {
            return when (fatigue) {
                MoodDO.VERY_GOOD -> VERY_GOOD
                MoodDO.GOOD -> GOOD
                MoodDO.MEDIOCRE -> MEDIOCRE
                MoodDO.BAD -> BAD
                MoodDO.VERY_BAD -> VERY_BAD
                MoodDO.NONE -> NONE
            }
        }
        fun from(fatigue: Int) : CircleFatigueBO {
            return when (fatigue) {
                5 -> VERY_GOOD
                4 -> GOOD
                3 -> MEDIOCRE
                2 -> BAD
                1 -> VERY_BAD
                else -> NONE
            }
        }
    }
}