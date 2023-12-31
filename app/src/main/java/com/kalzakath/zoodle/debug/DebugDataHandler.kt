package com.kalzakath.zoodle.debug

import android.content.Context
import com.kalzakath.zoodle.DataHandler
import com.kalzakath.zoodle.R
import com.kalzakath.zoodle.SecureFileHandler
import com.kalzakath.zoodle.interfaces.RowEntryModel
import com.kalzakath.zoodle.model.MoodEntryModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random

class DebugDataHandler (secureFileHandler: SecureFileHandler,
                        private var context: Context
): DataHandler(secureFileHandler, context) {

    override fun read(): ArrayList<RowEntryModel> {
        val debugArrayList = arrayListOf<RowEntryModel>()
        for (i in 1..512) {
            debugArrayList.add(createNewMoodEntry())
       }
        val dateTimeNow = LocalDateTime.now()
        debugArrayList.add(MoodEntryModel(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(dateTimeNow)))

        secureFileHandler.write(debugArrayList)

        return super.read()
    }

    private fun createNewMoodEntry(): MoodEntryModel {
        val random = Random
        val randomYear = random.nextInt(2010, 2021).toString()
        var randMonth = random.nextInt(1, 12).toString()
        if (randMonth.toInt() < 10) randMonth = "0$randMonth"
        var randDay = random.nextInt(1, 28).toString()
        if (randDay.toInt() < 10) randDay = "0$randDay"
        val randMood = random.nextInt(1, 5)
        val randFatigue = random.nextInt(1, 5)

        val availFeelings = context.resources.getStringArray(R.array.available_feelings)

        val choices: MutableList<String> = ArrayList()
        choices.add("Programming")
        choices.add("Gaming")
        choices.add("Reading")
        choices.add("Going out")
        choices.add("School")
        choices.add("Rugby")
        choices.add("DnD")
        choices.add("Hanging out")

        val list: MutableList<String> = ArrayList()
        for (i in 1..random.nextInt(4)) {
            list.add(choices[random.nextInt(0, choices.size - 1)])
        }

        val feelings: MutableList<String> = ArrayList()
        for (i in 1..random.nextInt(4)) {
            feelings.add(availFeelings[random.nextInt(0, availFeelings.size - 1)])
        }

        return MoodEntryModel(
            "$randomYear-$randMonth-$randDay",
            "12:34",
            randMood,
            randFatigue,
            feelings,
            list,
            "test_" + UUID.randomUUID().toString()
        )
    }
}
