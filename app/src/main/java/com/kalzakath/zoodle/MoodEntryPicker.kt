package com.kalzakath.zoodle

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import com.kalzakath.zoodle.layout.ChooseFatigueCircle
import com.kalzakath.zoodle.layout.ChooseMoodCircle
import com.kalzakath.zoodle.model.MoodEntryModel
import com.kalzakath.zoodle.utils.ResUtil.getDateStringEN
import com.kalzakath.zoodle.utils.ResUtil.getDateStringFR
import com.kalzakath.zoodle.utils.ResUtil.getTimeStringEN
import com.kalzakath.zoodle.utils.ResUtil.getTimeStringFR
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MoodEntryPicker(context: Context, val onCreateMoodEntry: (MoodEntryModel) -> Unit) : Dialog(context) {

    private var dialog = Dialog(context)

    private fun initButtons() {
        val bmpCancel = dialog.findViewById<Button>(R.id.bmpCancel)
        val bmpCreate = dialog.findViewById<Button>(R.id.bmpCreate)
        val resetMood = dialog.findViewById<TextView>(R.id.tvmpMoodTitle)
        val resetFatigue = dialog.findViewById<TextView>(R.id.tvmpFatigueTitle)

        resetMood.setOnClickListener {
            resetMoodEntryMood()
        }

        resetFatigue.setOnClickListener {
            resetFatigueEntryMood()
        }

        bmpCancel.setOnClickListener {
            dialog.dismiss()
        }

        bmpCreate.setOnClickListener {
            onCreateMoodEntry(createNewMoodEntry())
            dialog.dismiss()
        }
    }

    private fun createNewMoodEntry(): MoodEntryModel {
        val date: TextView = dialog.findViewById(R.id.tvmpDateValue)
        val time: TextView = dialog.findViewById(R.id.tvmpTimeValue)
        val mood: ChooseMoodCircle = dialog.findViewById(R.id.tvmpMoodValue)
        val fatigue: ChooseFatigueCircle = dialog.findViewById(R.id.tvmpFatigueValue)

        return MoodEntryModel(
            getDateStringEN(date.text.toString()),
            getTimeStringEN(time.text.toString()),
            mood.toInt(),
            fatigue.toInt()
        )
    }

    private fun resetMoodEntryMood() {
        val mood: ChooseMoodCircle = dialog.findViewById(R.id.tvmpMoodValue)
        mood.reset()
    }

    private fun resetFatigueEntryMood() {
        val fatigue: ChooseFatigueCircle = dialog.findViewById(R.id.tvmpFatigueValue)
        fatigue.reset()
    }

    private fun loadDefaults() {
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date: TextView = dialog.findViewById(R.id.tvmpDateValue)
        val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
        val time: TextView = dialog.findViewById(R.id.tvmpTimeValue)

        date.text = getDateStringFR(dateFormat.format(LocalDate.now()))
        time.text = getTimeStringFR(timeFormat.format(LocalTime.now()))
    }

    fun showPopup() {
        val view = LayoutInflater.from(context).inflate(R.layout.mood_picker, null, false)
        dialog.setContentView(view)
        dialog.create()

        loadDefaults()
        initButtons()
        dialog.show()
    }
}
