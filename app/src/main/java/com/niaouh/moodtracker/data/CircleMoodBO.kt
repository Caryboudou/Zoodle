package com.niaouh.moodtracker.data

import android.graphics.drawable.LayerDrawable
import com.niaouh.moodtracker.R
import com.niaouh.moodtracker.data.MoodDO

enum class CircleMoodBO(
    val colorId: Int,
    val backgroundId: Int,
    val moodDrawable: Int
) {
    VERY_GOOD(
        R.color.colorMoodVeryGood,
        R.drawable.mood_circle_very_good,
        R.drawable.ic_very_good
    ),
    VERY_GOOD_H(
        R.color.colorMoodVeryGood1,
        R.drawable.mood_circle_very_good_1,
        R.drawable.ic_very_good_1
    ),
    GOOD(
        R.color.colorMoodGood,
        R.drawable.mood_circle_good,
        R.drawable.ic_good
    ),
    GOOD_H(
        R.color.colorMoodGood1,
        R.drawable.mood_circle_good_1,
        R.drawable.ic_good_1
    ),
    MEDIOCRE(
        R.color.colorMoodMediocre,
        R.drawable.mood_circle_medicore,
        R.drawable.ic_mediocre
    ),
    MEDIOCRE_H(
        R.color.colorMoodMediocre1,
        R.drawable.mood_circle_medicore_1,
        R.drawable.ic_mediocre_1
    ),
    BAD(
        R.color.colorMoodBad,
        R.drawable.mood_circle_bad,
        R.drawable.ic_bad
    ),
    BAD_H(
        R.color.colorMoodBad1,
        R.drawable.mood_circle_bad_1,
        R.drawable.ic_bad_1
    ),
    VERY_BAD(
        R.color.colorMoodVeryBad,
        R.drawable.mood_circle_very_bad,
        R.drawable.ic_very_bad
    ),
    NONE(
        R.color.colorMoodNone,
        R.drawable.mood_circle_none,
        R.drawable.ic_unknow
    );

    fun toInt(): Int {
        return when (this) {
            VERY_GOOD -> 9
            VERY_GOOD_H -> 8
            GOOD -> 7
            GOOD_H -> 6
            MEDIOCRE -> 5
            MEDIOCRE_H -> 4
            BAD -> 3
            BAD_H -> 2
            VERY_BAD -> 1
            NONE -> 0
        }
    }

    fun toText(max5: Boolean): String {
        if (max5)
            return "${when (this) {
                VERY_GOOD -> 5
                VERY_GOOD_H -> 5
                GOOD -> 4
                GOOD_H -> 4
                MEDIOCRE -> 3
                MEDIOCRE_H -> 2
                BAD -> 2
                BAD_H -> 1
                VERY_BAD -> 1
                NONE -> 0
            }}/5"
        return "${when (this) {
            VERY_GOOD -> 9
            VERY_GOOD_H -> 8
            GOOD -> 7
            GOOD_H -> 6
            MEDIOCRE -> 5
            MEDIOCRE_H -> 4
            BAD -> 3
            BAD_H -> 2
            VERY_BAD -> 1
            NONE -> 0
        }}/9"
    }

    companion object {
        fun from(mood: MoodDO): CircleMoodBO {
            return when (mood) {
                MoodDO.VERY_GOOD -> VERY_GOOD
                MoodDO.VERY_GOOD_H -> VERY_GOOD_H
                MoodDO.GOOD -> GOOD
                MoodDO.GOOD_H -> GOOD_H
                MoodDO.MEDIOCRE -> MEDIOCRE
                MoodDO.MEDIOCRE_H -> MEDIOCRE_H
                MoodDO.BAD -> BAD
                MoodDO.BAD_H -> BAD_H
                MoodDO.VERY_BAD -> VERY_BAD
                MoodDO.NONE -> NONE
            }
        }
        fun from(mood: Int) : CircleMoodBO {
            return when (mood) {
                9 -> VERY_GOOD
                8 -> VERY_GOOD_H
                7 -> GOOD
                6 -> GOOD_H
                5 -> MEDIOCRE
                4 -> MEDIOCRE_H
                3 -> BAD
                2 -> BAD_H
                1 -> VERY_BAD
                else -> NONE
            }
        }
    }
}