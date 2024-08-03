package com.niaouh.moodtracker.interfaces

import com.niaouh.moodtracker.MoodTrackerMain
import com.niaouh.moodtracker.RecyclerViewAdaptor
import com.niaouh.moodtracker.RowControllerEvent
import java.time.LocalDate
import java.util.Date

interface DataController: DataControllerAccessors, DataControllerEventHandlers

interface DataControllerEventHandlers {
    fun registerForUpdates(listener: DataControllerEventListener)
    fun unregisterForUpdates(listener: DataControllerEventListener)
}

interface DataControllerEventListener {
    fun onUpdateFromDataController(event: RowControllerEvent)
}

interface DataControllerAccessors {
    var mainRowEntryList: ArrayList<RowEntryModel>

    fun add(rowEntryModel: RowEntryModel, callUpdate: Boolean = true)
    fun add(rowEntryList: ArrayList<RowEntryModel>)

    fun remove(rowEntryModel: RowEntryModel, callUpdate: Boolean = true)
    fun remove(rowEntryList: ArrayList<RowEntryModel>)
    fun removeAt(position: Int, callUpdate: Boolean = true)

    fun update(rowEntryModel: RowEntryModel, callUpdate: Boolean = true)
    fun update(updateRowEntryList: ArrayList<RowEntryModel>, callUpdate: Boolean = true)
    fun updateAt(position: Int, rowEntryModel: RowEntryModel, callUpdate: Boolean = true): Boolean

    fun size(): Int

    fun indexOf(rowEntryModel: RowEntryModel): Int
    fun get(position: Int): RowEntryModel

    fun <T> findDate(condition: T): RowEntryModel?

    fun findFirst(date: Date): Int
}