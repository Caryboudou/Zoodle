package com.niaouh.moodtracker.model

import android.graphics.Color
import android.widget.CheckBox
import android.widget.TableRow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.IgnoreExtraProperties
import com.niaouh.moodtracker.*
import com.niaouh.moodtracker.data.CircleFatigueBO
import com.niaouh.moodtracker.data.CircleMoodBO
import com.niaouh.moodtracker.data.CircleStateBO
import com.niaouh.moodtracker.interfaces.DataController
import com.niaouh.moodtracker.interfaces.RowEntryModel
import com.niaouh.moodtracker.utils.ResUtil.getDateStringFR
import com.niaouh.moodtracker.utils.ResUtil.getDayNameFR
import com.niaouh.moodtracker.utils.ResUtil.getMonthNameFR
import com.niaouh.moodtracker.utils.ResUtil.getTimeStringFR
import java.io.Serializable
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

data class MoodEntryModelToJson(
    var dateYear: Int = 2020,
    var dateMonth: Int = 1,
    var dateDay: Int = 1,
    var dateHour: Int = 0,
    var dateMinute: Int = 0,
    var mood: Int = 0,
    var fatigue: Int = 0,
    var note: String = "",
    var ritaline: String = "",
    var trackers: ArrayList<String> = arrayListOf(),
    var key: String = "local_" + UUID.randomUUID().toString(),
    var lastUpdated: String = LocalDateTime.now().toString()

): Serializable
fun MoodEntryModelToJson.MoodEntryModel(): MoodEntryModel {
    return createNewEntry(dateYear,
        dateMonth,
        dateDay,
        dateHour,
        dateMinute,
        mood,
        fatigue,
        note,
        ritaline,
        trackers,
        key,
        lastUpdated)
}

@IgnoreExtraProperties
data class MoodEntryModel(
    override var date: LocalDateTime = LocalDateTime.now(),
    var mood: Int = 0,
    var fatigue: Int = 0,
    var note: String = "",
    var ritaline: String = "",
    override var key: String = "local_" + UUID.randomUUID().toString(),
    var lastUpdated: String = LocalDateTime.now().toString()

): RowEntryModel,
    Serializable {

    var isVisible = true
    override var viewType: Int = 1
    var trackers = arrayListOf<String>()

    @Transient var viewHolder: RecyclerView.ViewHolder? = null

    fun textMood(): String {
        val monthName = getMonthNameFR(date.month.value)
        val dayName = getDayNameFR(date.dayOfWeek.value)
        val dayNumber = date.dayOfMonth
        return "$dayName $dayNumber $monthName"
    }

    fun textMoodDay(): String {
        return getDayNameFR(date.dayOfWeek.value)
    }

    fun textMoodSnackbar(): String {
        val monthName = getMonthNameFR(date.month.value)
        val dayNumber = date.dayOfMonth
        val yearNumber = date.year
        return "$dayNumber $monthName $yearNumber"
    }
}
fun MoodEntryModel.MoodEntryModelToJson(): MoodEntryModelToJson {
    return MoodEntryModelToJson(date.year,
        date.monthValue,
        date.dayOfMonth,
        date.hour,
        date.minute,
        mood,
        fatigue,
        note,
        ritaline,
        trackers,
        key,
        lastUpdated)
}
fun createNewEntry(year: Int = 2020,
                   month: Int = 1,
                   day:Int = 1,
                   hour:Int = 23,
                   minute:Int = 59,
                   mood: Int = 0,
                   fatigue: Int = 0,
                   note: String = "",
                   ritaline: String = "",
                   trackers: ArrayList<String> = arrayListOf(),
                   key: String = "local_" + UUID.randomUUID().toString(),
                   lastUpdated: String = LocalDateTime.now().toString()): MoodEntryModel {
    val date = LocalDateTime.of(year, month,day, hour, minute)
    val moodEntry = MoodEntryModel(date, mood, fatigue, note, ritaline, key, lastUpdated)
    moodEntry.trackers = trackers
    return moodEntry
}
fun MoodEntryModel.updateDate(calendar: Calendar) {
    val newDate = LocalDateTime.of(calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH)+1,
        calendar.get(Calendar.DAY_OF_MONTH),
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE))

    date = newDate
}
fun MoodEntryModel.updateDateOnly(calendar: Calendar) {
    val newDate = LocalDateTime.of(calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH)+1,
        calendar.get(Calendar.DAY_OF_MONTH),
        date.hour,
        date.minute)

    date = newDate
}
fun MoodEntryModel.updateTime(calendar: Calendar) {
    val newDate = LocalDateTime.of(date.year,
        date.month,
        date.dayOfMonth,
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE))

    date = newDate
}

fun MoodEntryModel.compare(moodEntry: MoodEntryModel): Boolean {
    var isTheSame = true

    if (date != moodEntry.date) isTheSame = false
    if (mood != moodEntry.mood) isTheSame = false
    if (fatigue != moodEntry.fatigue) isTheSame = false

    return isTheSame
}

fun MoodEntryModel.hideRow(mViewHolder: MoodViewHolder) {
    if (isVisible) mViewHolder.body.maxHeight = 1000
    else mViewHolder.body.maxHeight = 0
}

fun MoodEntryModel.toMap(): Map<String, Any?> {
    return mapOf(
        "date" to date,
        "mood" to mood,
        "fatigue" to fatigue,
        "note" to note,
        "ritaline" to ritaline,
        "key" to key,
        "lastUpdated" to lastUpdated
    )
}
fun MoodEntryModel.update(moodEntry: MoodEntryModel) {
    lastUpdated = LocalDateTime.now().toString()

    date = moodEntry.date
    mood = moodEntry.mood
    fatigue = moodEntry.fatigue
    note = moodEntry.note
    ritaline = moodEntry.ritaline
}

fun MoodEntryModel.bindToViewHolder(holder: RecyclerView.ViewHolder, rowController: DataController) {
    val mViewHolder = holder as MoodViewHolder
    mViewHolder.dateText.text = textMood()
    mViewHolder.timeText.text = getTimeStringFR(date)
    mViewHolder.dateTextTrack.text = getDateStringFR(date)
    mViewHolder.timeTextTrack.text = getTimeStringFR(date)
    mViewHolder.noteText.text = note

    if (Settings.modeNote && note != "") {
        mViewHolder.Note.visibility = android.view.View.VISIBLE
    } else {
        mViewHolder.Note.visibility = android.view.View.GONE
    }

    if (Settings.trackerMode) {
        mViewHolder.trackMode.visibility = android.view.View.VISIBLE
        mViewHolder.notTrackMode.visibility = android.view.View.GONE
    } else {
        mViewHolder.trackMode.visibility = android.view.View.GONE
        mViewHolder.notTrackMode.visibility = android.view.View.VISIBLE
    }
    val header = TableRow(holder.itemView.context)
    for (t in Settings.trackerList) {
        val head = TextView(holder.itemView.context)
        head.setTextColor(Color.WHITE)
        val text = "$t  "
        head.text = text
        header.addView(head)
    }
    val newRow = TableRow(holder.itemView.context)
    for (t in Settings.trackerList) {
        val checkTrack = CheckBox(holder.itemView.context)
        if (trackers.contains(t)) checkTrack.isChecked = true
        checkTrack.setOnCheckedChangeListener {_, isChecked ->
            if (!isChecked) trackers.remove(t)
            else {
                if (!trackers.contains(t)) trackers.add(t)
            }
            rowController.update(this)
        }
        newRow.addView(checkTrack)
    }
    mViewHolder.trackTable.removeAllViews()
    mViewHolder.trackTable.addView(header)
    mViewHolder.trackTable.addView(newRow)

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

        when (mood) {
            1 -> { mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_very_bad) }
            2 -> { mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_bad_1) }
            3 -> { mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_bad) }
            4 -> { mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_mediocre_1) }
            5 -> { mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_mediocre) }
            6 -> { mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_good_1) }
            7 -> { mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_good) }
            8 -> { mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_very_good_1) }
            9 -> { mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_very_good) }
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

        when (fatigue) {
            1 -> { mViewHolder.fatigueText.setBackgroundResource(R.drawable.fatigue_rating_colour_very_bad) }
            2 -> { mViewHolder.fatigueText.setBackgroundResource(R.drawable.fatigue_rating_colour_bad_1) }
            3 -> { mViewHolder.fatigueText.setBackgroundResource(R.drawable.fatigue_rating_colour_bad) }
            4 -> { mViewHolder.fatigueText.setBackgroundResource(R.drawable.fatigue_rating_colour_mediocre_1) }
            5 -> { mViewHolder.fatigueText.setBackgroundResource(R.drawable.fatigue_rating_colour_mediocre) }
            6 -> { mViewHolder.fatigueText.setBackgroundResource(R.drawable.fatigue_rating_colour_good_1) }
            7 -> { mViewHolder.fatigueText.setBackgroundResource(R.drawable.fatigue_rating_colour_good) }
            8 -> { mViewHolder.fatigueText.setBackgroundResource(R.drawable.fatigue_rating_colour_very_good_1) }
            9 -> { mViewHolder.fatigueText.setBackgroundResource(R.drawable.fatigue_rating_colour_very_good) }
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

    try {
        dose = ritaline.toInt()
    } catch (_: Exception) {}

    for (word in listWord) {
        var intValue = word.value
        intValue = intValue.replace(Regex("[^0-9]"), "")
        dose += intValue.toInt()
    }

    return dose
}
