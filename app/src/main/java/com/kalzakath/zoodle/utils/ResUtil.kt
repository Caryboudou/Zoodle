package com.kalzakath.zoodle.utils

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build

object ResUtil {

    fun getString(resources: Resources, resourceId: Int): String {
        return resources.getString(resourceId)
    }

    fun getFormattedString(resources: Resources, resourceId: Int, vararg textsToFormat: String?): String {
        return String.format(
            resources.getString(resourceId),
            *textsToFormat
        )
    }

    fun getDateStringFR(date: String) : String {
        val slipped = date.split('-')
        return slipped[2]+"/"+slipped[1]+"/"+slipped[0]
    }
    fun getDateStringEN(date: String) : String {
        val slipped = date.split('/')
        return slipped[2]+"-"+slipped[1]+"-"+slipped[0]
    }

    fun getTimeStringFR(time: String) : String {
        val slipped = time.split(":")
        return slipped[0]+"h"+slipped[1]
    }
    fun getTimeStringEN(time: String) : String {
        val slipped = time.split("h")
        return slipped[0]+":"+slipped[1]
    }

    fun getColor(context: Context?, resourceId: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context!!.getColor(resourceId)
        } else {
            context!!.resources.getColor(resourceId)
        }
    }

    fun getColorAsColorStateList(resources: Resources, resourceId: Int): ColorStateList {
        return ColorStateList.valueOf(resources.getColor(resourceId))
    }

    fun getBoolean(resources: Resources, resourceId: Int): Boolean {
        return resources.getBoolean(resourceId)
    }

    fun getDrawable(context: Context?, resourceId: Int): Drawable? {
        return context!!.resources.getDrawable(resourceId, context.theme)
    }

    fun getDimenDp(resources: Resources, resourceId: Int): Int {
        return (resources.getDimension(resourceId) / resources.displayMetrics.density).toInt()
    }
}
