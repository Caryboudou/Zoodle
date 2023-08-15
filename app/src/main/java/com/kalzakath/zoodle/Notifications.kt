package com.kalzakath.zoodle

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import java.util.Calendar
import java.util.concurrent.atomic.AtomicInteger

private const val CHANNEL_ID = "notification_channel_id"

object RemindersManager {
    private const val REMINDER_NOTIFICATION_REQUEST_CODE = 123
    @SuppressLint("ScheduleExactAlarm")
    fun startReminder(
        context: Context,
        reminderTime: String = "20:00",
        reminderId: Int = REMINDER_NOTIFICATION_REQUEST_CODE
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val (hours, min) = reminderTime.split(":").map { it.toInt() }

        val action = PendingIntent.getBroadcast(
            context,
            reminderId,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calendar: Calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, hours)
        calendar.set(Calendar.MINUTE, min)
        calendar.set(Calendar.SECOND, 0)


        // si heure de l alarme est deja passee aujourd hui -> debute demain
        if (Calendar.getInstance()
                .apply { add(Calendar.MINUTE, 1) }.timeInMillis - calendar.timeInMillis > 0
        ) {
            calendar.add(Calendar.DATE, 1)
        }

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            action)
    }

    fun stopReminder(
        context: Context,
        reminderId: Int = REMINDER_NOTIFICATION_REQUEST_CODE
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = PendingIntent.getBroadcast(
            context.applicationContext,
            reminderId,
            Intent(context.applicationContext, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.cancel(intent)
    }
}

class AlarmReceiver : BroadcastReceiver() {

    /**
     * sends notification when receives alarm
     * and then reschedule the reminder again
     * */

    private var notificationId = AtomicInteger()

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager

        notificationManager.sendReminderNotification(
            applicationContext = context,
            channelId = CHANNEL_ID
        )
    }

    fun send(context: Context) {
        val notificationManager = getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager

        notificationManager.sendReminderNotification(
            applicationContext = context,
            channelId = CHANNEL_ID
        )
    }

    private fun NotificationManager.sendReminderNotification(
        applicationContext: Context,
        channelId: String,
    ) {
        val contentIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            1,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_edit)
            .setContentTitle(applicationContext.getString(R.string.app_notification))
            .setContentText(applicationContext.getString(R.string.notification_title))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        notify(generateNextNotificationId(), builder.build())
    }

    private fun generateNextNotificationId() = notificationId.getAndIncrement()
}

class BootReceiver : BroadcastReceiver() {
    /*
    * restart reminders alarms when user's device reboots
    * */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            RemindersManager.startReminder(context)
        }
    }
}


fun createNotif(context: Context, time: String = "20:00") {
    createNotificationsChannels(context)
    RemindersManager.startReminder(context, time)
    /*val a = AlarmReceiver ()
    a.send(context)*/
}

fun deleteNotif(context: Context) {
    RemindersManager.stopReminder(context)
}

fun createNotificationsChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = context.resources.getString(R.string.app_name)
        val descriptionText = context.resources.getString(R.string.app_notification)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
