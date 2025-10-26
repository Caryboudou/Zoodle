package com.niaouh.moodtracker

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.niaouh.moodtracker.interfaces.RowEntryModel
import com.niaouh.moodtracker.model.MoodEntryModel
import com.niaouh.moodtracker.model.compare
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.lang.reflect.Modifier

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MoodTrackerMainTest {

    private lateinit var main: MoodTrackerMain
    private lateinit var secureFH: SecureFileHandler
    private lateinit var testJsonArrayString: String
    private lateinit var testJsonArray: ArrayList<MoodEntryModel>
    private lateinit var rowController: RowController

    @BeforeAll
    fun setup() {
        val gson = Gson()
        testJsonArray = arrayListOf(MoodEntryModel(), MoodEntryModel())
        testJsonArrayString = gson.toJson(testJsonArray)

        secureFH = mockk {
            every { read() } returns testJsonArrayString
            every { read("settings.json") } returns ""
        }
        rowController = RowController()
        main = MoodTrackerMain(secureFH,rowController)
    }

    @AfterEach
    fun setdown() {
        rowController.mainRowEntryList.clear()
    }

    @Test
    fun convertToArrayList() {
        val gson = Gson()
        val testArray = arrayListOf(MoodEntryModel(), MoodEntryModel())
        val jsonString = gson.toJson(testArray)

        val jsonArray = main.convertToArrayList(jsonString)
        jsonArray.indices.forEach { assert((jsonArray[it] as MoodEntryModel).compare(testArray[it])) }
    }

    @Test
    fun loadLocalData() {
        main.loadLocalData()
        rowController.mainRowEntryList.indices.forEach { (rowController.mainRowEntryList[it] as MoodEntryModel).compare(testJsonArray[it]) }
    }

    @Test
    fun loadSettingData() {
        val gsonBuilder = GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).create()

        val testMoodMode = 100
        val testMoodMax = 1000
        Settings.moodMode = testMoodMode
        Settings.moodMax = testMoodMax
        val testJsonSettingsString = gsonBuilder.toJson(Settings)
        every { secureFH.read("settings.json") } returns testJsonSettingsString
        Settings.setDefaultSettings()

        assert(Settings.moodMax == Settings.Default.moodMax)
        assert(Settings.moodMode == Settings.Default.moodMode)

        main.loadSettingData()

        assert(Settings.moodMax == testMoodMax)
        assert(Settings.moodMode == testMoodMode)
    }

    @Test
    fun onUpdateFromDataController() {
        val testArray: ArrayList<RowEntryModel> = arrayListOf(MoodEntryModel(), MoodEntryModel())
        val testEventRemove = RowControllerEvent(testArray,RowControllerEvent.REMOVE)
        every { secureFH.write(arrayListOf<RowEntryModel>()) } returns true

        main.onUpdateFromDataController(testEventRemove)

        verify { secureFH.write(arrayListOf<RowEntryModel>()) }

        val testEventOther = RowControllerEvent(testArray,RowControllerEvent.ADDITION)
        main.onUpdateFromDataController(testEventOther)
    }

    @Test
    fun saveLocalDataToOnline() {
    }

    @Test
    fun onUpdateFromDatabase() {
    }

    @Test
    fun onLoginUpdateFromDatabase() {
    }

    @Test
    fun readSettingsDataFromJson() {
    }
}