package com.niaouh.moodtracker


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.niaouh.moodtracker.model.MoodEntryModel
import com.niaouh.moodtracker.utils.ResUtil.getTimeStringFR
import com.niaouh.moodtracker.alarm_rc.AlarmAdapter
import com.niaouh.moodtracker.model.MoodEntryModelToJson
import com.niaouh.moodtracker.model.createNewEntry
import com.niaouh.moodtracker.trackerpopup.TrackerPopup
import com.niaouh.moodtracker.utils.ResUtil.getTimeStringEN
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.collections.ArrayList


class SettingsActivity() : AppCompatActivity() {

    private lateinit var getImportJsonFileResult: ActivityResultLauncher<Intent>
    private lateinit var getImportCSVFileResult: ActivityResultLauncher<Intent>
    private lateinit var getExportJsonFileResult: ActivityResultLauncher<Intent>
    private lateinit var getExportCSVFileResult: ActivityResultLauncher<Intent>
    private var moodData = ArrayList<MoodEntryModel>()
    private lateinit var trackerPopup: TrackerPopup
    private lateinit var tvTrakerName: TextView

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val sMoodNumerals: Switch = findViewById(R.id.sMoodNumerals)
        val sModeNote: Switch = findViewById(R.id.sModeNote)
        val sReminderForget: Switch = findViewById(R.id.sReminder)
        val spMaxMood: Spinner = findViewById(R.id.spMaxMood)
        val ibReminderTime: ImageButton = findViewById(R.id.ibReminderTime)
        val tvSettingsImport: TextView = findViewById(R.id.tvSettingsImport)
        val tvSettingsImportCSV: TextView = findViewById(R.id.tvSettingsImportCSV)
        val tvSettingsExport: TextView = findViewById(R.id.tvSettingsExport)
        val tvSettingsExportCSV: TextView = findViewById(R.id.tvSettingsExportCSV)
        val llTracker: LinearLayout = findViewById(R.id.llTracker)
        tvTrakerName = findViewById(R.id.tvTrakerName)
        val etMedicationName: EditText = findViewById(R.id.etMedictaionName)
        val bSettingsConfirm: ImageButton = findViewById(R.id.bSettingsConfirm)
        val ibAddAlarm: ImageButton = findViewById(R.id.ibAddAlarm)
        val rvAlarm: RecyclerView = findViewById(R.id.recyclerViewSettings)
        val llRecycle: LinearLayout = findViewById(R.id.llRecyclerView)
        val dataImport = ArrayList<MoodEntryModel>()

        ArrayAdapter.createFromResource(
            this,
            R.array.max_mood,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner.
            spMaxMood.adapter = adapter
            if (Settings.moodMax == 5)
                spMaxMood.setSelection(0)
            else spMaxMood.setSelection(1)
        }

        val alarmRecycleView = AlarmAdapter(Settings.notificationList, llRecycle)
        rvAlarm.adapter = alarmRecycleView
        rvAlarm.layoutManager = LinearLayoutManager(this)

        trackerPopup = TrackerPopup(this) { l -> finishTracker(l) }

        val securityHandler = SecurityHandler(applicationContext)
        val secureFileHandler = SecureFileHandler(securityHandler)

        val jsonString = secureFileHandler.read()
        if (jsonString != "") moodData = getMoodListFromJSON(jsonString)

        sMoodNumerals.isChecked = Settings.moodMode == Settings.MoodModes.NUMBERS
        sMoodNumerals.isChecked = Settings.fatigueMode == Settings.FatigueModes.NUMBERS
        sModeNote.isChecked = Settings.modeNote
        etMedicationName.setText(Settings.medicationName)
        tvTrakerName.text = getTrackerList()

        sReminderForget.isChecked = Settings.notificationAct
        val timeReminder =
            getString(R.string.settings_reminder_time_forget) + " " + getTimeStringFR(Settings.notificationTime)
        sReminderForget.text = timeReminder

        val dtPickerTime = TimePicker()
        dtPickerTime.onUpdateListener = {
            val time = LocalTime.of(it.get(Calendar.HOUR_OF_DAY), it.get(Calendar.MINUTE))
            val timeReminder =
                getString(R.string.settings_reminder_time_forget) + " " + getTimeStringFR(time)
            Settings.notificationTime = time
            sReminderForget.text = timeReminder
            deleteNotifForget(this)
            createNotifForget(this, time)
        }
        val dtPickerTimeRV = TimePicker()
        dtPickerTimeRV.onUpdateListener = {
            val time = LocalTime.of(it.get(Calendar.HOUR_OF_DAY), it.get(Calendar.MINUTE))
            alarmRecycleView.addAlarm(time, this)
        }

        llTracker.setOnClickListener {
            trackerPopup.showPopup(Settings.trackerList)
        }

        sMoodNumerals.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Settings.moodMode = Settings.MoodModes.NUMBERS; Settings.fatigueMode =
                    Settings.FatigueModes.NUMBERS
            } else {
                Settings.moodMode = Settings.MoodModes.FACES; Settings.fatigueMode =
                    Settings.FatigueModes.FACES
            }
        }

        sModeNote.setOnCheckedChangeListener { _, isChecked ->
            Settings.modeNote = isChecked
        }

        sReminderForget.setOnCheckedChangeListener { _, isChecked ->
            Settings.notificationAct = isChecked
            if (isChecked) {
                val newText =
                    getString(R.string.settings_reminder_time_forget) + " " + getTimeStringFR(
                        Settings.notificationTime
                    )
                sReminderForget.text = newText
                deleteNotifForget(this)
                createNotifForget(this, Settings.notificationTime)
            } else {
                deleteNotifForget(this)
            }
        }

        ibReminderTime.setOnClickListener {
            dtPickerTime.show(this, Settings.notificationTime)
        }

        ibAddAlarm.setOnClickListener {
            dtPickerTimeRV.show(this)
        }

        tvSettingsExport.setOnClickListener {
            val intent = Intent()
                .setType("text/json")
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(
                    Intent.EXTRA_TITLE,
                    "mood_tracker_export_med_${Settings.medicationName}.json"
                )
                .setAction(Intent.ACTION_CREATE_DOCUMENT)
            getExportJsonFileResult.launch(intent)
        }

        tvSettingsExportCSV.setOnClickListener {
            val intent = Intent()
                .setType("text/csv")
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(
                    Intent.EXTRA_TITLE,
                    "mood_tracker_export_med_${Settings.medicationName}.csv"
                )
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
                val data = arrayListOf<MoodEntryModelToJson>()
                for (m in dataImport) {
                    data.add(m.MoodEntryModelToJson())
                }
                finishIntent.putExtra("MoodEntries", data)
            }
            setResult(RESULT_OK, finishIntent)
            Settings.medicationName = etMedicationName.text.toString()
            Settings.trackerList.sort()
            Settings.moodMax =
                if (spMaxMood.selectedItemPosition == 0) 5
                else 9
            finish()
        }

        getExportJsonFileResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { exportResult ->
                try {
                    val gson = Gson()
                    val data = exportResult.data?.data
                    val outStream = data?.let { contentResolver.openOutputStream(it, "w") }
                    val jsonString = gson.toJson(moodData.map { m -> m.MoodEntryModelToJson() })
                    outStream?.write(jsonString.toByteArray())
                    outStream?.flush()
                    outStream?.close()
                } catch (e: Exception) {
                    Toast.makeText(this, "Unable to write to file", Toast.LENGTH_SHORT).show()
                }

            }

        getExportCSVFileResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { exportResult ->
                try {
                    val data = exportResult.data?.data
                    val outStream = data?.let { contentResolver.openOutputStream(it, "w") }
                    val writer = outStream?.bufferedWriter()
                    writer?.write("date|heure|mood|fatigue|note|medication|tracker|key|lastUpdated|")
                    for (m in moodData) {
                        writer?.newLine()
                        writer?.write(
                            "${m.textMoodDay()}|${getTimeStringEN(m.date)}|${m.mood}|${m.fatigue}|${
                                m.note.replace(
                                    "\n",
                                    "/n"
                                )
                            }|${m.ritaline}|"
                        )
                        for (t in m.trackers) {
                            writer?.write("$t;")
                        }
                        writer?.write("|${m.key}|${m.lastUpdated}|")
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
                            var year = 0
                            var month = 0
                            var day = 0
                            var hour = 0
                            var minute = 0
                            var exception = 0

                            try {
                                val localDate = LocalDate.parse(mood["date"])
                                year = localDate.year
                                month = localDate.monthValue
                                day = localDate.dayOfMonth
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this,
                                    "Date must be of format yyyy-MM-dd",
                                    Toast.LENGTH_SHORT
                                ).show()
                                exception++
                            }

                            try {
                                val localDate = LocalTime.parse(mood["time"])
                                hour = localDate.hour
                                minute = localDate.minute
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this,
                                    "Time must be of format hh:mm",
                                    Toast.LENGTH_SHORT
                                ).show()
                                exception++
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
                                else if (mood["mood"]!!.toInt() > Settings.moodMax) Settings.moodMax =
                                    mood["mood"]?.toInt() ?: 5
                            }
                            if (mood["fatigue"] != null) {
                                if (mood["fatigue"]!!.toInt() in 6..10) Settings.moodMax = 10
                                else if (mood["fatigue"]!!.toInt() > Settings.moodMax) Settings.moodMax =
                                    mood["fatigue"]?.toInt() ?: 5
                            }

                            val medication =
                                if (mood["medication"] != null) mood["medication"].toString()
                                else ""

                            val trackers = arrayListOf<String>()
                            val values = mood["tracker"]?.split(";") ?: listOf()
                            for (v in values) trackers.add(v)

                            if (exception == 0) {
                                dataImport.add(
                                    createNewEntry(
                                        year, month, day, hour, minute,
                                        mood["mood"].toString().toInt(),
                                        mood["fatigue"].toString().toInt(),
                                        note,
                                        medication,
                                        trackers,
                                        key.toString(),
                                        lastUpdated.toString()
                                    )
                                )
                                Toast.makeText(this, "File processed correctly", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
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
                    val csvFile = path?.let { contentResolver.openInputStream(it) }
                    inputAsString =
                        csvFile?.bufferedReader().use { it?.readText() ?: "Failed to read" }
                } catch (e: Exception) {
                    Toast.makeText(this, "Unable to open file", Toast.LENGTH_SHORT).show()
                    exception = 1
                }

                if (inputAsString.isNotEmpty() && exception == 0) {
                    val moodEntryList: MutableList<Map<String, String>> = LinkedList()
                    val moodE = inputAsString.split("\n")
                    val header = moodE.first().replace(" ", "").split("|")
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
                        var year = 0
                        var month = 0
                        var day = 0
                        var hour = 0
                        var minute = 0
                        var exception = 0

                        try {
                            val localDate = LocalDate.parse(mood["date"])
                            year = localDate.year
                            month = localDate.monthValue
                            day = localDate.dayOfMonth
                        } catch (e: Exception) {
                            Toast.makeText(
                                this,
                                "Date must be of format yyyy-MM-dd",
                                Toast.LENGTH_SHORT
                            ).show()
                            exception++
                        }

                        try {
                            val localDate = LocalTime.parse(mood["time"])
                            hour = localDate.hour
                            minute = localDate.minute
                        } catch (e: Exception) {
                            Toast.makeText(
                                this,
                                "Time must be of format hh:mm",
                                Toast.LENGTH_SHORT
                            ).show()
                            exception++
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

                        val medication =
                            if (mood["medication"] != null) mood["medication"].toString()
                            else ""

                        val note =
                            if (mood["note"] != null) mood["note"].toString().replace("/n", "\n")
                            else ""

                        val trackers = arrayListOf<String>()
                        val values = mood["tracker"]?.split(";") ?: listOf()
                        for (v in values) trackers.add(v)

                        if (exception == 0) {
                            val moodToAdd = createNewEntry(
                                year, month, day, hour, minute,
                                mood["mood"].toString().toInt(),
                                mood["fatigue"].toString().toInt(),
                                note,
                                medication,
                                trackers,
                                key,
                                lastUpdated
                            )
                            moodToAdd.trackers = trackers

                            dataImport.add(moodToAdd)
                            Toast.makeText(this, "File processed correctly", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
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

    private fun getTrackerList(): String {
        if (Settings.trackerList.size == 0) return "aucun"
        var s = Settings.trackerList[0]
        for (t in 1 until Settings.trackerList.size) {
            s = "$s, ${Settings.trackerList[t]}"
        }
        if (s.length > 20) {
            s = s.substring(0, 20) + "..."
        }
        return s
    }

    private fun finishTracker(newTrackerList: ArrayList<String>) {
        Settings.trackerList = newTrackerList
        tvTrakerName.text = getTrackerList()
    }
}