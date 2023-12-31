package com.kalzakath.zoodle

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kalzakath.zoodle.model.MoodEntryModel

class FeelingsActivity: AppCompatActivity()  {

    private lateinit var feelingsRecycleView: RecyclerView
    private lateinit var selectedAdaptor: FeelingsRecycleViewAdaptor
    private lateinit var availableRecycleView: RecyclerView
    private lateinit var availableAdaptor: FeelingsRecycleViewAdaptor
    private lateinit var moodEntry: MoodEntryModel
    private var deleteMode = false
    private var availableFeelings: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.feelings_recycle_view)

        val bConfirm: Button = findViewById(R.id.bFeelingsConfirm)
        val bDelete: ImageButton = findViewById(R.id.delete)
        val bActivityAddNew: ImageButton = findViewById(R.id.bFeelingsAddNew)
        val arrayData = intent.getStringArrayListExtra("AvailableFeelings")
        val bActivityAdd: Button = findViewById(R.id.bActivityAdd)
        val bActivityCancel: Button = findViewById(R.id.bActivityCancel)
        val llAddActivity: LinearLayout = findViewById(R.id.llAddNewActivity)
        val etAddActivity: EditText = findViewById(R.id.etFeelingsAddNewTitle)
        val bDeleteActivity: Button = findViewById(R.id.bFeelingsDelete)
        val bRestoreDefaults: Button = findViewById(R.id.bFeelingsRestoreDefaults)

        moodEntry = intent.getSerializableExtra("MoodEntry") as MoodEntryModel

        if (arrayData != null) {
            availableFeelings.addAll(arrayData)
        }
        // Remove accidental duplicates
        for (item in moodEntry.feelings) {
            availableFeelings.remove(item)
        }

        feelingsRecycleView = findViewById(R.id.rvSelected)
        feelingsRecycleView.layoutManager = LinearLayoutManager(this)
        selectedAdaptor = FeelingsRecycleViewAdaptor(applicationContext, moodEntry.feelings,
            { feeling -> moveToAvailable(feeling) },
            { feeling -> removeFromAvailable(feeling)})

        feelingsRecycleView.adapter = selectedAdaptor

        availableRecycleView = findViewById(R.id.rvAvailable)
        availableRecycleView.layoutManager = LinearLayoutManager(this)
        availableAdaptor =
            FeelingsRecycleViewAdaptor(applicationContext, availableFeelings.toMutableList(),
                { feeling -> moveToSelected(feeling) },
                { feeling -> removeFromAvailable(feeling)})

        availableRecycleView.adapter = availableAdaptor

        if (availableFeelings.size == 0) {
            restoreDefaults()
        }

        bConfirm.setOnClickListener {
            val finishIntent = Intent()
            finishIntent.putExtra("MoodEntry", moodEntry)
            finishIntent.putStringArrayListExtra("AvailableFeelings", availableFeelings)
            setResult(RESULT_OK, finishIntent)
            finish()
        }

        bDelete.setOnClickListener {
            finish()
        }

        bDeleteActivity.setOnClickListener {
            deleteMode = !deleteMode
            selectedAdaptor.toggleDeleteMode(deleteMode)
            availableAdaptor.toggleDeleteMode(deleteMode)

            if (deleteMode) {
                feelingsRecycleView.background.setTint(Color.RED)
                availableRecycleView.background.setTint(Color.RED)
            } else {
                feelingsRecycleView.background.setTint(resources.getColor(R.color.dark_gray, theme))
                availableRecycleView.background.setTint(resources.getColor(R.color.dark_gray, theme))
            }
        }

        bActivityAddNew.setOnClickListener {
            llAddActivity.visibility = View.VISIBLE
        }

        bActivityCancel.setOnClickListener {
            llAddActivity.visibility = View.INVISIBLE
            etAddActivity.setText("")
        }

        bActivityAdd.setOnClickListener {
            selectedAdaptor.addItem(etAddActivity.text.toString())

            llAddActivity.visibility = View.INVISIBLE
            etAddActivity.setText("")
        }

        bDeleteActivity.setOnClickListener {
            deleteMode = !deleteMode
            selectedAdaptor.toggleDeleteMode(deleteMode)
            availableAdaptor.toggleDeleteMode(deleteMode)

            if (deleteMode) {
                feelingsRecycleView.background.setTint(Color.RED)
                availableRecycleView.background.setTint(Color.RED)
            } else {
                feelingsRecycleView.background.setTint(resources.getColor(R.color.dark_gray, theme))
                availableRecycleView.background.setTint(resources.getColor(R.color.dark_gray, theme))
            }
        }

        bRestoreDefaults.setOnClickListener {
            restoreDefaults()
        }
    }

    private fun removeFromAvailable(activity: String) {
        availableFeelings.remove(activity)
    }

    private fun moveToSelected(activity: String) {
        selectedAdaptor.run {
            addItem(activity)
        }
    }

    private fun moveToAvailable(activity: String) {
        availableAdaptor.run {
            addItem(activity)
        }
    }

    private fun restoreDefaults() {
        val stringArray =
            applicationContext.resources.getStringArray(R.array.available_feelings)
        for (activity in stringArray) {
            availableAdaptor.addItem(activity)
            if (!availableFeelings.contains(activity)) availableFeelings.add(activity)
        }

    }
}