package com.niaouh.moodtracker

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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.niaouh.moodtracker.layout.ChooseFatigueCircle
import com.niaouh.moodtracker.layout.ChooseMoodCircle
import com.niaouh.moodtracker.model.MoodEntryModel
import com.niaouh.moodtracker.model.updateDateOnly
import com.niaouh.moodtracker.model.updateTime
import com.niaouh.moodtracker.trackerpopup.TrackerMainRecycleViewAdaptor
import com.niaouh.moodtracker.utils.ResUtil.getDateStringFR
import com.niaouh.moodtracker.utils.ResUtil.getTimeStringFR
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
    private val trackerListMood = arrayListOf<Pair<String,Boolean>>()

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

        val llTracker: LinearLayout = findViewById(R.id.llTracker)
        val rvTracker: RecyclerView = findViewById(R.id.rvTracker)

        val etNote: EditText = findViewById(R.id.etFrontNote)
        val etRitaline: EditText = findViewById(R.id.etFrontRitaline)
        val tvFrontRitalineTitle: TextView = findViewById(R.id.tvFrontRitalineTitle)

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
            tvFrontRitalineTitle.text = Settings.medicationName
            llRitaline.visibility = View.VISIBLE
        }

        if (Settings.trackerList.isEmpty()) llTracker.visibility = View.GONE
        else {
            llTracker.visibility = View.VISIBLE
            for (t in Settings.trackerList) {
                if (moodEntry.trackers.contains(t))
                    trackerListMood.add(Pair(t,true))
                else trackerListMood.add(Pair(t,false))
            }
            val adapter = TrackerMainRecycleViewAdaptor(trackerListMood)
            rvTracker.layoutManager = GridLayoutManager(this,2)
            rvTracker.adapter = adapter
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
            dtPickerDate.show(this, moodEntry.date)
        }

        time.setOnClickListener {
            dtPickerTime.show(this, moodEntry.time)
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
            for (t in trackerListMood) {
                if (!t.second) moodEntry.trackers.remove(t.first)
                else {
                    if (!moodEntry.trackers.contains(t.first)) moodEntry.trackers.add(t.first)
                }
            }
            intent.putExtra("MoodEntry", moodEntry)
            setResult(RESULT_OK, intent)
            finish()
        }

        close.setOnClickListener {
            finish()
        }
    }
}