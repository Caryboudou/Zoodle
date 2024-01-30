package com.niaouh.moodtracker.trackerpopup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.niaouh.moodtracker.R
import com.niaouh.moodtracker.interfaces.DataController
import com.niaouh.moodtracker.interfaces.RowEntryModel
import java.util.*

class TrackerMainRecycleViewAdaptor(private val trackerList: ArrayList<Pair<String, Boolean>>
): Adapter<TrackerMainRecycleViewAdaptor.TrackerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackerViewHolder {
        val viewHolder: View = LayoutInflater.from(parent.context).inflate(R.layout.tracker_main_entry_layout, parent, false)
        return TrackerViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return trackerList.size
    }

    override fun onBindViewHolder(holder: TrackerViewHolder, position: Int) {
        holder.text.text = trackerList[position].first
        holder.text.isChecked = trackerList[position].second
        holder.text.setOnCheckedChangeListener {_, isChecked ->
            trackerList[position] = trackerList[position].copy(second = isChecked)
        }
    }

    class TrackerViewHolder(itemView: View) : ViewHolder(itemView) {
        val text: CheckBox = itemView.findViewById(R.id.tvText)
    }
}