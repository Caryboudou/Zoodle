package com.kalzakath.zoodle

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.kalzakath.zoodle.debug.TestSuite
import com.kalzakath.zoodle.interfaces.DataController
import com.kalzakath.zoodle.interfaces.MainActivityInterface
import com.kalzakath.zoodle.interfaces.RowEntryModel
import com.kalzakath.zoodle.layout.ChooseFatigueCircle
import com.kalzakath.zoodle.layout.ChooseMoodCircle
import com.kalzakath.zoodle.model.MoodEntryModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Logger

class MainActivity : AppCompatActivity(), MainActivityInterface {

    private lateinit var getActivitiesActivityResult: ActivityResultLauncher<Intent>
    private lateinit var getSettingsActivityResult: ActivityResultLauncher<Intent>
    private lateinit var getFeelingsActivityResult: ActivityResultLauncher<Intent>
    private lateinit var getTrendViewActivitiesResult: ActivityResultLauncher<Intent>
    private lateinit var getFrontPageActivityResult: ActivityResultLauncher<Intent>
    private lateinit var rowController: DataController
    private lateinit var secureFileHandler: SecureFileHandler
    private lateinit var onlineDataHandler: FirebaseConnectionHandler
    private val log = Logger.getLogger(MainActivity::class.java.name + "****************************************")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val securityHandler = SecurityHandler(applicationContext)
        secureFileHandler = SecureFileHandler(securityHandler)
        rowController = RowController()
        onlineDataHandler = FirebaseConnectionHandler()
        MoodTrackerMain(secureFileHandler,rowController,onlineDataHandler)

        //dataHandler = DataHandler(secureFileHandler, applicationContext)

        setupRecycleView()
        initButtons()
        setActivityListeners()

        //dataHandler = TestSuite.useLocalData(secureFileHandler, applicationContext)
        //TestSuite.setDefaultSettings()

        //rowController.update(dataHandler.read())

        val moodEntry = intent.getSerializableExtra("MoodEntry")
        if (moodEntry != null) rowController.update(moodEntry as MoodEntryModel)

        val todayMoodEntry: RowEntryModel? = rowController.find("Date", DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(LocalDate.now()))
        if (todayMoodEntry == null) startActivityFrontPage(null)
    }

    override fun setupRecycleView(): RecyclerViewAdaptor {
        val recyclerViewAdaptor = RecyclerViewAdaptor(
            { moodEntry -> setMoodValue(moodEntry) },
            { moodEntry -> startActivityActivities(moodEntry) },
            { moodEntry -> startActivityFeelings(moodEntry) },
            rowController)

        recyclerViewAdaptor.onLongPress = {
            log.info("Consumed onLongPress")
            startActivityFrontPage(it)
        }

        val callback: ItemTouchHelper.Callback = SwipeHelperCallback(recyclerViewAdaptor)
        val mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper.attachToRecyclerView(findViewById(R.id.recyclerViewMain))

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewMain)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = recyclerViewAdaptor

        return recyclerViewAdaptor
    }

    override fun startActivityActivities(moodEntry: MoodEntryModel) {
        val intent = Intent(this, ActivitiesActivity::class.java)
        val jsonArray = secureFileHandler.read("available.json")

        // Get activities that are stored in local json file
        if (jsonArray.isNotEmpty()) {
            val gson = GsonBuilder().create()
            val activities = gson.fromJson(jsonArray, ArrayList::class.java)
            if (activities.isNotEmpty()) {
                val data = activities.filterIsInstance<String>() as ArrayList<String>
                intent.putStringArrayListExtra("AvailableActivities", data)
            }
        }

        intent.putExtra("MoodEntry", moodEntry)
        getActivitiesActivityResult.launch(intent)
    }

    override fun startActivitySettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        getSettingsActivityResult.launch(intent)
    }

    override fun startActivityFrontPage(moodEntry: MoodEntryModel?) {
        val intent = Intent(this, DetailedViewActivity::class.java)
        if (moodEntry != null) intent.putExtra("MoodEntry", moodEntry)
        getFrontPageActivityResult.launch(intent)
    }

    override fun startActivityTrendView() {
        val intent = Intent(this, TrendViewActivity::class.java)
        getTrendViewActivitiesResult.launch(intent)
    }

    override fun startActivityFeelings(moodEntry: MoodEntryModel) {
        val intent = Intent(this, FeelingsActivity::class.java)
        val jsonArray = secureFileHandler.read("feelings.json")

        // Get activities that are stored in local json file
        if (jsonArray.isNotEmpty()) {
            val gson = GsonBuilder().create()
            val feelings = gson.fromJson(jsonArray, ArrayList::class.java)
            var data = ArrayList<String>()
            if (feelings.isNotEmpty()) data = feelings.filterIsInstance<String>() as ArrayList<String>
            intent.putStringArrayListExtra("AvailableFeelings", data)
        }

        intent.putExtra("MoodEntry", moodEntry)

        getFeelingsActivityResult.launch(intent)
    }

    private fun setMoodValue(moodEntry: MoodEntryModel, creation: Boolean = false) {
        val numberPickerMood: ChooseMoodCircle = findViewById(R.id.tvmpMoodValue)
        val numberPickerFatigue: ChooseFatigueCircle = findViewById(R.id.tvmpFatigueValue)
        val resetMood : TextView = findViewById(R.id.tvmpMoodTitle)
        val resetFatigue : TextView = findViewById(R.id.tvmpFatigueTitle)
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

        resetMood.setOnClickListener {
            numberPickerMood.reset()
        }

        resetFatigue.setOnClickListener {
            numberPickerFatigue.reset()
        }
        val mvHelper = MoodValueHelper()

        numberPickerMood.setSelected(moodEntry)
        numberPickerFatigue.setSelected(moodEntry)

        val clNumberPicker: ConstraintLayout = findViewById(R.id.clNumberPicker)
        clNumberPicker.visibility = View.VISIBLE

        val bNpConfirm: Button = findViewById(R.id.bNpConfirm)
        val bNpCancel: Button = findViewById(R.id.bNpCancel)

        bNpConfirm.text = if (creation) "Create"
                else "Update"

        bNpConfirm.setOnClickListener {
            val moodValue: Int = when (Settings.moodMode) {
                Settings.MoodModes.NUMBERS -> numberPickerMood.toInt()
                else -> mvHelper.getUnsanitisedNumber(numberPickerMood.toInt(), Settings.moodMax)
            }
            val fatigueValue: Int = when (Settings.fatigueMode) {
                Settings.FatigueModes.NUMBERS -> numberPickerFatigue.toInt()
                else -> mvHelper.getUnsanitisedNumber(numberPickerFatigue.toInt(), Settings.fatigueMax)
            }
            val dateValue: String = if (creation) dateFormat.format(LocalDateTime.now())
                    else moodEntry.date

            val timeValue: String = if (creation) timeFormat.format(LocalDateTime.now())
                    else moodEntry.time

            val newMood = MoodEntryModel(
                dateValue,
                timeValue,
                moodValue,
                fatigueValue,
                moodEntry.feelings,
                moodEntry.activities,
                moodEntry.key,
                LocalDateTime.now().toString()
            )
            clNumberPicker.visibility = View.INVISIBLE
            if (creation) rowController.add(newMood)
            else rowController.update(newMood)
        }

        bNpCancel.setOnClickListener {
            clNumberPicker.visibility = View.INVISIBLE
        }
    }

    private fun setActivityListeners() {
        getActivitiesActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val data = it.data?.getStringArrayListExtra("AvailableActivities")
                if (data != null) {
                    secureFileHandler.write(data as ArrayList<*>, "available.json")
                    rowController.update(it.data?.getSerializableExtra("MoodEntry") as MoodEntryModel)
                }
            }

        getTrendViewActivitiesResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        }

        getFrontPageActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it.data?.getSerializableExtra("MoodEntry")
            if (data != null) rowController.update(data as MoodEntryModel)
        }

        getFeelingsActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it.data?.getStringArrayListExtra("AvailableFeelings")
            if (data != null) {
                secureFileHandler.write(data as ArrayList<*>, "feelings.json")
                rowController.update(it.data?.getSerializableExtra("MoodEntry") as MoodEntryModel)
            }
        }

        getSettingsActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                //val data = it.data?.getParcelableExtra<Settings>("Settings")
                val moodEntries = it.data?.getParcelableArrayListExtra<Parcelable>("MoodEntries")
                var moodData = ArrayList<RowEntryModel>()
                if (moodEntries != null) {
                    val data = it.data?.getParcelableArrayListExtra<Parcelable>("MoodEntries")
                    if (data != null) moodData = data.filterIsInstance<RowEntryModel>() as ArrayList<RowEntryModel>
                }

                secureFileHandler.write(Settings)
//                recyclerViewAdaptor.updateListConfig()

                rowController.update(moodData)
            }
    }

    private fun initButtons() {
        val addNewButton: ImageButton = findViewById(R.id.addNewButton)

        addNewButton.setOnClickListener {
            setMoodValue(MoodEntryModel(), true)
        }

        val bViewTrend: ImageButton = findViewById(R.id.bViewTrend)
        bViewTrend.setOnClickListener {
            startActivityTrendView()
        }

        val ibSettings: ImageButton = findViewById(R.id.ibSettings)
        ibSettings.setOnClickListener {
            startActivitySettings()
        }

        val ibAddNewDebug: ImageButton = findViewById(R.id.ibAddNewDebug)
        ibAddNewDebug.setOnClickListener {
            val debugMood = MoodEntryFactory().createDebug(applicationContext)
            rowController.add(debugMood)
        }
    }
}
