package com.kalzakath.zoodle

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
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
import com.kalzakath.zoodle.data.CircleFatigueBO
import com.kalzakath.zoodle.data.CircleMoodBO
import com.kalzakath.zoodle.model.MoodEntryModel
import com.kalzakath.zoodle.model.getRitalineInt
import com.kalzakath.zoodle.utils.ResUtil.getDateStringFR
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class TrendViewActivity : AppCompatActivity() {

    private var moodData = ArrayList<MoodEntryModel>()
    private var maxDate: LocalDate = LocalDate.now()
    private var minDate: LocalDate = LocalDate.now()
    private lateinit var mainLayout: ConstraintLayout


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

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val dateFormatLocal = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        var date = dateFormatLocal.format(LocalDate.now())

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
        maxDate = LocalDate.parse(date, dateFormatLocal)

        date = dateFormatLocal.format(LocalDate.now().minusMonths(1))
        binitDate.text = getDateStringFR(date)
        minDate = LocalDate.parse(date, dateFormatLocal)

        val dtPickerInit = DatePicker()
        dtPickerInit.onUpdateListener = {
            date = dateFormat.format(it.time)
            minDate = LocalDate.parse(date, dateFormatLocal)
            binitDate.text = getDateStringFR(date)
            setLineChartData(cMood.isChecked, cFatigue.isChecked, cRitaline.isChecked)
        }

        val dtPickerEnd = DatePicker()
        dtPickerEnd.onUpdateListener = {
            date = dateFormat.format(it.time)
            maxDate = LocalDate.parse(date, dateFormatLocal)
            bendDate.text = getDateStringFR(date)
            setLineChartData(cMood.isChecked, cFatigue.isChecked, cRitaline.isChecked)
        }
        
        if (moodData.isNotEmpty()) setLineChartData(cMood.isChecked, cFatigue.isChecked, cRitaline.isChecked)

        cMood.setOnClickListener {
            setLineChartData(cMood.isChecked, cFatigue.isChecked, cRitaline.isChecked)
        }

        cFatigue.setOnClickListener {
            setLineChartData(cMood.isChecked, cFatigue.isChecked, cRitaline.isChecked)
        }

        cRitaline.setOnClickListener {
            setLineChartData(cMood.isChecked, cFatigue.isChecked, cRitaline.isChecked)
        }

        bReset.setOnClickListener {
            date = dateFormatLocal.format(LocalDate.now())
            bendDate.text = getDateStringFR(date)
            maxDate = LocalDate.parse(date, dateFormatLocal)

            date = dateFormatLocal.format(LocalDate.now().minusMonths(1))
            binitDate.text = getDateStringFR(date)
            minDate = LocalDate.parse(date, dateFormatLocal)

            setLineChartData(cMood.isChecked, cFatigue.isChecked, cRitaline.isChecked)
        }

        binitDate.setOnClickListener {
            dtPickerInit.show(this)
        }

        bendDate.setOnClickListener {
            dtPickerEnd.show(this)
        }

        bViewData.setOnClickListener {
            finish()
        }
    }

    private fun setLineChartData(cMood: Boolean, cFatigue: Boolean, cRitaline: Boolean) {
        var entryListMood: ArrayList<Entry> = ArrayList()
        var entryListFatigue: ArrayList<Entry> = ArrayList()
        var entryListRitaline: ArrayList<Entry> = ArrayList()
        val lines = mutableListOf<ILineDataSet>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd,HH:mm", Locale.ENGLISH )
        val dateFormatLocal = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        var dateIt : LocalDate
        var colorsMood = mutableListOf<Int>()
        var colorsFatigue = mutableListOf<Int>()

        val chart: LineChart = findViewById(R.id.getTheGraph)

        for (moods in moodData) {
            var moodNumber = moods.mood
            var fatigueNumber = moods.fatigue
            val ritalineNumber = moods.getRitalineInt()
            val dateTime = moods.date+","+moods.time
            val xDate = dateFormat.parse(dateTime)?.time?.toFloat() ?: 0.0.toFloat()

            if (Settings.moodMode == Settings.MoodModes.NUMBERS) { moodNumber = moods.mood; fatigueNumber = moods.fatigue }
            dateIt = LocalDate.parse(moods.date, dateFormatLocal)

            if (dateIt <= maxDate && dateIt > minDate) {
                if (cMood) {
                    val entryMood = Entry(xDate,moodNumber.toFloat())
                    if (moodNumber != 0) {
                        entryListMood.add(entryMood)
                        colorsMood.add(applicationContext.getColor(CircleMoodBO.from(moodNumber).colorId))
                    } else if (entryListMood.isNotEmpty()) {
                        val lineDataSet = initMoodLine(entryListMood, colorsMood, chart)
                        lines.add(lineDataSet)
                        entryListMood = ArrayList()
                        colorsMood = ArrayList()
                    }
                }

                if (cFatigue) {
                    if (fatigueNumber != 0) {
                        entryListFatigue.add(
                            Entry(xDate, fatigueNumber.toFloat())
                        )
                        colorsFatigue.add(
                            applicationContext.getColor(
                                CircleFatigueBO.from(
                                    fatigueNumber
                                ).colorId
                            )
                        )
                    } else if (entryListFatigue.isNotEmpty()) {
                        val lineDataSet = initFatigueLine(entryListFatigue, colorsFatigue, chart)
                        lines.add(lineDataSet)
                        entryListFatigue = ArrayList()
                        colorsFatigue = ArrayList()
                    }
                }

                if (cRitaline) {
                    if (ritalineNumber != 0) {
                        entryListRitaline.add(
                            Entry(xDate, ritalineNumber.toFloat())
                        )
                    } else if (entryListRitaline.isNotEmpty()) {
                        val lineDataSet = initRitalineLine(entryListRitaline, chart)
                        lines.add(lineDataSet)
                        entryListRitaline = ArrayList()
                    }
                }
            }
        }
        val lineDataSetMood = initMoodLine(entryListMood, colorsMood, chart)

        val lineDataSetFatigue = initFatigueLine(entryListFatigue, colorsFatigue, chart)

        val lineDataSetRitaline = initRitalineLine(entryListRitaline, chart)

        if (cMood) { lines.add(lineDataSetMood) }
        if (cFatigue) { lines.add(lineDataSetFatigue) }
        if (cRitaline) { lines.add(lineDataSetRitaline) }

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
        xAxis.labelRotationAngle = 90f
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

    private fun getMoodListFromJSON(jsonString: String): ArrayList<MoodEntryModel> {
        val moodList = ArrayList<MoodEntryModel>()

        if (jsonString.isNotEmpty()) {
            val gson = GsonBuilder().create()
            val type = object: TypeToken<Array<MoodEntryModel>>() {}.type
            val moodEntries = gson.fromJson<Array<MoodEntryModel>>(jsonString, type)

            for(x in moodEntries.indices) {
                moodList.add(moodEntries[x])
            }
        }

        return moodList
    }
}