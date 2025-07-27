package com.niaouh.moodtracker

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.niaouh.moodtracker.data.CircleFatigueBO
import com.niaouh.moodtracker.data.CircleMoodBO
import com.niaouh.moodtracker.model.MoodEntryModel
import com.niaouh.moodtracker.model.MoodEntryModelToJson
import com.niaouh.moodtracker.model.getRitalineInt
import com.niaouh.moodtracker.trackerpopup.TrendViewSelectMoyDialog
import com.niaouh.moodtracker.utils.ResUtil.getDateStringFR
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Logger
import kotlin.collections.ArrayList


class TrendViewActivity : AppCompatActivity() {

    private var moodData = ArrayList<MoodEntryModel>()
    private var maxDate: LocalDate = LocalDate.now()
    private var minDate: LocalDate = LocalDate.now()
    private lateinit var mainLayout: ConstraintLayout
    private lateinit var selectMoyDialog: TrendViewSelectMoyDialog
    private val DEFAULT_MOY = 7L
    private var moy: Long = 7L
    private val log = Logger.getLogger(MainActivity::class.java.name + "TrendView")


    class ChartValueFormatter(): ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            return formatter.format(Date(value.toLong()))
        }

        override fun getFormattedValue(value: Float): String {
            return value.toInt().toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trend_view)

        val securityHandler = SecurityHandler(applicationContext)
        val secureFileHandler = SecureFileHandler(securityHandler)

        val jsonString = secureFileHandler.read()
        if (jsonString != "") moodData = getMoodListFromJSON(jsonString)

        val bViewData: ImageButton = findViewById(R.id.bViewData)
        val bReset: ImageButton = findViewById(R.id.bTrendReset)
        val binitDate: Button = findViewById(R.id.bTrendInitDate)
        val bendDate: Button = findViewById(R.id.bTrendEndDate)
        val cMood: CheckBox = findViewById(R.id.checkMood)
        val cFatigue: CheckBox = findViewById(R.id.checkFatigue)
        val cRitaline: CheckBox = findViewById(R.id.checkRitaline)
        val cMoy: CheckBox = findViewById(R.id.checkMoy)
        val tvMoy: TextView = findViewById(R.id.tvMoy)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val dateFormatLocal = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        var date = LocalDate.now()

        tvMoy.text = moy.toString()
        selectMoyDialog = TrendViewSelectMoyDialog(this) { l ->
            finishSelectMoyDialog(l, cMood,
                cFatigue,
                cRitaline,
                cMoy)
            tvMoy.text = moy.toString()
        }

        cRitaline.text = Settings.medicationName

        if (Settings.medicationName == "") {
            cRitaline.visibility = View.GONE
            cRitaline.isChecked = false
        }
        else {
            cRitaline.visibility = VISIBLE
            cRitaline.isChecked = true
        }

        bendDate.text = getDateStringFR(date)
        maxDate = date

        date = LocalDate.now().minusMonths(1)
        binitDate.text = getDateStringFR(date)
        minDate = date

        val dtPickerInit = DatePicker()
        dtPickerInit.onUpdateListener = {
            minDate = LocalDate.of(it.get(Calendar.YEAR), it.get(Calendar.MONTH)+1, it.get(Calendar.DAY_OF_MONTH))
            binitDate.text = getDateStringFR(it)
            setLineChartData(cMood.isChecked, cFatigue.isChecked, cRitaline.isChecked, cMoy.isChecked)
        }

        val dtPickerEnd = DatePicker()
        dtPickerEnd.onUpdateListener = {
            maxDate = LocalDate.of(it.get(Calendar.YEAR), it.get(Calendar.MONTH)+1, it.get(Calendar.DAY_OF_MONTH))
            bendDate.text = getDateStringFR(it)
            setLineChartData(cMood.isChecked, cFatigue.isChecked, cRitaline.isChecked, cMoy.isChecked)
        }
        
        if (moodData.isNotEmpty())
            setLineChartData(cMood.isChecked, cFatigue.isChecked, cRitaline.isChecked, cMoy.isChecked)

        cMood.setOnClickListener {
            setLineChartData(cMood.isChecked, cFatigue.isChecked, cRitaline.isChecked, cMoy.isChecked)
        }

        tvMoy.setOnClickListener {
            log.info("tvmoy clicked")
            selectMoyDialog.showPopup(moy.toInt())
        }

        cFatigue.setOnClickListener {
            setLineChartData(cMood.isChecked, cFatigue.isChecked, cRitaline.isChecked, cMoy.isChecked)
        }

        cRitaline.setOnClickListener {
            setLineChartData(cMood.isChecked, cFatigue.isChecked, cRitaline.isChecked, cMoy.isChecked)
        }

        cMoy.setOnClickListener {
            setLineChartData(cMood.isChecked, cFatigue.isChecked, cRitaline.isChecked, cMoy.isChecked)
        }

        bReset.setOnClickListener {
            bendDate.text = getDateStringFR(LocalDate.now())
            maxDate = LocalDate.now()

            binitDate.text = getDateStringFR(LocalDate.now().minusMonths(1))
            minDate = LocalDate.now().minusMonths(1)

            moy = DEFAULT_MOY
            tvMoy.text = moy.toString()

            setLineChartData(cMood.isChecked, cFatigue.isChecked, cRitaline.isChecked)
        }

        binitDate.setOnClickListener {
            dtPickerInit.show(this, minDate)
        }

        bendDate.setOnClickListener {
            dtPickerEnd.show(this, maxDate)
        }

        bViewData.setOnClickListener {
            finish()
        }
    }

    private fun finishSelectMoyDialog(l: Int, cMood: CheckBox,
            cFatigue: CheckBox,
            cRitaline: CheckBox,
            cMoy: CheckBox) {
        moy = l.toLong()
        if (cMoy.isChecked)
            setLineChartData(cMood.isChecked,
                cFatigue.isChecked,
                cRitaline.isChecked,
                cMoy.isChecked)
    }

    private fun setLineChartData(cMood: Boolean, cFatigue: Boolean, cRitaline: Boolean, cMoy: Boolean = true) {
        var entryListMood: ArrayList<Entry> = ArrayList()
        var entryListFatigue: ArrayList<Entry> = ArrayList()
        var entryListRitaline: ArrayList<Entry> = ArrayList()
        val entriesMood: ArrayList<Entry> = ArrayList()
        val entriesFatigue: ArrayList<Entry> = ArrayList()
        val listRitaline: ArrayList<Pair<LocalDate, Entry>> = ArrayList()
        val listLineRitaline: ArrayList<ArrayList<Pair<LocalDate, Entry>>> = ArrayList()
        val lines = mutableListOf<ILineDataSet>()
        val linesMood = mutableListOf<ILineDataSet>()
        val linesFatigue = mutableListOf<ILineDataSet>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd,HH:mm", Locale.ENGLISH)
        val dateFormatLocal = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        var dateIt : LocalDate
        var colorsMood = mutableListOf<Int>()
        var colorsFatigue = mutableListOf<Int>()

        log.info("moy $moy")

        val chart: LineChart = findViewById(R.id.getTheGraph)

        if (cMoy) {
            minDate = minDate.minusDays(moy)
        }

        for (moods in moodData) {
            var moodNumber = moods.mood
            var fatigueNumber = moods.fatigue
            val ritalineNumber = moods.getRitalineInt()
            val dateTime = Date.from(moods.date.atZone(ZoneId.systemDefault()).toInstant())
            val xDate = dateTime?.time?.toFloat() ?: 0.0.toFloat()

            if (Settings.moodMode == Settings.MoodModes.NUMBERS) { moodNumber = moods.mood; fatigueNumber = moods.fatigue }
            dateIt = moods.date.toLocalDate()

            if (dateIt in minDate..maxDate) {
                if (cMood) {
                    val entryMood = Entry(xDate,moodNumber.toFloat())
                    entriesMood.add(entryMood)
                    if (moodNumber != 0) {
                        entryListMood.add(entryMood)
                        colorsMood.add(applicationContext.getColor(CircleMoodBO.from(moodNumber).colorId))
                    } else if (entryListMood.isNotEmpty()) {
                        val lineDataSet = initMoodLine(entryListMood, colorsMood, chart)
                        linesMood.add(lineDataSet)
                        entryListMood = ArrayList()
                        colorsMood = ArrayList()
                    }
                }

                if (cFatigue) {
                    val entryFatigue = Entry(xDate, fatigueNumber.toFloat())
                    entriesFatigue.add(entryFatigue)
                    if (fatigueNumber != 0) {
                        entryListFatigue.add(entryFatigue)
                        colorsFatigue.add(
                            applicationContext.getColor(
                                CircleFatigueBO.from(
                                    fatigueNumber
                                ).colorId
                            )
                        )
                    } else if (entryListFatigue.isNotEmpty()) {
                        val lineDataSet = initFatigueLine(entryListFatigue, colorsFatigue, chart)
                        linesFatigue.add(lineDataSet)
                        entryListFatigue = ArrayList()
                        colorsFatigue = ArrayList()
                    }
                }

                if (cRitaline) {
                    if (ritalineNumber != 0) {
                        listRitaline.add(
                            Pair(dateIt, Entry(xDate, ritalineNumber.toFloat()))
                        )
                    } else if (entryListRitaline.isNotEmpty()) {
                        listLineRitaline.add(listRitaline)
                        entryListRitaline = ArrayList()
                    }
                }
            }
        }

        if (cMoy) minDate = minDate.plusDays(moy)

        val lineDataSetMood = initMoodLine(entryListMood, colorsMood, chart)

        val lineDataSetFatigue = initFatigueLine(entryListFatigue, colorsFatigue, chart)

        if (cMood) { linesMood.add(lineDataSetMood) }
        if (cFatigue) { linesFatigue.add(lineDataSetFatigue) }
        if (cRitaline) { listLineRitaline.add(listRitaline)
            for (listR in listLineRitaline) {
                val l = initRitalineLine((listR.filter { e -> e.first >= minDate }).map { e -> e.second } as ArrayList<Entry>, chart)
                lines.add(l)
            }
        }

        if (cMoy) {
            if (cMood) lines.add(getMoy(entriesMood, chart, "Mood", Color.RED))
            if (cFatigue) lines.add(getMoy(entriesFatigue, chart, "Fatigue", Color.BLUE))
        }
        else {
            if (cMood) {
                for (l in linesMood) lines.add(l)
            }
            if (cFatigue) {
                for (l in linesFatigue) lines.add(l)
            }
        }

        val data = LineData(lines)
        data.setValueTextColor(Color.WHITE)
        data.setValueFormatter(ChartValueFormatter())

        chart.legend.isEnabled = false

        chart.invalidate()
        chart.data = data
        chart.setBackgroundColor(Color.BLACK)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.WHITE
        xAxis.valueFormatter = ChartValueFormatter()
        xAxis.labelRotationAngle = 0f
        xAxis.setDrawGridLines(false)

        if (cMood or cFatigue) {
            val yAxis = chart.axisLeft
            yAxis.textColor = Color.WHITE
            yAxis.axisMaximum = Settings.moodMax.toFloat()
            yAxis.axisMinimum = 1f
            yAxis.granularity = 1f
            yAxis.setDrawGridLines(true)
        }

        chart.axisRight.setDrawGridLines(false)
    }

    private fun initFatigueLine(entryListFatigue: ArrayList<Entry>, colorsFatigue: MutableList<Int>, lineChart: LineChart): ILineDataSet {
        Collections.sort(entryListFatigue, EntryXComparator())
        val lineDataSetFatigue = LineDataSet(entryListFatigue, "Fatigue")
        lineDataSetFatigue.color = Color.GRAY
        lineDataSetFatigue.circleRadius = 6f
        lineDataSetFatigue.circleColors = colorsFatigue.reversed()
        lineDataSetFatigue.setDrawCircles(true)
        lineDataSetFatigue.setDrawValues(false)
        lineDataSetFatigue.valueTextSize = 10F
        lineDataSetFatigue.lineWidth = 2f
        lineDataSetFatigue.valueTextColor = Color.WHITE
        lineDataSetFatigue.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        lineDataSetFatigue.axisDependency = lineChart.axisLeft.axisDependency
        return lineDataSetFatigue
    }

    private fun initMoodLine(entryListMood: ArrayList<Entry>, colorsMood: List<Int>, lineChart: LineChart): ILineDataSet {
        Collections.sort(entryListMood, EntryXComparator())
        val lineDataSetMood = LineDataSet(entryListMood, "Mood")
        lineDataSetMood.valueTextSize = 10F
        lineDataSetMood.lineWidth = 2f
        lineDataSetMood.color = Color.LTGRAY
        lineDataSetMood.circleRadius = 6f
        lineDataSetMood.circleColors = colorsMood.reversed()
        lineDataSetMood.setDrawCircles(true)
        lineDataSetMood.setDrawValues(false)
        lineDataSetMood.valueTextColor = Color.WHITE
        lineDataSetMood.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        lineDataSetMood.axisDependency = lineChart.axisLeft.axisDependency
        return  lineDataSetMood
    }

    private fun initRitalineLine(entryListRitaline: ArrayList<Entry>, lineChart: LineChart): ILineDataSet {
        Collections.sort(entryListRitaline, EntryXComparator())
        val lineDataSetRitaline = LineDataSet(entryListRitaline, "Ritaline")
        lineDataSetRitaline.valueTextSize = 10F
        lineDataSetRitaline.lineWidth = 2f
        lineDataSetRitaline.color = Color.LTGRAY
        lineDataSetRitaline.circleRadius = 6f
        lineDataSetRitaline.setDrawCircles(true)
        lineDataSetRitaline.setDrawValues(true)
        lineDataSetRitaline.valueTextColor = Color.WHITE
        lineDataSetRitaline.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        lineDataSetRitaline.axisDependency = lineChart.axisRight.axisDependency
        return  lineDataSetRitaline
    }

    private fun getMoy(entry : ArrayList<Entry>, lineChart: LineChart, label : String, color : Int = Color.RED) : ILineDataSet {
        val toReturn = ArrayList<Entry>()
        entry.reverse()
        if (entry.isEmpty()) return LineDataSet(toReturn, "Mood")

        val dateArray = ArrayList<LocalDate>()
        val dateInit = Date(entry[0].x.toLong())
        log.info("first date entry moy $dateInit")
        var dateInitLocal = LocalDate.of(dateInit.year, dateInit.month + 1, dateInit.date)
        dateInitLocal = dateInitLocal.plusDays(moy-2)
        for (e in entry) {
            val dateTemp = Date(e.x.toLong())
            val date = LocalDate.of(dateTemp.year, dateTemp.month + 1, dateTemp.date)
            if (date > dateInitLocal) {
                dateArray.add(date)
                dateInitLocal = date
            }
        }
        for (d in dateArray) {
            var sum = 0f
            var sum_div = 0
            val d_min = d.minusDays(moy-1)
            var lastEntry = entry[0]
            for (e in entry) {
                val dateTemp = Date(e.x.toLong())
                val date = LocalDate.of(dateTemp.year, dateTemp.month + 1, dateTemp.date)
                if (date >= d_min && date <= d) {
                    if (e.y != 0f) {
                        sum += e.y
                        sum_div++
                    }
                }
                if (date > d) {
                    if (sum_div !=0 )
                        toReturn.add(Entry(e.x,sum/sum_div))
                    break
                }
                lastEntry = e
            }
        }

        Collections.sort(toReturn, EntryXComparator())
        val lineDataSetMood = LineDataSet(toReturn, label)
        lineDataSetMood.valueTextSize = 10F
        lineDataSetMood.lineWidth = 2f
        lineDataSetMood.color = color
        lineDataSetMood.setDrawCircles(false)
        lineDataSetMood.setDrawValues(false)
        lineDataSetMood.valueTextColor = Color.WHITE
        lineDataSetMood.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        lineDataSetMood.axisDependency = lineChart.axisLeft.axisDependency
        return  lineDataSetMood
    }

    private fun getMoodListFromJSON(jsonString: String): ArrayList<MoodEntryModel> {
        val moodList = ArrayList<MoodEntryModel>()

        if (jsonString.isNotEmpty()) {
            val gson = GsonBuilder().create()
            val type = object: TypeToken<Array<MoodEntryModelToJson>>() {}.type
            val moodEntries = gson.fromJson<Array<MoodEntryModelToJson>>(jsonString, type)

            for(x in moodEntries.indices) {
                moodList.add(moodEntries[x].MoodEntryModel())
            }
        }

        return moodList
    }
}