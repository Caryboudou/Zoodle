package com.kalzakath.zoodle.alarm_rc

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.kalzakath.zoodle.R
import com.kalzakath.zoodle.Settings
import com.kalzakath.zoodle.TimePicker
import com.kalzakath.zoodle.createNotif
import com.kalzakath.zoodle.createNotifSeveral
import com.kalzakath.zoodle.deleteAllNotifSeveral
import com.kalzakath.zoodle.deleteNotif
import com.kalzakath.zoodle.deleteNotifSeveral
import com.kalzakath.zoodle.model.*
import com.kalzakath.zoodle.utils.ResUtil.getTimeStringFR
import java.text.SimpleDateFormat
import java.util.*

class AlarmAdapter(data: ArrayList<String>, layer: LinearLayout): Adapter<AlarmAdapter.AlarmViewHolder>() {
    private var alarmList: MutableList<String> = arrayListOf()
    private var rcLayer: LinearLayout

    init {
        rcLayer = layer
        alarmList = data
        if (itemCount == 0)
            rcLayer.visibility = View.GONE
        else rcLayer.visibility = View.VISIBLE
        sortList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val viewHolder: View = LayoutInflater.from(parent.context).inflate(R.layout.settings_activity, parent, false)
        return AlarmViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return alarmList.size
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        initButtons(holder, position)
    }

    private fun initButtons(mHolder: AlarmViewHolder, position: Int) {
        val time = alarmList[position]
        val context = mHolder.itemView.context

        mHolder.updateTime(time)
        /*deleteAlarm(position, context)
        addAlarm(time, context)*/

        val dtPickerTime = TimePicker()
        dtPickerTime.onUpdateListener = {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            val dtTime = timeFormat.format(it.time)
            val timeReminder = context.getString(R.string.settings_reminder_time) + " " + getTimeStringFR(dtTime)
            mHolder.text.text = timeReminder
            deleteAlarm(position, context)
            addAlarm(dtTime, context)
        }

        mHolder.ibSet.setOnClickListener {
            dtPickerTime.show(mHolder.itemView.context, time)
        }
        mHolder.ibDelete.setOnClickListener {
            deleteAlarm(position, context)
        }
    }

    fun deleteAlarm(position: Int, context: Context) {
        deleteNotifSeveral(context, alarmList[position])
        alarmList.removeAt(position)
        notifyItemRemoved(position)
        sortList()
        if (itemCount == 0) {
            rcLayer.visibility = View.GONE
            deleteAllNotifSeveral(context)
        }
    }

    fun addAlarm(time: String, context: Context) {
        if (alarmList.contains(time)) return
        alarmList.add(time)
        notifyItemInserted(alarmList.size.minus(1))
        sortList()
        rcLayer.visibility = View.VISIBLE
        createNotifSeveral(context, time)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun sortList() {
        alarmList.sort()
        notifyDataSetChanged()
        Settings.notificationList.sort()
    }

    class AlarmViewHolder(itemView: View) : ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.tvReminderTime)
        val ibSet: ImageButton = itemView.findViewById(R.id.ibSetReminder)
        val ibDelete: ImageButton = itemView.findViewById(R.id.ibDeleteReminder)

        fun updateTime(newTime: String) {
            val context = itemView.context
            val time = getTimeStringFR(newTime)
            val timeReminder = context.getString(R.string.settings_reminder_time) + " " + time
            text.text = timeReminder
        }
    }
}
