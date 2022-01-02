package com.productivity.fullmangement.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.productivity.fullmangement.R
import com.productivity.fullmangement.ui.main.HomeActivity
import timber.log.Timber
import kotlin.random.Random

// Notification ID.
private val NOTIFICATION_ID = 0
private val REQUEST_CODE = 0
private val FLAGS = 0

fun sendNotification(
    notificationTitle: String,
    notificationDesc: String,
    taskId: Long,
    applicationContext: Context
) {
    // Create the content intent for the notification, which launches
    // this activity
    val contentIntent = Intent(applicationContext, HomeActivity::class.java).apply {
        action = System.currentTimeMillis().toString()
        putExtra("taskId", taskId)
    }

    //To back to the All Tasks activity when the user press back after open the task details activity from notification
    val stackBuilder = TaskStackBuilder.create(applicationContext)

    stackBuilder.addNextIntentWithParentStack(contentIntent)
    val contentPendingIntent = stackBuilder.getPendingIntent(taskId.toInt(), PendingIntent.FLAG_UPDATE_CURRENT)

    val summaryIntent = Intent(applicationContext, HomeActivity::class.java)
    stackBuilder.addNextIntentWithParentStack(summaryIntent)
    val summaryPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

    val soundTasks = Uri.parse("android.resource://" + applicationContext.packageName + "/" + R.raw.iphone)
    val notificationManager = NotificationManagerCompat.from(applicationContext)
    val notificationCompatBuilder: NotificationCompat.Builder =
        NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.full_management_notification_channel_id)
        )
    val randomNumber = Random.nextInt()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        notificationCompatBuilder
            .setSmallIcon(R.drawable.ic_full_management_icon)
            .setColor(applicationContext.getColor(R.color.colorAccent))
            .setContentTitle(notificationTitle)
            .setContentText(notificationDesc)
            .setOnlyAlertOnce(false)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(notificationDesc)
            )
            .setGroup(applicationContext.getString(R.string.group_key_full_management))
            .setSound(soundTasks)
            .setVibrate(longArrayOf(1000, 1000))
            .setLights(Color.GREEN, 3000, 3000)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .build()

        val summaryNotification = NotificationCompat.Builder(
                applicationContext,
                applicationContext.getString(R.string.full_management_notification_channel_id)
            )
            .setSmallIcon(R.drawable.ic_full_management_icon)
            .setColor(applicationContext.getColor(R.color.colorAccent))
            .setStyle(
                NotificationCompat.InboxStyle()
                    .addLine("$notificationTitle $notificationDesc")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(applicationContext.getString(R.string.group_key_full_management))
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
            .setGroupSummary(true)
            .setContentIntent(summaryPendingIntent)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(
                applicationContext.getString(R.string.full_management_notification_channel_id),
                applicationContext.getString(R.string.full_management_notification_channel_name),
                soundTasks,
                applicationContext
            )
        }

        notificationManager.notify(taskId.toInt(), notificationCompatBuilder.build())
        notificationManager.notify(0, summaryNotification)
    } else {
        notificationCompatBuilder
                .setSmallIcon(R.drawable.ic_full_management_icon)
                .setColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        applicationContext.resources,
                        R.drawable.ic_full_management_icon
                    )
                )
                .setContentTitle(notificationTitle)
                .setContentText(notificationDesc)
                .setVibrate(longArrayOf(1000, 1000))
                .setSound(soundTasks)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentPendingIntent)
                .setOnlyAlertOnce(false)
                .setAutoCancel(true)

        val bigTextStyle = NotificationCompat.BigTextStyle()
        bigTextStyle.setBigContentTitle(notificationTitle)
        bigTextStyle.bigText(notificationDesc)
        notificationCompatBuilder.setStyle(bigTextStyle)

        notificationManager.notify(taskId.toInt(), notificationCompatBuilder.build())
    }
}

fun createChannel(channelId: String, channelName: String, sound: Uri, context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()

        notificationChannel.apply {
            setSound(sound, audioAttributes)
            setShowBadge(true)
            enableLights(true)
            lightColor = Color.GREEN
            enableVibration(true)
            description = "notify you when your task deadline approaches"
        }

        val notificationManager = getSystemService(
            context,
            NotificationManager::class.java
        )
        notificationManager?.createNotificationChannel(notificationChannel)
    }
}

// TODO: Step 1.14 Cancel all notifications
/**
 * Cancels all notifications.
 *
 */
fun NotificationManager.cancelNotifications() {
    cancelAll()
}