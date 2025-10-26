package com.niaouh.moodtracker.interfaces

import com.niaouh.moodtracker.RecyclerViewAdaptor
import com.niaouh.moodtracker.model.MoodEntryModel

interface MainActivityInterface {
    fun startActivitySettings()
    fun startActivityFrontPage(moodEntry: MoodEntryModel?)
    fun startActivityTrendView()
    fun setupRecycleView()
}