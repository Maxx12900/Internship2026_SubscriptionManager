package com.example.subscriptionmanager.notifications

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.subscriptionmanager.R

const val CHANNEL_ID: String = "subscriptionManager"
var notification_id = 0

fun createNotification(context: Context, title: String, content: String): NotificationCompat.Builder {
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_home)
        .setContentTitle(title)
        .setContentText(content)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    return builder
}

fun showNotification(activity: Activity, notificationBuilder: NotificationCompat.Builder) {
    notification_id++  // make sure each notification is unique
    with (NotificationManagerCompat.from(activity)) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
            // Request permission to send notifications here

            ActivityCompat.requestPermissions(
                activity,
                listOf(Manifest.permission.POST_NOTIFICATIONS).toTypedArray(),
                1)

            return@with  // return from the current "with" label
        }

        notify(notification_id, notificationBuilder.build())
    }
}

fun createNotificationChannel(context: Context) {
    // NotificationChannel is supported only for API 26+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
}