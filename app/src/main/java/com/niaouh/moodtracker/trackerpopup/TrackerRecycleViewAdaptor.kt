package com.niaouh.moodtracker.trackerpopup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.niaouh.moodtracker.R
import java.util.*

class TrackerRecycleViewAdaptor(private val trackerList: ArrayList<String>): Adapter<TrackerRecycleViewAdaptor.TrackerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackerViewHolder {
        val viewHolder: View = LayoutInflater.from(parent.context).inflate(R.layout.tracker_entry_layout, parent, false)
        return TrackerViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return trackerList.size
    }

    override fun onBindViewHolder(holder: TrackerViewHolder, position: Int) {
        holder.text.text = trackerList[position]
        holder.text.setOnClickListener {
            removeTrack(holder)
        }
    }

    private fun removeTrack(t: TrackerViewHolder) {
        val index = trackerList.indexOf(t.text.text)
        if (index!=-1) {
            trackerList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun addTracker(tracked: String) {
        if (trackerList.contains(tracked)) return
        trackerList.add(tracked)
        notifyItemInserted(itemCount - 1)
    }

    class TrackerViewHolder(itemView: View) : ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.tvText)
    }
}