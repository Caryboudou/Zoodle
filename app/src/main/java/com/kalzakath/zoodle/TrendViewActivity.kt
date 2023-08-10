package com.kalzakath.zoodle

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.animation.Easing
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
import com.kalzakath.zoodle.model.MoodEntryModel
import java.text.SimpleDateFormat
import java.util.*


class TrendViewActivity : AppCompatActivity() {

    private var moodData = ArrayList<MoodEntryModel>()
    private var filter = "default"

    class MyFormat(val context: Context): ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
           when (Settings.moodMode) {
               Settings.MoodModes.NUMBERS -> return value.toString()
           }
            return ""
        }
    }

    class ChartValueFormatter(private val filter: String): ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val formatter = when (filter) {
                "year" -> SimpleDateFormat("yyyy-MM", Locale.ENGLISH)
                "month" -> SimpleDateFormat("yyyy-MM", Locale.ENGLISH)
                else -> SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            }
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

        val bViewData: Button = findViewById(R.id.bViewData)
        val bReset: Button = findViewById(R.id.bTrendReset)
        val bMonth: Button = findViewById(R.id.bTrendMonth)
        val bYear: Button = findViewById(R.id.bTrendYear)
        val cMood: CheckBox = findViewById(R.id.checkMood)
        val cFatigue: CheckBox = findViewById(R.id.checkFatigue)

        if (moodData.isNotEmpty()) setLineChartData(cMood.isChecked, cFatigue.isChecked)

        cMood.setOnClickListener {
            setLineChartData(cMood.isChecked, cFatigue.isChecked)
        }

        cFatigue.setOnClickListener {
            setLineChartData(cMood.isChecked, cFatigue.isChecked)
        }

        bReset.setOnClickListener {
            filter = "default"
            setLineChartData(cMood.isChecked, cFatigue.isChecked)
        }

        bMonth.setOnClickListener {
            filter = "month"
            setLineChartData(cMood.isChecked, cFatigue.isChecked)
        }

        bYear.setOnClickListener {
            filter = "year"
            setLineChartData(cMood.isChecked, cFatigue.isChecked)
        }

        bViewData.setOnClickListener {
            finish()
        }
    }

    private fun setLineChartData(cMood: Boolean, cFatigue: Boolean) {
        val entryListMood: ArrayList<Entry> = ArrayList()
        val entryListFatigue: ArrayList<Entry> = ArrayList()
        val timeArrayMood = mutableMapOf<String, ArrayList<Int>>()
        val timeArrayFatigue = mutableMapOf<String, ArrayList<Int>>()
        var subStringLength = 4
        var dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH )
        var format = "yyyy"

        for (moods in moodData) {
            var moodNumber = moods.mood
            var fatigueNumber = moods.fatigue
            var timePeriod = ""

            if (Settings.moodMode == Settings.MoodModes.NUMBERS) { moodNumber = moods.mood; fatigueNumber = moods.fatigue }

            if (filter == "month") {
                format = "yyyy-MM"
                subStringLength = 7
            } else if (filter == "year") format = "yyyy"

            if (filter != "default") {
                dateFormat = SimpleDateFormat(
                    format,
                    Locale.ENGLISH
                )

                if (timePeriod == "" || timePeriod != moods.date.substring(0, subStringLength)) {
                    timePeriod =
                        moods.date.substring(0, subStringLength)
                    if (timeArrayMood[timePeriod] == null) timeArrayMood[timePeriod] = ArrayList()
                    val valueMood = moods.mood
                    timeArrayMood[timePeriod]?.add(valueMood)

                    if (timeArrayFatigue[timePeriod] == null) timeArrayFatigue[timePeriod] = ArrayList()
                    val valueFatigue = moods.fatigue
                    timeArrayFatigue[timePeriod]?.add(valueFatigue)
                }
            } else {
                entryListMood.add(
                    Entry(
                        dateFormat.parse(moods.date)?.time?.toFloat() ?: 0.0.toFloat(),
                        (moodNumber.toFloat())
                    )
                )
                entryListFatigue.add(
                    Entry(
                        dateFormat.parse(moods.date)?.time?.toFloat() ?: 0.0.toFloat(),
                        (fatigueNumber.toFloat())
                    )
                )
            }
        }

        if (filter != "default") {
            for ((key) in timeArrayMood) {
                var average = 0
                for (data in timeArrayMood[key]!!) {
                    average += data
                }
                average /= timeArrayMood[key]!!.size
                entryListMood.add(
                Entry(
                    dateFormat.parse(key)?.time?.toFloat() ?: 0.0.toFloat(),
                    (average.toFloat())
                ))
            }
            for ((key) in timeArrayFatigue) {
                var average = 0
                for (data in timeArrayFatigue[key]!!) {
                    average += data
                }
                average /= timeArrayFatigue[key]!!.size
                entryListFatigue.add(
                Entry(
                    dateFormat.parse(key)?.time?.toFloat() ?: 0.0.toFloat(),
                    (average.toFloat())
                ))
            }
        }

        Collections.sort(entryListMood, EntryXComparator())
        Collections.sort(entryListFatigue, EntryXComparator())

        val lineDataSetMood = LineDataSet(entryListMood, "Mood")
        lineDataSetMood.color = Color.WHITE
        lineDataSetMood.circleRadius = 1f
        lineDataSetMood.valueTextSize = 10F
        lineDataSetMood.lineWidth = 2f
        lineDataSetMood.fillColor = Color.WHITE
        lineDataSetMood.valueTextColor = Color.WHITE
        lineDataSetMood.mode = LineDataSet.Mode.HORIZONTAL_BEZIER

        val lineDataSetFatigue = LineDataSet(entryListFatigue, "Fatigue")
        lineDataSetFatigue.color = Color.BLUE
        lineDataSetFatigue.circleRadius = 1f
        lineDataSetFatigue.valueTextSize = 10F
        lineDataSetFatigue.lineWidth = 2f
        lineDataSetFatigue.fillColor = Color.WHITE
        lineDataSetFatigue.valueTextColor = Color.WHITE
        lineDataSetFatigue.mode = LineDataSet.Mode.HORIZONTAL_BEZIER

        val lines = MutableList<ILineDataSet>(0) { _ -> lineDataSetMood}
        if (cMood) { lines.add(lineDataSetMood) }
        if (cFatigue) { lines.add(lineDataSetFatigue) }

        val data = LineData(lines)
        data.setValueTextColor(Color.WHITE)
        data.setValueFormatter(ChartValueFormatter(filter))

        val chart: LineChart = findViewById(R.id.getTheGraph)
        val legend = chart.legend
        legend.textColor = Color.WHITE

        chart.data = data
        chart.setBackgroundColor(Color.BLACK)
        chart.animateXY(0, 0, Easing.EaseInCubic)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.WHITE
        xAxis.valueFormatter = ChartValueFormatter(filter)
        xAxis.labelRotationAngle = 90f
        xAxis.setDrawGridLines(false)


        val yAxis = chart.axisLeft
        yAxis.textColor = Color.WHITE
        yAxis.axisMaximum = Settings.moodMax.toFloat()
        yAxis.axisMinimum = 0f
        yAxis.granularity = 1f
        if (moodData.isNotEmpty()) if (Settings.moodMode == Settings.MoodModes.FACES) yAxis.valueFormatter = MyFormat(applicationContext
        )
        yAxis.setDrawGridLines(false)
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