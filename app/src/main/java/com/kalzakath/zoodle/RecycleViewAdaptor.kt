package com.kalzakath.zoodle

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.kalzakath.zoodle.interfaces.DataController
import com.kalzakath.zoodle.interfaces.DataControllerEventListener
import com.kalzakath.zoodle.interfaces.RowEntryModel
import com.kalzakath.zoodle.model.*
import com.kalzakath.zoodle.utils.ResUtil.getMonthNameFRMaj
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Logger
import java.util.stream.IntStream


interface RecycleRowOnEvent {
    var onLongPress: ((MoodEntryModel) -> Unit)?
}

@SuppressLint("NotifyDataSetChanged")
class RecyclerViewAdaptor(
    val setMoodValue: (MoodEntryModel) -> Unit,
    val startNoteActivity: (MoodEntryModel) -> Unit,
    private val rowController: DataController
):
    Adapter<ViewHolder>(), SwipeHelperCallback.ItemTouchHelperAdaptor, RecycleRowOnEvent, DataControllerEventListener {

    override var onLongPress: ((MoodEntryModel) -> Unit)? = null
    private val log = Logger.getLogger(MainActivity::class.java.name + "****************************************")
    private var moodList: ArrayList<RowEntryModel> = arrayListOf()
    private lateinit var viewHolder: ViewHolder
    private var isVerticalScrollEnabled = true

    init {
        rowController.registerForUpdates(this)
        onUpdateFromDataController(RowControllerEvent())
    }

    override fun onUpdateFromDataController(event: RowControllerEvent) {
        moodList.clear()
        moodList.addAll(rowController.mainRowEntryList)
        addFilterViewMonth()
        addFilterViewWeek()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewFactory = RowViewFactory()
        viewHolder = viewFactory.createView(parent, viewType)
        return viewHolder
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addFilterView() {
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        if (moodList.isEmpty()) return

        var maxDate: LocalDate = LocalDate.now()
        var minDate: LocalDate
        var pos: OptionalInt = OptionalInt.empty()

        log.info("Adding FilterEntryViews")

        for (title in arrayListOf("Cette semaine", "Ce mois", "Cette année", "Avant")) {
            when (title) {
                "Cette semaine" -> {
                    maxDate = LocalDate.now().plusDays(1)
                    val minusDay = LocalDate.now().dayOfWeek.value.toLong()
                    minDate = LocalDate.parse("${LocalDate.now().minusDays(minusDay)}", format)
                    pos = IntStream.range(0, moodList.size - 1)
                        .filter { moodList[it].viewType == MoodEntryModel().viewType }
                        .filter {
                            LocalDate.parse(
                                (moodList[it] as MoodEntryModel).date, format
                            ) < maxDate
                                    && LocalDate.parse(
                                (moodList[it] as MoodEntryModel).date,
                                format
                            ) > minDate
                        }
                        .findFirst()
                }

                "Ce mois" -> {
                    val minusDay = LocalDate.now().dayOfWeek.value.toLong()-1
                    maxDate = LocalDate.parse("${LocalDate.now().minusDays(minusDay)}", format)

                    val date = LocalDate.now().minusMonths(1)
                        .format(DateTimeFormatter.ofPattern("yyyy-MM")) + "-" + LocalDate.now()
                        .lengthOfMonth()
                    minDate = LocalDate.parse(date, format)

                    for (i in moodList.indices) {
                        val view = MoodEntryModel().viewType
                        if (moodList[i].viewType == view) {
                            val date = LocalDate.parse((moodList[i] as MoodEntryModel).date, format)
                            val b1: Boolean = date < maxDate
                            val b2: Boolean = date > minDate
                            if (b1 && b2) {
                                pos = OptionalInt.of(i)
                                break
                            }
                        }
                    }
                }

                "Cette année" -> {
                    val date: String = LocalDate.now().minusMonths(0)
                        .format(DateTimeFormatter.ofPattern("yyyy-MM")) + "-01"
                    maxDate = LocalDate.parse(date, format)
                    minDate = LocalDate.parse("${LocalDate.now().year - 1}-12-31", format)

                    val minusDay = LocalDate.now().dayOfWeek.value.toLong() - 1
                    val maxDateWeek = LocalDate.parse("${LocalDate.now().minusDays(minusDay)}", format)
                    if (maxDateWeek.isBefore(maxDate)) maxDate = maxDateWeek

                    for (i in moodList.indices) {
                        val view = MoodEntryModel().viewType
                        if (moodList[i].viewType == view) {
                            val date = LocalDate.parse((moodList[i] as MoodEntryModel).date, format)
                            val b1: Boolean = date < maxDate
                            val b2: Boolean = date > minDate
                            if (b1 && b2) {
                                pos = OptionalInt.of(i)
                                break
                            }
                        }
                    }
                }

                "Avant" -> {
                    maxDate = LocalDate.parse("${LocalDate.now().year}-01-01", format)
                    for (i in moodList.indices) {
                        val view = MoodEntryModel().viewType
                        if (moodList[i].viewType == view) {
                            if (LocalDate.parse((moodList[i] as MoodEntryModel).date, format) < maxDate) {
                                pos = OptionalInt.of(i)
                                break
                            }
                        }
                    }
                }
            }

            if (pos != OptionalInt.empty()) {
                moodList.add(
                    pos.asInt, FilterEntryModel(
                        title,
                        moodList[pos.asInt].date,
                        convertStringToTime(moodList[pos.asInt].time).plusMinutes(1).toString()
                    )
                )
                notifyItemInserted(pos.asInt)
                pos = OptionalInt.empty()
            }
        }
        /*for (i in moodList.indices) {
            // Prevent two filter rows one after the other
            val t = moodList[i]
            val test = moodList[i-1]
            if( moodList[i].viewType == FilterEntryModel().viewType)
                    if (i > 0)
                        if (moodList[i - 1].key != "")
                            if (moodList[i -1].viewType == FilterEntryModel().viewType) {
                                moodList.removeAt(i - 1)
                                notifyItemRemoved(i-1)
                            }
        }*/
    }

    private fun getListFilter() : Pair<ArrayList<Pair<LocalDate, LocalDate>>, ArrayList<String>> {
        val arrayFilter = arrayListOf<Pair<LocalDate, LocalDate>>()
        val arrayFilterName = arrayListOf<String>()
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        if (moodList.isEmpty()) return Pair(arrayFilter, arrayFilterName)

        val lastDate = moodList.last().date
        val fisrtDate = moodList.first().date
        val today = LocalDate.parse(fisrtDate, format)
        var minDate = LocalDate.parse(lastDate, format)
        val toMinus = minDate.dayOfMonth.toLong()
        minDate = minDate.minusDays(toMinus)
        var maxDate = minDate.plusMonths(2)
        maxDate = maxDate.minusDays((maxDate.dayOfMonth - 1).toLong())

        while (minDate.isBefore(today)) {
            arrayFilter.add(Pair(minDate, maxDate))
            arrayFilterName.add("${getMonthNameFRMaj(minDate.monthValue + 1)} ${minDate.year}")
            minDate = maxDate.minusDays(1)
            maxDate = minDate.plusMonths(2)
            maxDate = maxDate.minusDays((maxDate.dayOfMonth - 1).toLong())
        }
        arrayFilter.reverse()
        arrayFilterName.reverse()

        return Pair(arrayFilter, arrayFilterName)
    }

    private fun addFilterViewMonth() {
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        if (moodList.isEmpty()) return

        var maxDate: LocalDate
        var minDate: LocalDate
        var pos: OptionalInt = OptionalInt.empty()

        val (arrayFilter, arrayFilterName) = getListFilter()

        log.info("Adding FilterEntryViews")

        for (i in 0 until arrayFilter.size) {
            minDate = arrayFilter[i].first
            maxDate = arrayFilter[i].second
            pos = IntStream.range(0, moodList.size - 1)
                .filter { moodList[it].viewType == MoodEntryModel().viewType }
                .filter {
                    LocalDate.parse(
                        (moodList[it] as MoodEntryModel).date, format
                    ) < maxDate
                            && LocalDate.parse(
                        (moodList[it] as MoodEntryModel).date,
                        format
                    ) > minDate
                }
                .findFirst()

            if (pos != OptionalInt.empty()) {
                moodList.add(
                    pos.asInt, FilterEntryModel(
                        arrayFilterName[i],
                        moodList[pos.asInt].date,
                        convertStringToTime(moodList[pos.asInt].time).plusMinutes(1).toString()
                    )
                )
                notifyItemInserted(pos.asInt)
            }
        }
        /*for (i in moodList.indices) {
            // Prevent two filter rows one after the other
            val t = moodList[i]
            val test = moodList[i-1]
            if( moodList[i].viewType == FilterEntryModel().viewType)
                    if (i > 0)
                        if (moodList[i - 1].key != "")
                            if (moodList[i -1].viewType == FilterEntryModel().viewType) {
                                moodList.removeAt(i - 1)
                                notifyItemRemoved(i-1)
                            }
        }*/
    }

    private fun addFilterViewWeek() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        if (moodList.isEmpty()) return
        val view = MoodEntryModel().viewType
        for (i in moodList.indices) {
            if (moodList[i].viewType == view && moodList[i+1].viewType == view) {
                val date1 = dateFormat.parse((moodList[i] as MoodEntryModel).date)
                val date2 = dateFormat.parse((moodList[i+1] as MoodEntryModel).date)
                if (date1 != null && date2 != null) {
                    cal1.time = date1
                    cal2.time = date2
                    if (cal1.get(Calendar.WEEK_OF_YEAR) != cal2.get(Calendar.WEEK_OF_YEAR)) {
                        moodList.add(i + 1, WeekFilterEntryModel())
                        notifyItemInserted(i + 1)
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return moodList[position].viewType
    }

    private fun toggleFilterView(position: Int) {
        var visibility: Boolean? = null
        for (i in position + 1 until moodList.size) {
            val row = moodList[i]
            if (row.viewType == MoodEntryModel().viewType) {
                val mood = row as MoodEntryModel
                if (visibility == null) {
                    visibility = !mood.isVisible
                }
                mood.isVisible = visibility

                val viewHolder = mood.viewHolder
                if (viewHolder != null) {
                    val mVH = viewHolder as MoodViewHolder
                    mood.hideRow(mVH)
                }
            }else if (row.viewType == WeekFilterEntryModel().viewType) {
                val sep = row as WeekFilterEntryModel
                if (visibility == null) {
                    visibility = !sep.isVisible
                }
                sep.isVisible = visibility

                val viewHolder = sep.viewHolder
                if (viewHolder != null) {
                    val wVH = viewHolder as WeekFilterViewHolder
                    sep.hideRow(wVH)
                }
            }else {
                break
            }
        }
    }

    private fun initButtons(viewHolder: ViewHolder, row: RowEntryModel) {

        when (row.viewType) {
            FilterEntryModel().viewType -> {
                (viewHolder as FilterViewHolder).tvFilterTitle.setOnClickListener {
                    toggleFilterView(moodList.indexOf(row))
                }
            }
            WeekFilterEntryModel().viewType -> {
                (viewHolder as WeekFilterViewHolder).sepWeekFilter.setOnClickListener {
                    toggleFilterView(moodList.indexOf(row))
                }
            }
            MoodEntryModel().viewType -> {
                val moodEntry = row as MoodEntryModel
                val mHolder = viewHolder as MoodViewHolder

                val dtPickerDate = DatePicker()
                dtPickerDate.onUpdateListener = {
                    mHolder.updateDateText(it)
                    moodEntry.updateDateOnly(it)
                    rowController.update(moodEntry)
                }

                val dtPickerTime = TimePicker()
                dtPickerTime.onUpdateListener = {
                    mHolder.updateTimeText(it)
                    moodEntry.updateTime(it)
                    rowController.update(moodEntry)
                }

                /*mHolder.scrollView.setOnTouchListener(object : View.OnTouchListener {
                    @SuppressLint("ClickableViewAccessibility")
                    override fun onTouch(view: View, motionEvent: MotionEvent?): Boolean {
                        // Disallow the touch request for parent scroll on touch of child view
                        view.getParent().requestDisallowInterceptTouchEvent(false)
                        return true
                    }
                })*/

                mHolder.dateText.setOnClickListener {
                    dtPickerDate.show(mHolder.itemView.context, moodEntry.date)
                }

                mHolder.timeText.setOnClickListener {
                    dtPickerTime.show(mHolder.itemView.context, moodEntry.time)
                }

                mHolder.moodText.setOnClickListener {
                    setMoodValue(moodEntry)
                }

                mHolder.fatigueText.setOnClickListener {
                    setMoodValue(moodEntry)
                }

                mHolder.moodFace.setOnClickListener {
                    setMoodValue(moodEntry)
                }

                mHolder.fatigueFace.setOnClickListener {
                    setMoodValue(moodEntry)
                }

                mHolder.note.setOnClickListener {
                    startNoteActivity(moodEntry)
                }

                mHolder.dateText.setOnLongClickListener {
                    onLongPress?.invoke(row)
                    return@setOnLongClickListener true
                }

                mHolder.timeText.setOnLongClickListener {
                    onLongPress?.invoke(row)
                    return@setOnLongClickListener true
                }

                mHolder.moodText.setOnLongClickListener {
                    onLongPress?.invoke(row)
                    return@setOnLongClickListener true
                }

                mHolder.fatigueText.setOnLongClickListener {
                    onLongPress?.invoke(row)
                    return@setOnLongClickListener true
                }

                mHolder.moodFace.setOnLongClickListener {
                    onLongPress?.invoke(row)
                    return@setOnLongClickListener true
                }

                mHolder.fatigueFace.setOnLongClickListener {
                    onLongPress?.invoke(row)
                    return@setOnLongClickListener true
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (moodList[position].viewType) {
            MoodEntryModel().viewType -> (moodList[position] as MoodEntryModel).bindToViewHolder(holder)
            FilterEntryModel().viewType -> (moodList[position] as FilterEntryModel).bindToViewHolder(holder)
            WeekFilterEntryModel().viewType -> (moodList[position] as WeekFilterEntryModel).bindToViewHolder(holder)
        }
        initButtons(holder, moodList[position])
    }

    override fun getItemCount(): Int {
        return moodList.size
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        return false
    }

    fun getItem(position: Int): RowEntryModel? {
        if (position < moodList.size) return moodList[position]
        return null
    }

    override fun onItemDismiss(position: Int) : RowEntryModel {
        val row = moodList[position]
        rowController.remove(moodList[position])
        return row
    }

    override fun onItemAdd(row: RowEntryModel) {
        rowController.add(row)
    }
}