package com.niaouh.moodtracker

import android.graphics.Color
import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.niaouh.moodtracker.model.MoodEntryModel
import com.niaouh.moodtracker.utils.ResUtil.getDateStringFR

class TrackerActivity: AppCompatActivity() {

    private var moodData = ArrayList<MoodEntryModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker)

        val securityHandler = SecurityHandler(applicationContext)
        val secureFileHandler = SecureFileHandler(securityHandler)

        val jsonString = secureFileHandler.read()
        if (jsonString != "") moodData = getMoodListFromJSON(jsonString)

        val ibTrackerConfirm: ImageButton = findViewById(R.id.ibTrackerConfirm)

        initTable()

        ibTrackerConfirm.setOnClickListener {
            finish()
        }
    }

    private fun initTable() {
        val table: TableLayout = findViewById(R.id.table_main)
        val tableHeader: TableLayout = findViewById(R.id.table_header)
        table.removeAllViews()
        tableHeader.removeAllViews()
        val header = TableRow(this)
        val newTV = TextView(this)
        newTV.setTextColor(Color.BLACK)
        newTV.text = "2020-11-29"
        header.addView(newTV)
        for (t in Settings.trackerList) {
            val newTVhead = TextView(this)
            newTVhead.setTextColor(Color.WHITE)
            newTVhead.width = 200
            newTVhead.text = t
            header.addView(newTVhead)
        }
        tableHeader.addView(header)
        for (m in moodData) {
            val newRow = TableRow(this)
            val date = TextView(this)
            date.setTextColor(Color.WHITE)
            date.text = getDateStringFR(m.date)
            newRow.addView(date)
            for (t in Settings.trackerList) {
                val checkTrack = CheckBox(this)
                checkTrack.width = 200
                if (m.trackers.contains(t)) checkTrack.isChecked = true
                newRow.addView(checkTrack)
            }
            table.addView(newRow)
        }
    }

    private fun getMoodListFromJSON(jsonString: String): ArrayList<MoodEntryModel> {
        val moodList = ArrayList<MoodEntryModel>()

        if (jsonString.isNotEmpty()) {
            val gson = GsonBuilder().create()
            val type = object: TypeToken<Array<MoodEntryModel>>() {}.type
            val moodEntries = gson.fromJson<Array<MoodEntryModel>>(jsonString, type)

            for(x in moodEntries.indices) {
                moodList.add(moodEntries[x])
            }
        }

        return moodList
    }
}