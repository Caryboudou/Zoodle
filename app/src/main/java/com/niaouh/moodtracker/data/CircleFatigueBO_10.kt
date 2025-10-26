package com.niaouh.moodtracker.data

import com.niaouh.moodtracker.R
import com.niaouh.moodtracker.data.MoodDO

enum class CircleFatigueBO_10(
    val colorId: Int,
    val backgroundId: Int,
    val fatigueDrawable: Int
) {
    FATIGUE_10(
        R.color.colorFatigueVeryGood,
        R.drawable.fatigue_circle_very_good,
        R.drawable.ic_very_good_fatigue
    ),
    FATIGUE_9(
        R.color.colorFatigueVeryGood1,
        R.drawable.fatigue_circle_very_good_1,
        R.drawable.ic_very_good_fatigue_1
    ),
    FATIGUE_8(
        R.color.colorFatigueGood,
        R.drawable.fatigue_circle_good,
        R.drawable.ic_good_fatigue
    ),
    FATIGUE_7(
        R.color.colorFatigueGood1,
        R.drawable.fatigue_circle_good_1,
        R.drawable.ic_good_fatigue_1
    ),
    FATIGUE_6(
        R.color.colorFatigueMediocre,
        R.drawable.fatigue_circle_medicore,
        R.drawable.ic_mediocre_fatigue
    ),
    FATIGUE_5(
        R.color.colorFatigueMediocre1,
        R.drawable.fatigue_circle_medicore_1,
        R.drawable.ic_mediocre_fatigue_1
    ),
    FATIGUE_4(
        R.color.colorFatigueBad,
        R.drawable.fatigue_circle_bad,
        R.drawable.ic_bad_fatigue
    ),
    FATIGUE_3(
        R.color.colorFatigueBad1,
        R.drawable.fatigue_circle_bad_1,
        R.drawable.ic_bad_fatigue_1
    ),
    FATIGUE_2(
        R.color.colorFatigueVeryBad,
        R.drawable.fatigue_circle_very_bad,
        R.drawable.ic_very_bad_fatigue
    ),
    FATIGUE_1(
        R.color.colorFatigueVeryBad1,
        R.drawable.fatigue_circle_very_bad_1,
        R.drawable.ic_very_bad_fatigue_1
    ),
    NONE(
        R.color.colorMoodNone,
        R.drawable.mood_circle_none,
        R.drawable.ic_unknow
    );

    fun toInt(): Int {
        return when (this) {
            FATIGUE_10 -> 10
            FATIGUE_9 -> 9
            FATIGUE_8 -> 8
            FATIGUE_7 -> 7
            FATIGUE_6 -> 6
            FATIGUE_5 -> 5
            FATIGUE_4 -> 4
            FATIGUE_3 -> 3
            FATIGUE_2 -> 2
            FATIGUE_1 -> 1
            NONE -> 0
        }
    }

    companion object {
        fun from(fatigue: Int) : CircleFatigueBO_10 {
            return when (fatigue) {
                10 -> FATIGUE_10
                9 -> FATIGUE_9
                8 -> FATIGUE_8
                7 -> FATIGUE_7
                6 -> FATIGUE_6
                5 -> FATIGUE_5
                4 -> FATIGUE_4
                3 -> FATIGUE_3
                2 -> FATIGUE_2
                1 -> FATIGUE_1
                else -> NONE
            }
        }
    }
}