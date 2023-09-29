package com.kalzakath.zoodle

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification.VISIBILITY_PUBLIC
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.coroutineScope
import java.lang.Exception
import java.net.SocketException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Logger

private const val CHANNEL_ID = "notification_channel_id"
private const val NAME_NOTIF = "notification_tag_"
private const val DEFAULTTIME = "20:00"

object RemindersManager {
    @SuppressLint("ScheduleExactAlarm")
    fun startReminder(
        context: Context,
        reminderTime: String
    ) {
        val (hours, min) = reminderTime.split(":").map { it.toInt() }

        val calendarDelay: Calendar = Calendar.getInstance()
        val calendar: Calendar = Calendar.getInstance()

        calendarDelay.set(Calendar.HOUR_OF_DAY, hours)
        calendarDelay.set(Calendar.MINUTE, min)
        calendarDelay.set(Calendar.SECOND, 0)


        // si heure de l alarme est deja passee aujourd hui -> debute demain
        if (calendarDelay.before(calendar)) {
            calendarDelay.add(Calendar.DATE, 1)
        }

        val alarmWorkRequest =
            OneTimeWorkRequestBuilder<AlarmWorker>()
                .addTag(reminderTime)
                .setInitialDelay(calendarDelay.timeInMillis - calendar.timeInMillis, TimeUnit.MILLISECONDS)
                .build()
        WorkManager.getInstance(context).enqueue(alarmWorkRequest)
    }

    fun stopReminder(
        context: Context
    ) {
        WorkManager.getInstance(context).cancelAllWork()
    }
}

class AlarmWorker (appcontext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appcontext, workerParams) {
    private var notificationId = AtomicInteger()

    companion object {
        private const val REMINDER_WORK_NAME = "reminder"
        private const val PARAM_NAME = "name"
        private val log = Logger.getLogger(MainActivity::class.java.name + "****************************************")


        fun runAt(
            reminderTime: String,
            context: Context
        ) {
            val (hours, min) = reminderTime.split(":").map { it.toInt() }

            val calendarDelay: Calendar = Calendar.getInstance()
            val calendar: Calendar = Calendar.getInstance()

            calendarDelay.set(Calendar.HOUR_OF_DAY, hours)
            calendarDelay.set(Calendar.MINUTE, min)
            calendarDelay.set(Calendar.SECOND, 0)


            // si heure de l alarme est deja passee aujourd hui -> debute demain
            if (calendarDelay.before(calendar)) {
                calendarDelay.add(Calendar.DATE, 1)
            }

            val data = workDataOf(PARAM_NAME to reminderTime)

            val alarmWorkRequest =
                OneTimeWorkRequestBuilder<AlarmWorker>()
                    .setInputData(data)
                    .setInitialDelay(
                        calendarDelay.timeInMillis - calendar.timeInMillis,
                        TimeUnit.MILLISECONDS
                    )
                    .build()
            WorkManager.getInstance(context).enqueueUniqueWork(REMINDER_WORK_NAME, ExistingWorkPolicy.REPLACE,alarmWorkRequest)
        }
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME)
        }
    }

    override suspend fun doWork(): Result = coroutineScope {
        val context = applicationContext
        val reminderTime = inputData.getString(PARAM_NAME) as String
        var isScheduleNext = true

        try {
            val notificationManager = getSystemService(
                context,
                NotificationManager::class.java
            ) as NotificationManager

            notificationManager.sendReminderNotification(
                applicationContext = context,
                channelId = CHANNEL_ID
            )
            Result.success()
        }
        catch (e: Exception) {
        // only retry 3 times
            if (runAttemptCount > 3) {
                return@coroutineScope Result.success()
            }
            // retry if network failure, else considered failed
            when(e.cause) {
                is SocketException -> {
                    isScheduleNext = false
                    Result.retry()
                }
                else -> Result.failure()
                }
        }
        finally {
            // only schedule next day if not retry, else it will overwrite the retry attempt
            // - because we use uniqueName with ExistingWorkPolicy.REPLACE
            if (isScheduleNext) {runAt(reminderTime, context)
            log.info("set nex alarm at $reminderTime")}// schedule for next day
        }
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
            .setSmallIcon(R.drawable.icon_foreground)
            .setContentTitle(applicationContext.getString(R.string.app_notification))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(applicationContext.getColor(R.color.colorIconNotif))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notify(generateNextNotificationId(), builder.build())
    }

    private fun generateNextNotificationId() = notificationId.getAndIncrement()
}

class AlarmWorkerSeveral (appcontext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appcontext, workerParams) {
    private var notificationId = AtomicInteger()

    companion object {
        private const val REMINDER_WORK_NAME = "reminder"
        private const val PARAM_NAME = "name"
        private val log = Logger.getLogger(MainActivity::class.java.name + "****************************************")


        fun runAt(
            reminderTime: String,
            context: Context
        ) {
            val (hours, min) = reminderTime.split(":").map { it.toInt() }

            val calendarDelay: Calendar = Calendar.getInstance()
            val calendar: Calendar = Calendar.getInstance()

            calendarDelay.set(Calendar.HOUR_OF_DAY, hours)
            calendarDelay.set(Calendar.MINUTE, min)
            calendarDelay.set(Calendar.SECOND, 0)


            // si heure de l alarme est deja passee aujourd hui -> debute demain
            if (calendarDelay.before(calendar)) {
                calendarDelay.add(Calendar.DATE, 1)
            }

            val workName = NAME_NOTIF+reminderTime

            val data = workDataOf(PARAM_NAME to reminderTime)

            val alarmWorkRequest =
                OneTimeWorkRequestBuilder<AlarmWorker>()
                    .setInputData(data)
                    .setInitialDelay(
                        calendarDelay.timeInMillis - calendar.timeInMillis,
                        TimeUnit.MILLISECONDS
                    )
                    .build()
            WorkManager.getInstance(context).enqueueUniqueWork(workName, ExistingWorkPolicy.KEEP,alarmWorkRequest)
        }
        fun cancel(context: Context, reminderTime: String) {
            val workName = NAME_NOTIF+reminderTime
            WorkManager.getInstance(context).cancelUniqueWork(workName)
        }
    }

    override suspend fun doWork(): Result = coroutineScope {
        val context = applicationContext
        val reminderTime = inputData.getString(PARAM_NAME) as String
        var isScheduleNext = true

        try {
            val notificationManager = getSystemService(
                context,
                NotificationManager::class.java
            ) as NotificationManager

            notificationManager.sendReminderNotification(
                applicationContext = context,
                channelId = CHANNEL_ID
            )
            Result.success()
        }
        catch (e: Exception) {
        // only retry 3 times
            if (runAttemptCount > 3) {
                return@coroutineScope Result.success()
            }
            // retry if network failure, else considered failed
            when(e.cause) {
                is SocketException -> {
                    isScheduleNext = false
                    Result.retry()
                }
                else -> Result.failure()
                }
        }
        finally {
            // only schedule next day if not retry, else it will overwrite the retry attempt
            // - because we use uniqueName with ExistingWorkPolicy.REPLACE
            if (isScheduleNext) {runAt(reminderTime, context)
            log.info("set nex alarm at $reminderTime")}// schedule for next day
        }
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
            .setSmallIcon(R.drawable.icon_foreground)
            .setContentTitle(applicationContext.getString(R.string.app_notification))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(applicationContext.getColor(R.color.colorIconNotif))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notify(generateNextNotificationId(), builder.build())
    }

    private fun generateNextNotificationId() = notificationId.getAndIncrement()
}

fun createNotif(context: Context, time: String = Settings.notificationTime) {
    createNotificationsChannels(context)
    //RemindersManager.startReminder(context, time)
    AlarmWorker.runAt(time, context)
}

fun deleteNotif(context: Context) {
    //RemindersManager.stopReminder(context)
    AlarmWorker.cancel(context)
}

fun createNotifSeveral(context: Context, time: String = Settings.notificationTime) {
    createNotificationsChannels(context)
    AlarmWorkerSeveral.runAt(time, context)
}

fun deleteNotifSeveral(context: Context, time: String) {
    val tag_notif = NAME_NOTIF+time
    AlarmWorkerSeveral.cancel(context, tag_notif)
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
