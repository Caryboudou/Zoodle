package com.niaouh.moodtracker.trackerpopup

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.niaouh.moodtracker.R

class TrackerPopup(private val context: Context,
                   private val finish: (ArrayList<String>) -> Unit) : Dialog(context) {
    private var dialog = Dialog(context)
    private lateinit var trackedList: ArrayList<String>

    fun showPopup(inputList: ArrayList<String>) {
        trackedList = inputList.clone() as ArrayList<String>
        val view = LayoutInflater.from(context).inflate(R.layout.tracker_popup, null, false)
        dialog.setContentView(view)
        dialog.create()
        initButton()
        dialog.show()
    }

    private fun initButton() {
        val rvTracked: RecyclerView = dialog.findViewById(R.id.rvTracked)
        val etAddTracker: EditText = dialog.findViewById(R.id.etAddTracker)
        val bAddTracker: Button = dialog.findViewById(R.id.bAddTracker)
        val bCreate: Button = dialog.findViewById(R.id.bCreate)
        val bCancel: Button = dialog.findViewById(R.id.bCancel)

        val adapter = TrackerRecycleViewAdaptor(trackedList)
        rvTracked.layoutManager = LinearLayoutManager(context)
        rvTracked.adapter = adapter

        bAddTracker.setOnClickListener {
            val newTracker = etAddTracker.text.toString()
            adapter.addTracker(newTracker)
        }

        bCreate.setOnClickListener {
            finish(trackedList)
            dialog.dismiss()
        }

        bCancel.setOnClickListener {
            dialog.dismiss()
        }
    }
}