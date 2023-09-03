package com.kalzakath.zoodle

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.kalzakath.zoodle.interfaces.DataController
import com.kalzakath.zoodle.interfaces.DataControllerEventListener
import com.kalzakath.zoodle.interfaces.RowEntryModel
import com.kalzakath.zoodle.model.*
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

    init {
        rowController.registerForUpdates(this)
        onUpdateFromDataController(RowControllerEvent())
    }

    override fun onUpdateFromDataController(event: RowControllerEvent) {
        moodList.clear()
        moodList.addAll(rowController.mainRowEntryList)
        addFilterView()
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



    override fun getItemViewType(position: Int): Int {
        return moodList[position].viewType
    }

    private fun toggleFilterView(position: Int) {
        for (i in position + 1 until moodList.size) {
            val row = moodList[i]
            if (row.viewType == MoodEntryModel().viewType) {
                val mood = row as MoodEntryModel
                mood.isVisible = !mood.isVisible

                val viewHolder = mood.viewHolder
                if (viewHolder != null) {
                    val mVH = viewHolder as MoodViewHolder
                    mood.hideRow(mVH)
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
            MoodEntryModel().viewType -> {
                val moodEntry = row as MoodEntryModel
                val mHolder = viewHolder as MoodViewHolder

                val dtPickerDate = DatePicker()
                dtPickerDate.onUpdateListener = {
                    mHolder.updateDateText(it)
                    moodEntry.updateDate(it)
                    rowController.update(moodEntry)
                }

                val dtPickerTime = TimePicker()
                dtPickerTime.onUpdateListener = {
                    mHolder.updateTimeText(it)
                    moodEntry.updateTime(it)
                    rowController.update(moodEntry)
                }

                mHolder.dateText.setOnClickListener {
                    dtPickerDate.show(mHolder.itemView.context)
                }

                mHolder.timeText.setOnClickListener {
                    dtPickerTime.show(mHolder.itemView.context)
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

    override fun onItemDismiss(position: Int) {
        rowController.remove(moodList[position])
    }                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
}