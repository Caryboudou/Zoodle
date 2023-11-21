package com.kalzakath.zoodle.model

import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.IgnoreExtraProperties
import com.kalzakath.zoodle.*
import com.kalzakath.zoodle.data.CircleFatigueBO
import com.kalzakath.zoodle.data.CircleMoodBO
import com.kalzakath.zoodle.data.CircleStateBO
import com.kalzakath.zoodle.interfaces.RowEntryModel
import com.kalzakath.zoodle.utils.ResUtil.getDayNameFR
import com.kalzakath.zoodle.utils.ResUtil.getMonthNameFR
import com.kalzakath.zoodle.utils.ResUtil.getTimeStringFR
import java.io.Serializable
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@IgnoreExtraProperties
data class MoodEntryModel(
    override var date: String = "2020-01-01",
    override var time: String = "08:30",
    var mood: Int = 0,
    var fatigue: Int = 0,
    var feelings: MutableList<String> = ArrayList(),
    var activities: MutableList<String> = ArrayList(),
    var note: String = "",
    override var key: String = "local_" + UUID.randomUUID().toString(),
    var lastUpdated: String = LocalDateTime.now().toString(),
    var sleep: Int = 0,
    var ritaline: String = ""

): RowEntryModel,
    Serializable {

    var isVisible = true
    override var viewType: Int = 1

    @Transient var viewHolder: RecyclerView.ViewHolder? = null

    fun textMood(): String {
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateLocal = LocalDate.parse(date, format)
        val monthName = getMonthNameFR(dateLocal.month.value)
        val dayName = getDayNameFR(dateLocal.dayOfWeek.value)
        val dayNumber = dateLocal.dayOfMonth
        return "$dayName $dayNumber $monthName"
    }

    fun textMoodSnackbar(): String {
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateLocal = LocalDate.parse(date, format)
        val monthName = getMonthNameFR(dateLocal.month.value)
        val dayNumber = dateLocal.dayOfMonth
        val yearNumber = dateLocal.year
        return "$dayNumber $monthName $yearNumber"
    }
}

fun MoodEntryModel.updateDate(calendar: Calendar) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
    val dateStr = dateFormat.format(calendar.time)

    date = dateStr
    time = timeFormat.format(calendar.time)
}
fun MoodEntryModel.updateDateOnly(calendar: Calendar) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val dateStr = dateFormat.format(calendar.time)

    date = dateStr
}
fun MoodEntryModel.updateTime(calendar: Calendar) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
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
    if (isVisible) mViewHolder.body.maxHeight = 1000
    else mViewHolder.body.maxHeight = 0
}

fun MoodEntryModel.toMap(): Map<String, Any?> {
    return mapOf(
        "date" to date,
        "time" to time,
        "mood" to mood,
        "fatigue" to fatigue,
        "feelings" to feelings,
        "activities" to activities,
        "note" to note,
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
    note = moodEntry.note
    sleep = moodEntry.sleep
    ritaline = moodEntry.ritaline
}

fun MoodEntryModel.bindToViewHolder(holder: RecyclerView.ViewHolder) {
    val mViewHolder = holder as MoodViewHolder
    mViewHolder.dateText.text = textMood()
    mViewHolder.timeText.text = getTimeStringFR(time)
    mViewHolder.noteText.text = note

    if (Settings.modeNote && note != "") {
        mViewHolder.Note.visibility = android.view.View.VISIBLE
    } else {
        mViewHolder.Note.visibility = android.view.View.GONE
    }

    hideRow(mViewHolder)

    mViewHolder.moodText.text = mood.toString()
    mViewHolder.moodText.text = mood.toString()

    mViewHolder.fatigueText.text = fatigue.toString()
    mViewHolder.fatigueText.text = fatigue.toString()

    viewHolder = holder
    applyDrawableFatigue()
    applyDrawableMood()
}

fun MoodEntryModel.applyDrawableMood() {
    if (viewHolder != null && Settings.moodMode == Settings.MoodModes.NUMBERS) {
        val mViewHolder = viewHolder as MoodViewHolder
        mViewHolder.moodFace.visibility = android.view.View.INVISIBLE

        when {
            mood == 1 -> { mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_very_bad) }
            mood == 2 -> { mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_bad) }
            mood == 3 -> { mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_mediocre) }
            mood == 4 -> { mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_good) }
            mood == 5 -> { mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_very_good) }
            else -> { mViewHolder.moodText.setBackgroundResource(R.drawable.none_rating_color) }
        }
    }
    else if (viewHolder != null && Settings.moodMode == Settings.MoodModes.FACES) {
        val mViewHolder = viewHolder as MoodViewHolder
        val face = mViewHolder.moodFace
        face.state = CircleStateBO.CHOOSE_MOOD
        face.mood = CircleMoodBO.from(mood)
        face.visibility = android.view.View.VISIBLE
        mViewHolder.moodText.setBackgroundResource(0)
    }
}

fun MoodEntryModel.applyDrawableFatigue() {
    if (viewHolder != null && Settings.fatigueMode == Settings.FatigueModes.NUMBERS) {
        val mViewHolder = viewHolder as MoodViewHolder
        mViewHolder.fatigueFace.visibility = android.view.View.INVISIBLE

        when {
            fatigue == 1 -> { mViewHolder.fatigueText.setBackgroundResource(R.drawable.fatigue_rating_colour_very_bad) }
            fatigue == 2 -> { mViewHolder.fatigueText.setBackgroundResource(R.drawable.fatigue_rating_colour_bad) }
            fatigue == 3 -> { mViewHolder.fatigueText.setBackgroundResource(R.drawable.fatigue_rating_colour_mediocre) }
            fatigue == 4 -> { mViewHolder.fatigueText.setBackgroundResource(R.drawable.fatigue_rating_colour_good) }
            fatigue == 5 -> { mViewHolder.fatigueText.setBackgroundResource(R.drawable.fatigue_rating_colour_very_good) }
            else -> { mViewHolder.fatigueText.setBackgroundResource(R.drawable.none_rating_color) }
        }
    } else if (viewHolder != null && Settings.fatigueMode == Settings.FatigueModes.FACES) {
        val mViewHolder = viewHolder as MoodViewHolder
        val face = mViewHolder.fatigueFace
        face.state = CircleStateBO.CHOOSE_MOOD
        face.fatigue = CircleFatigueBO.from(fatigue)
        face.visibility = android.view.View.VISIBLE
        mViewHolder.fatigueText.setBackgroundResource(0)
    }
}

fun MoodEntryModel.getRitalineInt(): Int {
    val regex = Regex("""\d+mg|\d+\h""")
    val listWord = regex.findAll(ritaline)
    var dose = 0

    for (word in listWord) {
        var intValue = word.value
        intValue = intValue.replace(Regex("[^0-9]"), "")
        dose += intValue.toInt()
    }

    return dose
}
