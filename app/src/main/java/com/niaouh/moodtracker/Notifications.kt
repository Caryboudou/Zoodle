package com.niaouh.moodtracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.niaouh.moodtracker.utils.ResUtil
import java.lang.Exception
import java.net.SocketException
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

private const val CHANNEL_ID = "notification_channel_id"

class ForgottenEntranceAlarmsWorker (appcontext: Context, workerParams: WorkerParameters):
    Worker(appcontext, workerParams) {

    companion object {
        private const val REMINDER_WORK_NAME = "notification_forget_entrance_tag"
        private const val PARAM_NAME = "name"
        private val log = Logger.getLogger(MainActivity::class.java.name + "Notification.ForgottenEntranceAlarmsWorker")


        fun runAt(
            reminderTime: String,
            context: Context,
            isCancelled: Boolean = false
        ) {
            val (hours, min) = reminderTime.split(":").map { it.toInt() }

            val calendarDelay: Calendar = Calendar.getInstance()
            val calendar: Calendar = Calendar.getInstance()

            calendarDelay.set(Calendar.HOUR_OF_DAY, hours)
            calendarDelay.set(Calendar.MINUTE, min)
            calendarDelay.set(Calendar.SECOND, 0)
            if (!isCancelled) calendarDelay.add(Calendar.DATE, 1) //on notifie le lendemain de l'oubi du jour
            else calendarDelay.add(Calendar.DATE, 2) //on notifie pour après demain si oubli lorsque qu on vient de remplir demain

            val data = workDataOf(PARAM_NAME to reminderTime)
            log.info("delay ${calendarDelay.timeInMillis - calendar.timeInMillis}")
            val alarmWorkRequest =
                OneTimeWorkRequestBuilder<ForgottenEntranceAlarmsWorker>()
                    .setInputData(data)
                    .setInitialDelay(
                        calendarDelay.timeInMillis - calendar.timeInMillis,
                        //1000,
                        TimeUnit.MILLISECONDS
                    )
                    .build()
            WorkManager.getInstance(context).enqueueUniqueWork(REMINDER_WORK_NAME, ExistingWorkPolicy.REPLACE,alarmWorkRequest)
        }
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME)
        }
        fun cancelTomorrow(context: Context, reminderTime: String) {
            runAt(reminderTime, context, true)
        }
    }

    override fun doWork(): Result {
        val context = applicationContext
        val reminderTime = inputData.getString(PARAM_NAME) as String
        var isScheduleNext = true

        try {
            cancelNotification(applicationContext)
            val notificationManager = getSystemService(
                context,
                NotificationManager::class.java
            ) as NotificationManager

            notificationManager.sendReminderNotification(
                applicationContext = context,
                channelId = CHANNEL_ID
            )
            return Result.success()
        }
        catch (e: Exception) {
        // only retry 3 times
            if (runAttemptCount > 3) {
                return Result.success()
            }
            // retry if network failure, else considered failed
            return when(e.cause) {
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
            log.info("set next forget alarm at $reminderTime")
            }// schedule for next day
        }
    }

    private fun NotificationManager.sendReminderNotification(
        applicationContext: Context,
        channelId: String
    ) {
        val calendar: Calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -1)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val monthString = if (calendar.get(Calendar.MONTH) < 9) "0${calendar.get(Calendar.MONTH)+1}"
            else "${calendar.get(Calendar.MONTH)+1}"
        val dayString = if (calendar.get(Calendar.DAY_OF_MONTH) < 10) "0${calendar.get(Calendar.DAY_OF_MONTH)}"
            else "${calendar.get(Calendar.DAY_OF_MONTH)}"
        val month = calendar.get(Calendar.MONTH)+1
        val year = calendar.get(Calendar.YEAR)
        val title = "Moral du $dayString/$monthString/$year non rempli"
        val nID = 1000*year+100*month+day //+ kotlin.random.Random.nextInt()
        log.info("notif rappel forget id $nID")

        val contentIntent = Intent(applicationContext, MainActivity::class.java)
        contentIntent.putExtra("Forgotten_entry_year", "$year")
        contentIntent.putExtra("Forgotten_entry_month", monthString)
        contentIntent.putExtra("Forgotten_entry_day", dayString)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            2,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.icon_foreground)
            .setContentTitle(title)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(applicationContext.getColor(R.color.colorIconNotif))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notify(nID, builder.build())
    }
}

class DailyAlarmsWorker (appcontext: Context, workerParams: WorkerParameters):
    Worker(appcontext, workerParams) {

    companion object {
        private const val REMINDER_WORK_NAME = "notification_daily_tag"
        private const val PARAM_NAME = "name"
        private const val NOTIF_ID = "notif_id"
        private const val NB_REPEAT = 3
        private val log = Logger.getLogger(MainActivity::class.java.name + "Notification.DailyAlarmsWorker")


        fun runAt(
            reminderTime: String,
            context: Context,
            delay: Int = 0
        ) {
            val (hours, min) = reminderTime.split(":").map { it.toInt() }

            val calendarDelay: Calendar = Calendar.getInstance()
            val calendar: Calendar = Calendar.getInstance()

            calendarDelay.set(Calendar.HOUR_OF_DAY, hours)
            calendarDelay.set(Calendar.MINUTE, min)
            calendarDelay.set(Calendar.SECOND, 0)

            // si heure de l alarme est deja passee aujourd hui -> on programme pas
            if (calendarDelay.before(calendar) || delay != 0) {
                calendarDelay.add(Calendar.DATE, 1)
            }

            for (i in 0 until NB_REPEAT) {
                val notifID = calendarDelay.get(Calendar.MONTH) * 1000000 +
                        calendarDelay.get(Calendar.DAY_OF_MONTH) * 10000 +
                        calendarDelay.get(Calendar.HOUR_OF_DAY) * 100 +
                        calendarDelay.get(Calendar.MINUTE)
                //calendarDelay.add(Calendar.MINUTE, dayToAdd * 2)
                log.info("runAt : dayOfMonth ${calendarDelay.get(Calendar.DAY_OF_MONTH)}, i = $i")
                log.info("runAt : notifID ${notifID}")
                log.info("delay ${calendarDelay.timeInMillis - calendar.timeInMillis}")
                val workName = "$REMINDER_WORK_NAME$reminderTime $i"

                val data = workDataOf(PARAM_NAME to reminderTime, NOTIF_ID to notifID)

                val alarmWorkRequest =
                    OneTimeWorkRequestBuilder<DailyAlarmsWorker>()
                        .setInputData(data)
                        .setInitialDelay(
                            calendarDelay.timeInMillis - calendar.timeInMillis,
                            //10000,
                            TimeUnit.MILLISECONDS
                        )
                        .addTag(REMINDER_WORK_NAME + "$notifID")
                        .build()
                WorkManager.getInstance(context).enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, alarmWorkRequest)
                calendarDelay.add(Calendar.DATE, 1)
            }
        }
        fun cancel(context: Context, reminderTime: String) {
            for (i in 0 until NB_REPEAT) {
                val workName = "$REMINDER_WORK_NAME$reminderTime $i"
                log.info("cancel $workName")
                WorkManager.getInstance(context).cancelUniqueWork(workName)
            }
        }
        fun cancelAll(context: Context) {
            for (hour in 0 until 24) {
                for (min in 0 until 60) {
                    var workname = REMINDER_WORK_NAME
                    workname+= if (hour <10 )  "0$hour:" else "$hour:"
                    workname+= if (min <10 )  "0$min" else "$min"
                    for (i in 0 until NB_REPEAT) {
                        WorkManager.getInstance(context).cancelUniqueWork("$workname $i")
                    }
                    log.info("cancel all $workname")
                }
            }
            log.info("cancel all")
        }
    }

    override fun doWork(): Result {
        val context = applicationContext
        val reminderTime = inputData.getString(PARAM_NAME) as String
        val notifID = inputData.getInt(NOTIF_ID, 0)
        var isScheduleNext = true

        try {
            val notificationManager = getSystemService(
                context,
                NotificationManager::class.java
            ) as NotificationManager
            log.info("send notif $notifID")
            notificationManager.sendReminderNotification(
                applicationContext = context,
                channelId = CHANNEL_ID,
                notifID = notifID
            )
            return Result.success()
        }
        catch (e: Exception) {
        // only retry 3 times
            if (runAttemptCount > 3) {
                return Result.success()
            }
            // retry if network failure, else considered failed
            return when(e.cause) {
                is SocketException -> {
                    isScheduleNext = false
                    Result.retry()
                }

                else -> Result.failure()
            }
        }
        finally {
            // schedule alarms for the NB_REPEAT next days
            // don t overwrite alarms because we use APPEND_OR_REPLACE policy
            runAt(reminderTime, context, 1)
            log.info("set next alarms at $reminderTime")}// schedule for next day
    }

    private fun NotificationManager.sendReminderNotification(
        applicationContext: Context,
        channelId: String,
        notifID: Int
    ) {
        log.info("begin sendReminderNotification")
        val contentIntent = Intent(applicationContext, MainActivity::class.java)
        log.info("begin sendReminderNotification making builder")
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            1,
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        log.info("begin sendReminderNotification making builder")
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.icon_foreground)
            .setContentTitle(applicationContext.getString(R.string.app_notification))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(applicationContext.getColor(R.color.colorIconNotif))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notify(notifID, builder.build())
        log.info("Send reminder : notif ID $notifID")
    }
}

fun createNotifForget(context: Context, time: LocalTime = Settings.notificationTime) {
    createNotificationsChannels(context)
    val timeStr = ResUtil.getTimeStringEN(time)
    ForgottenEntranceAlarmsWorker.runAt(timeStr, context)
}

fun deleteNotifForget(context: Context) {
    ForgottenEntranceAlarmsWorker.cancel(context)
}

fun deleteNotifForgetTomorrow(context: Context, time: LocalTime = Settings.notificationTime) {
    //RemindersManager.stopReminder(context)
    val timeStr = ResUtil.getTimeStringEN(time)
    ForgottenEntranceAlarmsWorker.cancelTomorrow(context, timeStr)
}

fun createNotifSeveral(context: Context, time: LocalTime = Settings.notificationTime) {
    createNotificationsChannels(context)
    val timeStr = ResUtil.getTimeStringEN(time)
    DailyAlarmsWorker.runAt(timeStr, context)
}

fun deleteNotifSeveral(context: Context, time: LocalTime) {
    val timeStr = ResUtil.getTimeStringEN(time)
    DailyAlarmsWorker.cancel(context, timeStr)
}

fun deleteAllNotifSeveral(context: Context) {
    DailyAlarmsWorker.cancelAll(context)
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

fun cancelNotification (context: Context) {
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancelAll()
}
