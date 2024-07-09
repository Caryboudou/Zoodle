package com.niaouh.moodtracker.data

import com.niaouh.moodtracker.data.CircleMoodBO

enum class MoodDO {
    VERY_GOOD,
    VERY_GOOD_H,
    GOOD,
    GOOD_H,
    MEDIOCRE,
    MEDIOCRE_H,
    BAD,
    BAD_H,
    VERY_BAD,
    NONE;

    companion object {
        fun from(mood: CircleMoodBO) : MoodDO {
            return when (mood) {
                CircleMoodBO.VERY_GOOD -> VERY_GOOD
                CircleMoodBO.VERY_GOOD_H -> VERY_GOOD_H
                CircleMoodBO.GOOD -> GOOD
                CircleMoodBO.GOOD_H -> GOOD_H
                CircleMoodBO.MEDIOCRE -> MEDIOCRE
                CircleMoodBO.MEDIOCRE_H -> MEDIOCRE_H
                CircleMoodBO.BAD -> BAD
                CircleMoodBO.BAD_H -> BAD_H
                CircleMoodBO.VERY_BAD -> VERY_BAD
                CircleMoodBO.NONE -> NONE
            }
        }
    }
}
