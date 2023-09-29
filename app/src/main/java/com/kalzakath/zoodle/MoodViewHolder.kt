package com.kalzakath.zoodle

import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.kalzakath.zoodle.layout.FatigueCircle
import com.kalzakath.zoodle.layout.MoodCircle
import java.text.SimpleDateFormat
import java.util.*

class MoodViewHolder(itemView: View) : RowViewHolder(itemView)  {
    val modeMood: ConstraintLayout = itemView.findViewById(R.id.llMainRowTop)
    val modeNote: LinearLayout = itemView.findViewById(R.id.llMainRowTopNote)

    val dateText: TextView = itemView.findViewById(R.id.tvMoodDate)
    val timeText: TextView = itemView.findViewById(R.id.tvMoodTime)
    val moodText: TextView= itemView.findViewById(R.id.tvMoodRating)
    val fatigueText: TextView = itemView.findViewById(R.id.tvFatigueRating)
    val moodFace: MoodCircle = itemView.findViewById(R.id.moodCircle)
    val fatigueFace: FatigueCircle= itemView.findViewById(R.id.fatigueCircle)

    val dateTextNote: TextView = itemView.findViewById(R.id.tvMoodDateNote)
    val timeTextNote: TextView = itemView.findViewById(R.id.tvMoodTimeNote)
    val moodTextNote: TextView= itemView.findViewById(R.id.tvMoodRatingNote)
    val fatigueTextNote: TextView = itemView.findViewById(R.id.tvFatigueRatingNote)
    val moodFaceNote: MoodCircle = itemView.findViewById(R.id.moodCircleNote)
    val fatigueFaceNote: FatigueCircle= itemView.findViewById(R.id.fatigueCircleNote)
    val noteText: TextView = itemView.findViewById(R.id.tvNote)

    val note: ImageButton = itemView.findViewById(R.id.ibNote)
    val body: ConstraintLayout = itemView.findViewById(R.id.cMoodEntry)
}

fun MoodViewHolder.updateDateText(calendar: Calendar) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    dateText.text = dateFormat.format(calendar.time)
    dateTextNote.text = dateFormat.format(calendar.time)
}
fun MoodViewHolder.updateTimeText(calendar: Calendar) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
    timeText.text = timeFormat.format(calendar.time)
    timeTextNote.text = timeFormat.format(calendar.time)
}

