package com.niaouh.moodtracker


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fasterxml.jackson.databind.MappingIterator
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.niaouh.moodtracker.model.MoodEntryModel
import com.niaouh.moodtracker.utils.ResUtil.getTimeStringFR
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.niaouh.moodtracker.alarm_rc.AlarmAdapter
import com.niaouh.moodtracker.model.updateTime
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class SettingsActivity() : AppCompatActivity() {

    private lateinit var getImportJsonFileResult: ActivityResultLauncher<Intent>
    private lateinit var getImportCSVFileResult: ActivityResultLauncher<Intent>
    private lateinit var getExportJsonFileResult: ActivityResultLauncher<Intent>
    private lateinit var getExportCSVFileResult: ActivityResultLauncher<Intent>
    private var moodData = ArrayList<MoodEntryModel>()

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val sMoodNumerals: Switch = findViewById(R.id.sMoodNumerals)
        val sModeNote: Switch = findViewById(R.id.sModeNote)
        val sReminder: Switch = findViewById(R.id.sReminder)
        val tvReminderTime: TextView = findViewById(R.id.tvReminderTime)
        val tvSettingsImport: TextView = findViewById(R.id.tvSettingsImport)
        val tvSettingsImportCSV: TextView = findViewById(R.id.tvSettingsImportCSV)
        val tvSettingsExport: TextView = findViewById(R.id.tvSettingsExport)
        val tvSettingsExportCSV: TextView = findViewById(R.id.tvSettingsExportCSV)
        val etMedicationName: EditText = findViewById(R.id.etMedictaionName)
        val bSettingsConfirm: ImageButton = findViewById(R.id.bSettingsConfirm)
        val ibAddAlarm: ImageButton = findViewById(R.id.ibAddAlarm)
        val rvAlarm: RecyclerView = findViewById(R.id.recyclerViewSettings)
        val llRecycle: LinearLayout = findViewById(R.id.llRecyclerView)
        val dataImport = ArrayList<MoodEntryModel>()

        val alarmRecycleView = AlarmAdapter(Settings.notificationList, llRecycle)
        rvAlarm.adapter = alarmRecycleView
        rvAlarm.layoutManager = LinearLayoutManager(this)

        val securityHandler = SecurityHandler(applicationContext)
        val secureFileHandler = SecureFileHandler(securityHandler)

        val jsonString = secureFileHandler.read()
        if (jsonString != "") moodData = getMoodListFromJSON(jsonString)

        sMoodNumerals.isChecked = Settings.moodMode == Settings.MoodModes.NUMBERS
        sMoodNumerals.isChecked = Settings.fatigueMode == Settings.FatigueModes.NUMBERS
        sModeNote.isChecked = Settings.modeNote
        etMedicationName.setText(Settings.medicationName)

        sReminder.isChecked = Settings.notificationAct
        val timeReminder = getString(R.string.settings_reminder_time) + " " + getTimeStringFR(Settings.notificationTime)
        tvReminderTime.text = timeReminder
        tvReminderTime.visibility = if(Settings.notificationAct) View.VISIBLE
                else View.INVISIBLE

        val dtPickerTime = TimePicker()
        dtPickerTime.onUpdateListener = {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            val time = timeFormat.format(it.time)
            val timeReminder = getString(R.string.settings_reminder_time) + " " + getTimeStringFR(time)
            Settings.notificationTime = time
            tvReminderTime.text = timeReminder
            deleteNotif(this)
            createNotif(this, time)
        }
        val dtPickerTimeRV = TimePicker()
        dtPickerTimeRV.onUpdateListener = {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            val time = timeFormat.format(it.time)
            alarmRecycleView.addAlarm(time, this)
        }

        sMoodNumerals.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) { Settings.moodMode = Settings.MoodModes.NUMBERS; Settings.fatigueMode = Settings.FatigueModes.NUMBERS }
            else {Settings.moodMode = Settings.MoodModes.FACES; Settings.fatigueMode = Settings.FatigueModes.FACES }
        }

        sModeNote.setOnCheckedChangeListener { _, isChecked ->
            Settings.modeNote = isChecked
        }

        sReminder.setOnCheckedChangeListener { _, isChecked ->
            Settings.notificationAct = isChecked
            if (isChecked) {
                val newText = getString(R.string.settings_reminder_time) + " " + getTimeStringFR(Settings.notificationTime)
                tvReminderTime.text = newText
                tvReminderTime.visibility = View.VISIBLE
                deleteNotif(this)
                createNotif(this, Settings.notificationTime)
            }
            else {
                tvReminderTime.visibility = View.INVISIBLE
                deleteNotif(this)
            }
        }

        tvReminderTime.setOnClickListener {
            dtPickerTime.show(this, Settings.notificationTime)
        }

        ibAddAlarm.setOnClickListener {
            dtPickerTimeRV.show(this)
        }

         tvSettingsExport.setOnClickListener {
            val intent = Intent()
                .setType("text/json")
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_TITLE, "mood_tracker_export.json")
                .setAction(Intent.ACTION_CREATE_DOCUMENT)
            getExportJsonFileResult.launch(intent)
        }
        
        tvSettingsExportCSV.setOnClickListener {
            val intent = Intent()
                .setType("text/csv")
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_TITLE, "mood_tracker_export.csv")
                .setAction(Intent.ACTION_CREATE_DOCUMENT)
            getExportCSVFileResult.launch(intent)
        }

        tvSettingsImport.setOnClickListener {
            val intent = Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)

            run { getImportJsonFileResult.launch(intent) }
        }

        tvSettingsImportCSV.setOnClickListener {
            val intent = Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)

            run { getImportCSVFileResult.launch(intent) }
        }

        bSettingsConfirm.setOnClickListener {
            val finishIntent = Intent()
            if (dataImport.isNotEmpty()) {
                finishIntent.putExtra("MoodEntries", dataImport)
            }
            setResult(RESULT_OK, finishIntent)
            Settings.medicationName = etMedicationName.text.toString()
            finish()
        }

        getExportJsonFileResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { exportResult ->
            try {
                val gson = Gson()
                val data = exportResult.data?.data
                val outStream = data?.let { contentResolver.openOutputStream(it, "w") }
                val jsonString = gson.toJson(moodData)
                outStream?.write(jsonString.toByteArray())
                outStream?.flush()
                outStream?.close()
            } catch (e: Exception) {
                Toast.makeText(this, "Unable to write to file", Toast.LENGTH_SHORT).show()
            }

        }

        getExportCSVFileResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { exportResult ->
            try {
                val data = exportResult.data?.data
                val outStream = data?.let { contentResolver.openOutputStream(it, "w") }
                val writer = outStream?.bufferedWriter()
                writer?.write("date|time|mood|fatigue|note|ritaline|key|lastUpdated")
                for (m in moodData) {
                    writer?.newLine()
                    writer?.write("${m.date}|${m.time}|${m.mood}|${m.fatigue}|${m.note.replace("\n","/n")}|${m.ritaline}|${m.key}|${m.lastUpdated}")
                }
                writer?.flush()
                outStream?.close()
            } catch (e: Exception) {
                Toast.makeText(this, "Unable to write to file", Toast.LENGTH_SHORT).show()
            }

        }

        getImportJsonFileResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                val path = activityResult.data?.data
                var inputAsString = ""
                var exception = 0

                try {
                    val jsonFile = path?.let { it -> contentResolver.openInputStream(it) }
                    inputAsString =
                        jsonFile?.bufferedReader().use { it?.readText() ?: "Failed to read" }
                } catch (e: Exception) {
                    Toast.makeText(this, "Unable to open file", Toast.LENGTH_SHORT).show()
                    exception = 1
                }

                if (inputAsString.isNotEmpty() && exception == 0) {
                    val gson = GsonBuilder().create()
                    val type = object : TypeToken<Array<HashMap<String, String>>>() {}.type

                    try {
                        val moodEntryList =
                            gson.fromJson<Array<HashMap<String, String>>>(inputAsString, type)

                        for (mood in moodEntryList) {
                            var date = "1987-11-06"
                            var exceptions = 0
                            try {
                                val format =
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
                                val localDate = LocalDate.parse(mood["date"])
                                date = format.format(localDate)
                            } catch (e: Exception) {
                                exceptions++
                            }

                            try {
                                val format =
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
                                val localDate = LocalDate.parse(
                                    mood["date"],
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                )
                                date = format.format(localDate)
                                exceptions = 0
                            } catch (e: Exception) {
                                exceptions++
                            }

                            val time = mood["time"]?.substring(0, 5)

                            if (exceptions != 0) {
                                Toast.makeText(
                                    this,
                                    "Date must be of format yyyy-MM-dd",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            val key = when (mood["key"]) {
                                null -> UUID.randomUUID().toString()
                                else -> mood["key"]
                            }

                            val lastUpdated = if (mood["lastUpdated"] != null) mood["lastUpdated"]
                            else LocalDateTime.now().toString()

                            val note = if (mood["note"] != null) mood["note"].toString()
                                else ""

                            if (mood["mood"] != null) {
                                if (mood["mood"]!!.toInt() in 6..10) Settings.moodMax = 10
                                else if (mood["mood"]!!.toInt() > Settings.moodMax) Settings.moodMax = mood["mood"]?.toInt() ?: 5
                            }
                            if (mood["fatigue"] != null) {
                                if (mood["fatigue"]!!.toInt() in 6..10) Settings.moodMax = 10
                                else if (mood["fatigue"]!!.toInt() > Settings.moodMax) Settings.moodMax = mood["fatigue"]?.toInt() ?: 5
                            }

                            val ritaline =
                                if (mood["ritaline"] != null) mood["ritaline"].toString()
                                else ""

                            dataImport.add(
                                MoodEntryModel(
                                    date,
                                    time.toString(),
                                    mood["mood"].toString().toInt(),
                                    mood["fatigue"].toString().toInt(),
                                    note,
                                    ritaline,
                                    key.toString(),
                                    lastUpdated.toString()
                                )
                            )
                        }
                        Toast.makeText(this, "File processed correctly", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Unable to process as JSON", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        getImportCSVFileResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                val path = activityResult.data?.data
                var inputAsString = ""
                var exception = 0

                try {
                    val csvFile = path?.let {contentResolver.openInputStream(it) }
                    inputAsString =
                        csvFile?.bufferedReader().use { it?.readText() ?: "Failed to read" }
                } catch (e: Exception) {
                    Toast.makeText(this, "Unable to open file", Toast.LENGTH_SHORT).show()
                    exception = 1
                }

                if (inputAsString.isNotEmpty() && exception == 0) {
                    val moodEntryList: MutableList<Map<String, String>> = LinkedList()
                    val moodE = inputAsString.split("\n")
                    val header = moodE.first().replace(" ","").split("|")
                    val nMoodE = moodE.subList(1, moodE.size)
                    for (next in nMoodE) {
                        val value = next.split("|")
                        val mapValue = mutableMapOf<String, String>()
                        for (p in header.zip(value)) {
                            mapValue[p.first] = p.second
                        }
                        moodEntryList.add(mapValue)
                    }

                    for (mood in moodEntryList) {
                        var date = "1987-11-06"
                        var exceptions = 0
                        try {
                            val format =
                                DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
                            val localDate = LocalDate.parse(mood["date"])
                            date = format.format(localDate)
                        } catch (e: Exception) {
                            exceptions++
                        }

                        val time = when (mood["time"]) {
                            null -> "20:00"
                            else -> mood["time"].toString().substring(0, 5)
                        }

                        if (exceptions != 0) {
                            Toast.makeText(
                                this,
                                "Date must be of format yyyy-MM-dd not ${mood["date"]}",
                                Toast.LENGTH_SHORT
                            ).show()
                            continue
                        }

                        val key = when (mood["key"]) {
                            null -> UUID.randomUUID().toString()
                            "" -> UUID.randomUUID().toString()
                            else -> mood["key"].toString()
                        }

                        val lastUpdated =
                            if (mood["lastUpdated"] != null) mood["lastUpdated"].toString()
                            else LocalDateTime.now().toString()

                        val moodValue = when (mood["mood"]) {
                            null -> 0
                            "" -> 0
                            else -> mood["mood"].toString().toInt()
                        }
                        if (moodValue in 6..10) Settings.moodMax = 10

                        val fatigueValue = when (mood["fatigue"]) {
                            null -> 0
                            "" -> 0
                            else -> mood["fatigue"].toString().toInt()
                        }
                        if (fatigueValue in 6..10) Settings.moodMax = 10

                        val ritaline =
                            if (mood["ritaline"] != null) mood["ritaline"].toString()
                            else ""

                        val note =
                            if (mood["note"] != null) mood["note"].toString().replace("/n","\n")
                            else ""

                        dataImport.add(
                            MoodEntryModel(
                                date,
                                time,
                                moodValue,
                                fatigueValue,
                                note,
                                ritaline,
                                key,
                                lastUpdated
                            )
                        )
                    }
                    Toast.makeText(this, "File processed correctly", Toast.LENGTH_SHORT)
                        .show()
                    }
                }
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