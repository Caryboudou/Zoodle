package com.niaouh.moodtracker

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.niaouh.moodtracker.interfaces.RowEntryModel
import com.niaouh.moodtracker.model.MoodEntryModel
import com.niaouh.moodtracker.model.MoodEntryModelToJson
import com.niaouh.moodtracker.model.createNewEntry
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Logger
import kotlin.random.Random

open class DataHandler(var secureFileHandler: SecureFileHandler,
                       private var context: Context
) {
    private val log = Logger.getLogger(MainActivity::class.java.name + "DataHandler")

     open fun read(): ArrayList<RowEntryModel> {
        val jsonArray = secureFileHandler.read()
        val moodData = ArrayList<RowEntryModel>()
        log.info("JsonArray found: ${jsonArray}")

        if (jsonArray.isNotEmpty()) {
            val gson = GsonBuilder().create()
            val type = object : TypeToken<Array<MoodEntryModelToJson>>() {}.type
            val moodEntries = gson.fromJson<Array<MoodEntryModelToJson>>(jsonArray, type)
            if (moodEntries.isEmpty()) return moodData

            for (x in moodEntries.indices) {
                moodData.add(moodEntries[x].MoodEntryModel())
            }
        }

        return moodData
    }

    open fun createNewMoodEntry(dateTimeNow: LocalDateTime = LocalDateTime.now()): MoodEntryModel {
        val random = Random(System.currentTimeMillis())

        val choices: MutableList<String> = ArrayList()
        choices.add("Programming")
        choices.add("Gaming")
        choices.add("Reading")
        choices.add("Going out")
        choices.add("School")
        choices.add("Rugby")
        choices.add("DnD")
        choices.add("Hanging out")

        val availFeelings = context.resources.getStringArray(R.array.available_feelings)

        val list: MutableList<String> = ArrayList()
        for (i in 1..random.nextInt(4)) {
            list.add(choices[random.nextInt(0, choices.size - 1)])
        }

        val feelings: MutableList<String> = ArrayList()
        for (i in 1..random.nextInt(4)) {
            feelings.add(availFeelings[random.nextInt(0, availFeelings.size - 1)])
        }

        return MoodEntryModel(
                dateTimeNow,
                3,
                3,
                "",
            "",
                UUID.randomUUID().toString()
            )
    }
}
