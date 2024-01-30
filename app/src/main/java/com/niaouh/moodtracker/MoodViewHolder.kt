package com.niaouh.moodtracker

import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TableLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.niaouh.moodtracker.layout.FatigueCircle
import com.niaouh.moodtracker.layout.MoodCircle
import java.text.SimpleDateFormat
import java.util.*

class MoodViewHolder(itemView: View) : RowViewHolder(itemView)  {
    val Note: LinearLayout = itemView.findViewById(R.id.llNoteView)

    val dateText: TextView = itemView.findViewById(R.id.tvMoodDate)
    val timeText: TextView = itemView.findViewById(R.id.tvMoodTime)
    val dateTextTrack: TextView = itemView.findViewById(R.id.tvMoodDateTrack)
    val timeTextTrack: TextView = itemView.findViewById(R.id.tvMoodTimeTrack)
    val moodText: TextView= itemView.findViewById(R.id.tvMoodRating)
    val fatigueText: TextView = itemView.findViewById(R.id.tvFatigueRating)
    val moodFace: MoodCircle = itemView.findViewById(R.id.moodCircle)
    val fatigueFace: FatigueCircle= itemView.findViewById(R.id.fatigueCircle)
    val noteText: TextView = itemView.findViewById(R.id.tvNote)
    val scrollView: ScrollView = itemView.findViewById(R.id.SCROLLER_ID)

    val note: ImageButton = itemView.findViewById(R.id.ibNote)
    val noteTrack: ImageButton = itemView.findViewById(R.id.ibNoteTrack)
    val body: ConstraintLayout = itemView.findViewById(R.id.cMoodEntry)

    val trackTable: TableLayout = itemView.findViewById(R.id.tlMood)

    val trackMode: ConstraintLayout = itemView.findViewById(R.id.trackmode)
    val notTrackMode: ConstraintLayout = itemView.findViewById(R.id.base)
}

fun MoodViewHolder.updateDateText(calendar: Calendar) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    dateText.text = dateFormat.format(calendar.time)
    dateTextTrack.text = dateFormat.format(calendar.time)
}
fun MoodViewHolder.updateTimeText(calendar: Calendar) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
    timeText.text = timeFormat.format(calendar.time)
    timeTextTrack.text = timeFormat.format(calendar.time)
}

