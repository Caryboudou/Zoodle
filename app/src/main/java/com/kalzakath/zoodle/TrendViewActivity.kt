package com.kalzakath.zoodle

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
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
import com.kalzakath.zoodle.model.updateDateTime
import com.kalzakath.zoodle.utils.ResUtil
import com.kalzakath.zoodle.utils.ResUtil.getDateStringFR
import com.kalzakath.zoodle.utils.ResUtil.getTimeStringFR
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class TrendViewActivity : AppCompatActivity() {

    private var moodData = ArrayList<MoodEntryModel>()
    private var maxDate: LocalDate = LocalDate.now()
    private var minDate: LocalDate = LocalDate.now()

    class MyFormat(val context: Context): ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
           return value.toString()
        }
    }

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
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val dateFormatLocal = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        var date = dateFormatLocal.format(LocalDate.now())

        bendDate.text = getDateStringFR(date)
        maxDate = LocalDate.parse(date, dateFormatLocal)

        date = dateFormatLocal.format(LocalDate.now().minusMonths(1))
        binitDate.text = getDateStringFR(date)
        minDate = LocalDate.parse(date, dateFormatLocal)

        val dtPickerInit = DatePicker()
        dtPickerInit.onUpdateListenerTrend = {
            date = dateFormat.format(it.time)
            minDate = LocalDate.parse(date, dateFormatLocal)
            binitDate.text = getDateStringFR(date)
            setLineChartData(cMood.isChecked, cFatigue.isChecked)
        }

        val dtPickerEnd = DatePicker()
        dtPickerEnd.onUpdateListenerTrend = {
            date = dateFormat.format(it.time)
            maxDate = LocalDate.parse(date, dateFormatLocal)
            bendDate.text = getDateStringFR(date)
            setLineChartData(cMood.isChecked, cFatigue.isChecked)
        }
        
        if (moodData.isNotEmpty()) setLineChartData(cMood.isChecked, cFatigue.isChecked)

        cMood.setOnClickListener {
            setLineChartData(cMood.isChecked, cFatigue.isChecked)
        }

        cFatigue.setOnClickListener {
            setLineChartData(cMood.isChecked, cFatigue.isChecked)
        }

        bReset.setOnClickListener {
            date = dateFormatLocal.format(LocalDate.now())
            bendDate.text = getDateStringFR(date)
            maxDate = LocalDate.parse(date, dateFormatLocal)

            date = dateFormatLocal.format(LocalDate.now().minusMonths(1))
            binitDate.text = getDateStringFR(date)
            minDate = LocalDate.parse(date, dateFormatLocal)

            setLineChartData(cMood.isChecked, cFatigue.isChecked)
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

    private fun setLineChartData(cMood: Boolean, cFatigue: Boolean) {
        val entryListMood: ArrayList<Entry> = ArrayList()
        val entryListFatigue: ArrayList<Entry> = ArrayList()
        var dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH )
        val dateFormatLocal = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        var dateIt : LocalDate

        for (moods in moodData) {
            var moodNumber = moods.mood
            var fatigueNumber = moods.fatigue

            if (Settings.moodMode == Settings.MoodModes.NUMBERS) { moodNumber = moods.mood; fatigueNumber = moods.fatigue }
            dateIt = LocalDate.parse(moods.date, dateFormatLocal)

            if (dateIt <= maxDate && dateIt > minDate) {
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
        data.setValueFormatter(ChartValueFormatter())

        val chart: LineChart = findViewById(R.id.getTheGraph)
        val legend = chart.legend
        legend.textColor = Color.WHITE

        chart.data = data
        chart.setBackgroundColor(Color.BLACK)
        chart.animateXY(0, 0, Easing.EaseInCubic)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.WHITE
        xAxis.valueFormatter = ChartValueFormatter()
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