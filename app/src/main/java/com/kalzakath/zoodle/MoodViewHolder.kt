package com.kalzakath.zoodle

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.kalzakath.zoodle.layout.FatigueCircle
import com.kalzakath.zoodle.layout.MoodCircle
import java.text.SimpleDateFormat
import java.util.*

class MoodViewHolder(itemView: View) : RowViewHolder(itemView)  {
    val dateText: TextView = itemView.findViewById(R.id.tvMoodDate)
    val timeText: TextView = itemView.findViewById(R.id.tvMoodTime)
    val moodText: TextView= itemView.findViewById(R.id.tvMoodRating)
    val fatigueText: TextView = itemView.findViewById(R.id.tvFatigueRating)
    val moodFace: MoodCircle = itemView.findViewById(R.id.moodCircle)
    val note: ImageButton = itemView.findViewById(R.id.ibNote)
    val fatigueFace: FatigueCircle= itemView.findViewById(R.id.fatigueCircle)
    val body: ConstraintLayout = itemView.findViewById(R.id.cMoodEntry)
}

fun MoodViewHolder.updateDateText(calendar: Calendar) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    dateText.text = dateFormat.format(calendar.time)
}
fun MoodViewHolder.updateTimeText(calendar: Calendar) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
    timeText.text = timeFormat.format(calendar.time)
}

