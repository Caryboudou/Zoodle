package com.kalzakath.zoodle.model

import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.IgnoreExtraProperties
import com.kalzakath.zoodle.*
import com.kalzakath.zoodle.interfaces.RowEntryModel
import com.kalzakath.zoodle.utils.ResUtil.getDateStringFR
import com.kalzakath.zoodle.utils.ResUtil.getTimeStringFR
import java.io.Serializable
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

@IgnoreExtraProperties
data class MoodEntryModel(
    override var date: String = "2020-01-01",
    override var time: String = "08:30",
    var mood: Int = 3,
    var fatigue: Int = 3,
    var feelings: MutableList<String> = ArrayList(),
    var activities: MutableList<String> = ArrayList(),
    override var key: String = "local_" + UUID.randomUUID().toString(),
    var lastUpdated: String = LocalDateTime.now().toString(),
    var sleep: Int = 0,
    var medication: Boolean = false
): RowEntryModel,
    Serializable {

    var isVisible = true
    override var viewType: Int = 1

    @Transient var viewHolder: RecyclerView.ViewHolder? = null
}

fun MoodEntryModel.updateDateTime(calendar: Calendar) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
    val dateStr = dateFormat.format(calendar.time)

    date = dateStr
    time = timeFormat.format(calendar.time)
}

fun MoodEntryModel.compare(moodEntry: MoodEntryModel): Boolean {
    var isTheSame = true

    if (date != moodEntry.date) isTheSame = false
    if (time != moodEntry.time) isTheSame = false
    if (mood != moodEntry.mood) isTheSame = false
    if (fatigue != moodEntry.fatigue) isTheSame = false
    if (activities != moodEntry.activities) isTheSame = false
    if (feelings != moodEntry.feelings) isTheSame = false

    return isTheSame
}

fun MoodEntryModel.hideRow(mViewHolder: MoodViewHolder) {
    if (isVisible) mViewHolder.body.maxHeight = 200
    else mViewHolder.body.maxHeight = 1
}

fun MoodEntryModel.toMap(): Map<String, Any?> {
    return mapOf(
        "date" to date,
        "time" to time,
        "mood" to mood,
        "fatigue" to fatigue,
        "feelings" to feelings,
        "activities" to activities,
        "key" to key,
        "lastUpdated" to lastUpdated
    )
}
fun MoodEntryModel.update(moodEntry: MoodEntryModel) {
    lastUpdated = LocalDateTime.now().toString()

    date = moodEntry.date
    time = moodEntry.time
    mood = moodEntry.mood
    fatigue = moodEntry.fatigue
    activities = moodEntry.activities
    feelings = moodEntry.feelings
    sleep = moodEntry.sleep
    medication = moodEntry.medication

}

fun MoodEntryModel.bindToViewHolder(holder: RecyclerView.ViewHolder) {
    val mViewHolder = holder as MoodViewHolder
    mViewHolder.dateText.text = getDateStringFR(date)
    mViewHolder.timeText.text = getTimeStringFR(time)
    val moodHelper = MoodValueHelper()

    hideRow(mViewHolder)

        if (Settings.moodMode == Settings.MoodModes.FACES)

            mViewHolder.moodText.text = mViewHolder.itemView.resources.getString(
                moodHelper.getEmoji(
                    toFaces(
                        moodHelper.getSanitisedNumber(mood, 5)
                    )
                )
            )
        else mViewHolder.moodText.text = mood.toString()
        mViewHolder.activityText.text = when (activities.toString()) {
            "[]" -> "Click to add an activity"
            else -> activities.toString().removeSurrounding(
                "[",
                "]"
            )
        }
        if (Settings.fatigueMode == Settings.FatigueModes.FACES)

            mViewHolder.fatigueText.text = mViewHolder.itemView.resources.getString(
                moodHelper.getEmoji(
                    toFaces(
                        moodHelper.getSanitisedNumber(fatigue, 5)
                    )
                )
            )
        else mViewHolder.fatigueText.text = fatigue.toString()
        mViewHolder.activityText.text = when (activities.toString()) {
            "[]" -> "Click to add an activity"
            else -> activities.toString().removeSurrounding(
                "[",
                "]"
            )
        }

    mViewHolder.feelingsText.text = when (feelings.toString()) {
        "[]" -> "Click to add feelings"
        else -> feelings.toString().removeSurrounding(
            "[",
            "]"
        )
    }

    viewHolder = holder
    applyDrawableFatigue()
    applyDrawableMood()
}

fun MoodEntryModel.applyDrawableMood() {
    if (viewHolder != null) {
        val mViewHolder = viewHolder as MoodViewHolder

        when {
            mood == 1 -> mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_very_bad)
            mood == 2 -> mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_bad)
            mood == 3 -> mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_mediocre)
            mood == 4 -> mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_good)
            mood == 5 -> mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_very_good)
            else -> mViewHolder.moodText.setBackgroundResource(R.drawable.none_rating_color)
        }
    }
}

fun MoodEntryModel.applyDrawableFatigue() {
    if (viewHolder != null) {
        val mViewHolder = viewHolder as MoodViewHolder

        when {
            fatigue == 1 -> mViewHolder.fatigueText.setBackgroundResource(R.drawable.fatigue_rating_colour_very_bad)
            fatigue == 2 -> mViewHolder.fatigueText.setBackgroundResource(R.drawable.fatigue_rating_colour_bad)
            fatigue == 3 -> mViewHolder.fatigueText.setBackgroundResource(R.drawable.fatigue_rating_colour_mediocre)
            fatigue == 4 -> mViewHolder.fatigueText.setBackgroundResource(R.drawable.fatigue_rating_colour_good)
            fatigue == 5 -> mViewHolder.fatigueText.setBackgroundResource(R.drawable.fatigue_rating_colour_very_good)
            else -> mViewHolder.fatigueText.setBackgroundResource(R.drawable.none_rating_color)
        }
    }
}
