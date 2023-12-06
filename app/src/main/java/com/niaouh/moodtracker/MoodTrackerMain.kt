package com.niaouh.moodtracker

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.niaouh.moodtracker.interfaces.*
import com.niaouh.moodtracker.model.MoodEntryModel
import java.lang.reflect.Modifier
import java.util.logging.Logger

class MoodTrackerMain(secureFileHandler: SecureFileHandler,
                      rowController: DataController
): DataControllerEventListener,
    MoodTracker {
    private val _secureFileHandler = secureFileHandler
    private val _rowController = rowController
    private val log = Logger.getLogger(MainActivity::class.java.name)

    init {
        _rowController.registerForUpdates(this)

        loadLocalData()
        loadSettingData()
    }

    override fun convertToArrayList(jsonString: String): ArrayList<RowEntryModel> {
        val myArrayList = arrayListOf<RowEntryModel>()

        if (jsonString.isNotEmpty()) {
            try {
                val gson = GsonBuilder().create()
                val type = object : TypeToken<Array<MoodEntryModel>>() {}.type
                val moodEntries = gson.fromJson<Array<MoodEntryModel>>(jsonString, type)

                for (x in moodEntries.indices) {
                    myArrayList.add(moodEntries[x])
                }
            } catch (e: Exception) {
                log.info("Unable to parse JSON - invalid format")
            }
        }

        return myArrayList
    }

    override fun loadLocalData() {
        val data = _secureFileHandler.read()
        if (data.isNotEmpty()) _rowController.update(convertToArrayList(data), false)
    }

    override fun loadSettingData() {
        readSettingsDataFromJson(_secureFileHandler.read("settings.json"))
    }

    override fun onUpdateFromDataController(event: RowControllerEvent) {
        // this writes a local dump of the whole list
        _secureFileHandler.write(_rowController.mainRowEntryList)
    }

    override fun readSettingsDataFromJson(jsonSettings: String?) {
        val gson = GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).create()
        val type = object : TypeToken<Settings>() {}.type
        val data = gson.fromJson<Settings>(jsonSettings, type)
        if (data != null) {
            Settings.moodMode = data.moodMode
            Settings.moodMax = data.moodMax
            Settings.fatigueMode = data.fatigueMode
            Settings.fatigueMax = data.fatigueMax
        }
    }
}