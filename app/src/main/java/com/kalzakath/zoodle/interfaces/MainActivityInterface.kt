package com.kalzakath.zoodle.interfaces

import com.kalzakath.zoodle.RecyclerViewAdaptor
import com.kalzakath.zoodle.model.MoodEntryModel

interface MainActivityInterface {
    fun startActivitySettings()
    fun startActivityFrontPage(moodEntry: MoodEntryModel?)
    fun startActivityTrendView()
    fun setupRecycleView(): RecyclerViewAdaptor
}