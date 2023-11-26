package com.niaouh.moodtracker.debug

import android.content.Context
import com.niaouh.moodtracker.SecureFileHandler
import com.niaouh.moodtracker.Settings
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

object TestSuite {
    fun useLocalData(secureFileHandler: SecureFileHandler, context: Context): DebugDataHandler {
        return DebugDataHandler(secureFileHandler, context)
    }

    fun useOnlineData (secureFileHandler: SecureFileHandler, context: Context): DebugDataHandler {
        return DebugDataHandler(secureFileHandler, context)

    }

    fun setDefaultSettings() {
        for (prop in Settings::class.memberProperties) {
            val defaultVal =
                Settings.Default::class.java.declaredFields.find { it.name == prop.name }
            if (defaultVal != null) {
                if (prop is KMutableProperty<*>) {
                    prop.setter.call(Settings, defaultVal.get(Settings.Default))
                }
            }
        }
    }
}