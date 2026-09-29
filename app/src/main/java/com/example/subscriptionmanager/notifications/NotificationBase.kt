package com.example.subscriptionmanager.notifications

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.subscriptionmanager.MainActivity
import com.example.subscriptionmanager.R

const val CHANNEL_ID: String = "subscriptionManager"
private var current_notification_id = 0

data class Notification(
    val id: Int,
    val builder: NotificationCompat.Builder
)

fun createNotification(context: Context, title: String, content: String): Notification {
    current_notification_id++  // make sure each notification is unique

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_home)
        .setContentTitle(title)
        .setContentText(content)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    val notification = Notification(current_notification_id, builder)

    return notification
}

fun showNotification(activity: Activity, notification: Notification) {
    with (NotificationManagerCompat.from(activity)) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
            // Request permission to send notifications here
            // TODO: if the user already denied giving access to notifications, persuade them
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )

            return@with  // return from the current "with" label
        }

        notify(notification.id, notification.builder.build())
    }
}

fun sendNotification(activity: Activity, notification: Notification) {
    val intent = Intent(activity, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra("NOTIFICATION_PAYLOAD", "Secret payload details here I suppose")
    }

    val pendingIntent = PendingIntent.getActivity(
        activity,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    notification.builder.setContentIntent(pendingIntent)

    showNotification(activity, notification)
}

@RequiresApi(Build.VERSION_CODES.O)
fun createNotificationChannel(context: Context) {
    val name: String = "Subscription Manager Notification Channel";
    val descriptionText: String = "Channel for notifications from Subscription Manager";
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
        description = descriptionText
    }

    val notificationManager: NotificationManager = context.getSystemService(
        NotificationManager::class.java
    ) as NotificationManager

    notificationManager.createNotificationChannel(channel)
}