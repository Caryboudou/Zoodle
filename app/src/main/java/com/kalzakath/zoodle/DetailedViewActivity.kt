package com.kalzakath.zoodle

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kalzakath.zoodle.layout.ChooseFatigueCircle
import com.kalzakath.zoodle.layout.ChooseMoodCircle
import com.kalzakath.zoodle.model.MoodEntryModel
import com.kalzakath.zoodle.model.updateDateOnly
import com.kalzakath.zoodle.model.updateTime
import com.kalzakath.zoodle.utils.ResUtil.getDateStringFR
import com.kalzakath.zoodle.utils.ResUtil.getTimeStringFR
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Logger


class DetailedViewActivity : AppCompatActivity() {
    private lateinit var secureFileHandler: SecureFileHandler
    private lateinit var securityHandler: SecurityHandler
    private val log = Logger.getLogger(MainActivity::class.java.name + "****************************************")

    private lateinit var moodEntry: MoodEntryModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_front_page)

        securityHandler = SecurityHandler(applicationContext)
        secureFileHandler = SecureFileHandler(securityHandler)

        val data = intent.getSerializableExtra("MoodEntry")
        moodEntry = if (data != null) data as MoodEntryModel
            else prepMoodEntry()

        initButtons()
    }

    private fun prepMoodEntry(): MoodEntryModel {
        val timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        return MoodEntryModel(dateFormat.format(LocalDate.now()), timeFormat.format(LocalDateTime.now()))
    }

    private fun initButtons() {
        val numberPickerMood: ChooseMoodCircle = findViewById(R.id.tvmpFrontMoodValue)
        val numberPickerFatigue: ChooseFatigueCircle = findViewById(R.id.tvmpFrontFatigueValue)
        val resetMood : TextView = findViewById(R.id.tvFrontMoodTitle)
        val resetFatigue : TextView = findViewById(R.id.tvFrontFatigueTitle)
        val llRitaline: LinearLayout = findViewById(R.id.llRitaline)

        val etNote: EditText = findViewById(R.id.etFrontNote)
        val etRitaline: EditText = findViewById(R.id.etFrontRitaline)

        val mainActivity: ImageButton = findViewById(R.id.ibFrontSeeData)
        val close: ImageButton = findViewById(R.id.ibClose)

        val date: TextView = findViewById(R.id.tvFrontDate)
        val time: TextView = findViewById(R.id.tvFrontTime)

        date.text = getDateStringFR(moodEntry.date)
        time.text = getTimeStringFR(moodEntry.time)
        etNote.setText(moodEntry.note)
        etRitaline.setText(moodEntry.ritaline)
        numberPickerMood.setSelected(moodEntry)
        numberPickerFatigue.setSelected(moodEntry)

        if (Settings.medicationName == "") {
            llRitaline.visibility = View.GONE
        }
        else {
            llRitaline.visibility = View.VISIBLE
        }

        val dtPickerDate = DatePicker()
        dtPickerDate.onUpdateListener = {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            date.text = getDateStringFR(dateFormat.format(it.time))
            moodEntry.updateDateOnly(it)
        }

        val dtPickerTime = TimePicker()
        dtPickerTime.onUpdateListener = {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            time.text = getTimeStringFR(timeFormat.format(it.time))
            moodEntry.updateTime(it)
        }

        date.setOnClickListener {
            dtPickerDate.show(this)
        }

        time.setOnClickListener {
            dtPickerTime.show(this)
        }

        resetMood.setOnClickListener {
            numberPickerMood.reset()
        }

        resetFatigue.setOnClickListener {
            numberPickerFatigue.reset()
        }

        etRitaline.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                etNote.requestFocus()
                true
            } else false
        }

        mainActivity.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            moodEntry.mood = numberPickerMood.toInt()
            moodEntry.fatigue = numberPickerFatigue.toInt()
            moodEntry.note = etNote.text.toString()
            moodEntry.ritaline = etRitaline.text.toString()
            intent.putExtra("MoodEntry", moodEntry)
            setResult(RESULT_OK, intent)
            finish()
        }

        close.setOnClickListener {
            finish()
        }
    }
}