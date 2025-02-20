package com.niaouh.moodtracker

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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.gson.GsonBuilder
import com.niaouh.moodtracker.debug.TestSuite
import com.niaouh.moodtracker.interfaces.DataController
import com.niaouh.moodtracker.interfaces.MainActivityInterface
import com.niaouh.moodtracker.interfaces.RowEntryModel
import com.niaouh.moodtracker.layout.ChooseFatigueCircle
import com.niaouh.moodtracker.layout.ChooseMoodCircle
import com.niaouh.moodtracker.layout.ChooseFatigueCircle10
import com.niaouh.moodtracker.layout.ChooseMoodCircle10
import com.niaouh.moodtracker.model.MoodEntryModel
import com.niaouh.moodtracker.model.MoodEntryModelToJson
import com.niaouh.moodtracker.model.createNewEntry
import com.niaouh.moodtracker.model.updateDateOnly
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Logger
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(), MainActivityInterface {

    private lateinit var getSettingsActivityResult: ActivityResultLauncher<Intent>
    private lateinit var getNoteActivityResult: ActivityResultLauncher<Intent>
    private lateinit var getTrendViewActivitiesResult: ActivityResultLauncher<Intent>
    private lateinit var getFrontPageActivityResult: ActivityResultLauncher<Intent>
    private lateinit var getTrackerActivityResult: ActivityResultLauncher<Intent>
    private lateinit var rowController: DataController
    private lateinit var recyclerViewAdaptor: RecyclerViewAdaptor
    private lateinit var dataHandler : DataHandler
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager : LinearLayoutManager
    private lateinit var secureFileHandler: SecureFileHandler
    private lateinit var clNumberPicker: ConstraintLayout
    private var clNumberInvisible: Boolean by Delegates.observable(true) { _, _, bool ->
       clNumberPicker.visibility = if (bool) View.INVISIBLE else View.VISIBLE }
    private val log = Logger.getLogger(MainActivity::class.java.name + "MainActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val securityHandler = SecurityHandler(applicationContext)
        secureFileHandler = SecureFileHandler(securityHandler)
        rowController = RowController()
        MoodTrackerMain(secureFileHandler,rowController)

        dataHandler = DataHandler(secureFileHandler, applicationContext)

        setContentView(R.layout.activity_main)
        clNumberPicker = findViewById(R.id.clNumberPicker)
        clNumberInvisible = true

        log.info("main 4")
        setupRecycleView()
        log.info("main 3")
        initButtons()
        log.info("main 2")
        setActivityListeners()

        //dataHandler = TestSuite.useLocalData(secureFileHandler, applicationContext)
        //TestSuite.setDefaultSettings()
        //Settings.setDefaultSettings()

        rowController.update(dataHandler.read())

        //cancelNotification(applicationContext)

        val moodEntry = intent.getSerializableExtra("MoodEntry")
        if (moodEntry != null)
            rowController.update(moodEntry as MoodEntryModel)

        val forgottenEntryYear = intent.getSerializableExtra("Forgotten_entry_year")
        val forgottenEntryMonth = intent.getSerializableExtra("Forgotten_entry_month")
        val forgottenEntryDay = intent.getSerializableExtra("Forgotten_entry_day")
        if (forgottenEntryYear != null && forgottenEntryMonth != null && forgottenEntryDay != null) {
            try {
                log.info("launch startActivityFrontPage for y $forgottenEntryYear m $forgottenEntryMonth d $forgottenEntryDay")
                val newMoodEntry = createNewEntry(
                    year = (forgottenEntryYear as String).toInt(),
                    month = (forgottenEntryMonth as String).toInt(),
                    day = (forgottenEntryDay as String).toInt()
                )
                log.info("newMoodEntry $newMoodEntry")
                startActivityFrontPage(newMoodEntry)
            } catch (_ : Exception) {
                log.info("failed launch startActivityFrontPage for y $forgottenEntryYear m $forgottenEntryMonth d $forgottenEntryDay")
            }
        }
        else {
            val todayMoodEntry: RowEntryModel? = rowController.findDate(LocalDate.now())
            if (todayMoodEntry == null) startActivityFrontPage(null)
            else {
                deleteNotifForgetTomorrow(applicationContext)
                log.info("Delete tomorrow reminder")
            }
        }
    }

    override fun setupRecycleView() {
        log.info("main 3")
        recyclerViewAdaptor = RecyclerViewAdaptor(
            { moodEntry -> setMoodValue(moodEntry) },
            { moodEntry -> startNoteActivity(moodEntry) },
            rowController)

        recyclerViewAdaptor.onLongPress = {
            log.info("Consumed onLongPress")
            if (clNumberInvisible) startActivityFrontPage(it)
        }

        val callback: ItemTouchHelper.Callback = SwipeHelperCallback(this, findViewById(R.id.constraintMain), recyclerViewAdaptor)
        val mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper.attachToRecyclerView(findViewById(R.id.recyclerViewMain))

        recyclerView = findViewById(R.id.recyclerViewMain)
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = recyclerViewAdaptor
    }

    override fun startActivitySettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        intent.getParcelableArrayListExtra<Parcelable>("MoodEntries")
        if (clNumberInvisible) getSettingsActivityResult.launch(intent)
    }

    override fun startActivityFrontPage(moodEntry: MoodEntryModel?) {
        val intent = Intent(this, DetailedViewActivity::class.java)
        if (moodEntry != null) intent.putExtra("MoodEntry", moodEntry.MoodEntryModelToJson())
        if (clNumberInvisible) getFrontPageActivityResult.launch(intent)
    }

    override fun startActivityTrendView() {
        val intent = Intent(this, TrendViewActivity::class.java)
        if (clNumberInvisible) getTrendViewActivitiesResult.launch(intent)
    }

    fun startActivityTracker() {
        val intent = Intent(this, TrackerActivity::class.java)
        getTrackerActivityResult.launch(intent)
    }

    private fun startNoteActivity(moodEntry: MoodEntryModel) {
        val intent = Intent(this, NoteActivity::class.java)
        intent.putExtra("MoodEntry", moodEntry.MoodEntryModelToJson())
        if (clNumberInvisible) getNoteActivityResult.launch(intent)
    }

    private fun setMoodValue(moodEntry: MoodEntryModel, creation: Boolean = false) {
        val numberPickerMood: ChooseMoodCircle10 = findViewById(R.id.tvmpMoodValue)
        val numberPickerFatigue: ChooseFatigueCircle10 = findViewById(R.id.tvmpFatigueValue)
        val resetMood : TextView = findViewById(R.id.tvmpMoodTitle)
        val resetFatigue : TextView = findViewById(R.id.tvmpFatigueTitle)

        resetMood.setOnClickListener {
            numberPickerMood.reset()
        }

        resetFatigue.setOnClickListener {
            numberPickerFatigue.reset()
        }
        val mvHelper = MoodValueHelper()

        numberPickerMood.setSelected(moodEntry)
        numberPickerFatigue.setSelected(moodEntry)

        if (!clNumberInvisible) return

        clNumberInvisible = false

        val bNpConfirm: Button = findViewById(R.id.bNpConfirm)
        val bNpCancel: Button = findViewById(R.id.bNpCancel)

        bNpConfirm.text = if (creation) "Créer"
                else "Mettre à jour"

        bNpConfirm.setOnClickListener {
            val moodValue: Int = when (Settings.moodMode) {
                Settings.MoodModes.NUMBERS -> numberPickerMood.toInt()
                else -> mvHelper.getUnsanitisedNumber(numberPickerMood.toInt(), Settings.moodMax)
            }
            val fatigueValue: Int = when (Settings.fatigueMode) {
                Settings.FatigueModes.NUMBERS -> numberPickerFatigue.toInt()
                else -> mvHelper.getUnsanitisedNumber(numberPickerFatigue.toInt(), Settings.fatigueMax)
            }
            val dateValue: LocalDateTime = if (creation) LocalDateTime.now()
                    else moodEntry.date

            moodEntry.date = dateValue
            moodEntry.mood = moodValue
            moodEntry.fatigue = fatigueValue
            moodEntry.lastUpdated = LocalDateTime.now().toString()

            clNumberInvisible = true
            rowController.update(moodEntry)
        }

        bNpCancel.setOnClickListener {
            clNumberInvisible = true
        }
    }

    private fun setActivityListeners() {
        getTrendViewActivitiesResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        }

        getTrackerActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        }

        getFrontPageActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it.data?.getSerializableExtra("MoodEntry")
            log.info("getFrontPageResult")
            if (data != null) rowController.update((data as MoodEntryModelToJson).MoodEntryModel())
            val todayMoodEntry: RowEntryModel? = rowController.findDate(LocalDate.now())
            if (todayMoodEntry != null) {
                deleteNotifForgetTomorrow(applicationContext, Settings.notificationTime)
                log.info("Delete tomorrow reminder")
            }
        }

        getNoteActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it.data?.getSerializableExtra("MoodEntry")
            if (data != null) rowController.update((data as MoodEntryModelToJson).MoodEntryModel())
        }

        getSettingsActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                //val data = it.data?.getParcelableExtra<Settings>("Settings")
                val moodEntries = it.data?.getSerializableExtra("MoodEntries")
                val moodData = ArrayList<RowEntryModel>()
                if (moodEntries != null) {
                    val moodTemp = moodEntries as ArrayList<MoodEntryModelToJson>
                    for (m in moodEntries) {
                        moodData.add(m.MoodEntryModel())
                    }
                }

                secureFileHandler.write(Settings)
//                recyclerViewAdaptor.updateListConfig()

                rowController.update(moodData)
            }
    }

    private fun initButtons() {
        val dtPickerDate = DatePicker()
        dtPickerDate.onUpdateListener = {
            val scrollPosition = recyclerViewAdaptor.findFirst(it.time)
            layoutManager.scrollToPositionWithOffset(scrollPosition, 0)
        }

        val ibFind: ImageButton = findViewById(R.id.ibFind)
        ibFind.setOnClickListener {
            dtPickerDate.show(this, LocalDateTime.now())
        }

        val addNewButton: ImageButton = findViewById(R.id.addNewButton)
        addNewButton.setOnClickListener {
            startActivityFrontPage(null)
        }

        val bViewTrend: ImageButton = findViewById(R.id.bViewTrend)
        bViewTrend.setOnClickListener {
            if (clNumberInvisible) startActivityTrendView()
        }

        val ibModeNote: ImageButton = findViewById(R.id.ibModeNote)
        if (Settings.modeNote) {
            ibModeNote.setImageResource(R.drawable.ic_mode_note_no)
        } else {
            ibModeNote.setImageResource(R.drawable.ic_mode_note)
        }
        ibModeNote.setOnClickListener {
            Settings.modeNote = !Settings.modeNote
            if (Settings.modeNote) {
                ibModeNote.setImageResource(R.drawable.ic_mode_note_no)
            } else {
                ibModeNote.setImageResource(R.drawable.ic_mode_note)
            }

            val scrollPosition = layoutManager.findFirstVisibleItemPosition()
            setupRecycleView()
            layoutManager = recyclerView.layoutManager as LinearLayoutManager
            layoutManager.scrollToPosition(scrollPosition)
        }

        val ibModeTrack: ImageButton = findViewById(R.id.ibModeTrack)
        if (Settings.trackerMode) {
            ibModeTrack.setImageResource(R.drawable.ic_mode_track)
        } else {
            ibModeTrack.setImageResource(R.drawable.ic_mode_track_no)
        }
        ibModeTrack.setOnClickListener {
            Settings.trackerMode = !Settings.trackerMode
            if (Settings.trackerMode) {
                ibModeTrack.setImageResource(R.drawable.ic_mode_track)
            } else {
                ibModeTrack.setImageResource(R.drawable.ic_mode_track_no)
            }

            val scrollPosition = layoutManager.findFirstVisibleItemPosition()
            setupRecycleView()
            layoutManager = recyclerView.layoutManager as LinearLayoutManager
            layoutManager.scrollToPosition(scrollPosition)
        }

        val ibSettings: ImageButton = findViewById(R.id.ibSettings)
        ibSettings.setOnClickListener {
            if (clNumberInvisible) startActivitySettings()
        }

        val ibAddNewDebug: ImageButton = findViewById(R.id.ibAddNewDebug)
        ibAddNewDebug.setOnClickListener {
            //val debugMood = MoodEntryFactory().createDebug(applicationContext)
            //rowController.add(debugMood)
            val newMoodEntry = createNewEntry(year = 2025 as Int,
                month = 1 as Int,
                day = 1 as Int)
            startActivityFrontPage(newMoodEntry)
        }
    }
}
