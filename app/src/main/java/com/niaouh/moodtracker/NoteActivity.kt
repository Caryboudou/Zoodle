package com.niaouh.moodtracker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.niaouh.moodtracker.model.MoodEntryModel
import com.niaouh.moodtracker.model.getRitalineInt
import com.niaouh.moodtracker.utils.ResUtil.getDateStringFR
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class NoteActivity: AppCompatActivity()  {

    private lateinit var moodEntry: MoodEntryModel

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.note_recycle_view)

        val data = intent.getSerializableExtra("MoodEntry")
        moodEntry = if (data != null) data as MoodEntryModel
            else prepMoodEntry()

        val bConfirm: ImageButton = findViewById(R.id.bConfirm)
        val tvTitle: TextView = findViewById(R.id.tvNoteTitle)
        val ibCancel: Button = findViewById(R.id.delete)
        val etAddNote: EditText = findViewById(R.id.etAddNote)
        val etRitaline: EditText = findViewById(R.id.etRitaline)
        val tvRitaline: TextView = findViewById(R.id.tvRitaline)
        val line: View = findViewById(R.id.line2)

        val newText = applicationContext.resources.getString(R.string.note_title) + getDateStringFR(moodEntry.date)
        tvTitle.text = newText
        etAddNote.setText(moodEntry.note)
        etRitaline.setText(moodEntry.ritaline)
        val str = "${Settings.medicationName} (${moodEntry.getRitalineInt()}mg) :"
        tvRitaline.text = str

        if (Settings.medicationName == "") {
            tvRitaline.visibility = View.GONE
            etRitaline.visibility = View.GONE
            line.visibility = View.GONE
        }
        else {
            tvRitaline.visibility = View.VISIBLE
            etRitaline.visibility = View.VISIBLE
            line.visibility = View.VISIBLE
        }

        bConfirm.setOnClickListener {
            val finishIntent = Intent()
            moodEntry.note = etAddNote.text.toString()
            moodEntry.ritaline = etRitaline.text.toString()
            finishIntent.putExtra("MoodEntry", moodEntry)
            setResult(RESULT_OK, finishIntent)
            finish()
        }

        ibCancel.setOnClickListener {
            finish()
        }
    }

    private fun prepMoodEntry(): MoodEntryModel {
        val timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        return MoodEntryModel(dateFormat.format(LocalDate.now()), timeFormat.format(LocalDateTime.now()))
    }
}