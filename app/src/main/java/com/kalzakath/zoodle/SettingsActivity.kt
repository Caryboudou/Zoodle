package com.kalzakath.zoodle


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.kalzakath.zoodle.model.MoodEntryModel
import com.kalzakath.zoodle.utils.ResUtil.getDateStringFR
import com.kalzakath.zoodle.utils.ResUtil.getTimeStringFR
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class SettingsActivity() : AppCompatActivity() {

    private lateinit var getImportJsonFileResult: ActivityResultLauncher<Intent>
    private lateinit var getExportJsonFileResult: ActivityResultLauncher<Intent>

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val sMoodNumerals: Switch = findViewById(R.id.sMoodNumerals)
        val sReminder: Switch = findViewById(R.id.sReminder)
        val tvReminderTime: TextView = findViewById(R.id.tvReminderTime)
        val tvSettingsImport: TextView = findViewById(R.id.tvSettingsImport)
        val tvSettingsExport: TextView = findViewById(R.id.tvSettingsExport)
        val bSettingsConfirm: ImageButton = findViewById(R.id.bSettingsConfirm)
        val dataImport = ArrayList<MoodEntryModel>()

        var parcelData: ArrayList<Parcelable>? = null
        val moodData = ArrayList<MoodEntryModel>()
        val moodEntries = intent.getParcelableArrayListExtra<Parcelable>("MoodEntries")
        if (moodEntries != null) parcelData = intent.getParcelableArrayListExtra<Parcelable>("MoodEntries")
        parcelData?.filterIsInstanceTo(moodData)

        sMoodNumerals.isChecked = Settings.moodMode == Settings.MoodModes.NUMBERS
        sMoodNumerals.isChecked = Settings.fatigueMode == Settings.FatigueModes.NUMBERS

        sReminder.isChecked = Settings.notificationAct
        val timeReminder = getString(R.string.settings_reminder_time) + " " + getTimeStringFR(Settings.notificationTime)
        tvReminderTime.text = timeReminder
        tvReminderTime.visibility = if(Settings.notificationAct) View.VISIBLE
                else View.INVISIBLE

        sMoodNumerals.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) { Settings.moodMode = Settings.MoodModes.NUMBERS; Settings.fatigueMode = Settings.FatigueModes.NUMBERS }
            else {Settings.moodMode = Settings.MoodModes.FACES; Settings.fatigueMode = Settings.FatigueModes.FACES }
        }

        sReminder.setOnCheckedChangeListener { _, isChecked ->
            Settings.notificationAct = isChecked
            if (isChecked) {
                val newText = getString(R.string.settings_reminder_time) + getTimeStringFR(Settings.notificationTime)
                tvReminderTime.text = newText
                tvReminderTime.visibility = View.VISIBLE
                createNotif(this, Settings.notificationTime)
            }
            else {
                tvReminderTime.visibility = View.INVISIBLE
                deleteNotif(this)
            }
        }

        tvReminderTime.setOnClickListener {
            getNewTime(tvReminderTime)
        }

        tvSettingsExport.setOnClickListener {
            val intent = Intent()
                .setType("text/json")
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_TITLE, "zoodle_export.json")
                .setAction(Intent.ACTION_CREATE_DOCUMENT)
            getExportJsonFileResult.launch(intent)
        }

        tvSettingsImport.setOnClickListener {
            val intent = Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)

            run { getImportJsonFileResult.launch(intent) }
        }

        bSettingsConfirm.setOnClickListener {
            val finishIntent = Intent()
            if (dataImport.isNotEmpty()) finishIntent.putParcelableArrayListExtra("MoodEntries", dataImport.filterIsInstanceTo(ArrayList()))
            setResult(RESULT_OK, finishIntent)
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
                            val moodFeelings = when (mood["feelings"]) {
                                null -> ArrayList<String>()
                                else -> mood["feelings"]?.let { (it.split(",")) } as MutableList<String>
                            }
                            val moodActivities = when (mood["activities"]) {
                                null -> ArrayList<String>()
                                else -> mood["activities"]?.let { (it.split(",")) } as MutableList<String>
                            }
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

                            if (mood["mood"] != null) {
                                if (mood["mood"]!!.toInt() in 6..10) Settings.moodMax = 10
                                else if (mood["mood"]!!.toInt() > Settings.moodMax) Settings.moodMax = mood["mood"]?.toInt() ?: 5
                            }
                            if (mood["fatigue"] != null) {
                                if (mood["fatigue"]!!.toInt() in 6..10) Settings.moodMax = 10
                                else if (mood["fatigue"]!!.toInt() > Settings.moodMax) Settings.moodMax = mood["fatigue"]?.toInt() ?: 5
                            }

                            dataImport.add(
                                MoodEntryModel(
                                    date,
                                    time.toString(),
                                    mood["mood"].toString().toInt(),
                                    mood["fatigue"].toString().toInt(),
                                    moodFeelings,
                                    moodActivities,
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
    }

    private fun getNewTime(tvReminderTime: TextView) {
        val ibClose: ImageButton = findViewById(R.id.closeTextView)
        val tpPicker: TimePicker = findViewById(R.id.timePicker)
        val bSave: Button = findViewById(R.id.saveTime)
        val clTimePicker: ConstraintLayout = findViewById(R.id.clock)

        tpPicker.setIs24HourView(true)
        tpPicker.hour = 20
        tpPicker.minute = 0

        clTimePicker.visibility = View.VISIBLE

        bSave.setOnClickListener {
            val hour = tpPicker.hour
            val minute = tpPicker.minute
            val hourStr = if (hour<10) "0$hour" else hour.toString()
            val minuteStr = if (minute<10) "0$minute" else minute.toString()
            Settings.notificationTime = "$hourStr:$minuteStr"

            val newText = getString(R.string.settings_reminder_time) + " " + getTimeStringFR(Settings.notificationTime)
            tvReminderTime.text = newText
            deleteNotif(this)
            createNotif(this, Settings.notificationTime)
            clTimePicker.visibility = View.INVISIBLE
        }

        ibClose.setOnClickListener{
            clTimePicker.visibility = View.INVISIBLE
        }
    }
}
