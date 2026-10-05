package com.example.subscriptionmanager.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
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
import androidx.core.content.ContextCompat
import com.example.subscriptionmanager.MainActivity
import com.example.subscriptionmanager.R
import java.util.Calendar

private var current_notification_id = 0

data class Notification(
    val id: Int,
    val builder: NotificationCompat.Builder
)

data class ScheduledNotification(
    val id: Int,
    val triggerAtMillis: Long
)

enum class ChannelIds(val id: String) {
    RENEWAL("Renewal")
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun requestNotificationPermission(activity: Activity) {
    // activity is required
    ActivityCompat.requestPermissions(
        activity,
        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
        1
    )
}


fun areNotificationsEnabled(context: Context): Boolean {
    val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    return prefs.getBoolean("notifications_enabled", true)
}

fun areNotificationsAllowed(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}

fun createNotification(context: Context, title: String, content: String, channelId: ChannelIds): Notification {
    current_notification_id++

    val builder = NotificationCompat.Builder(context, channelId.id)
        .setSmallIcon(R.drawable.ic_home)
        .setContentTitle(title)
        .setContentText(content)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    return Notification(current_notification_id, builder)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@SuppressLint("MissingPermission")
fun showNotification(context: Context, notification: Notification) {
    // Check if notifications are turned ON in Settings
    if (!areNotificationsEnabled(context)) return

    with(NotificationManagerCompat.from(context)) {
        if (!areNotificationsAllowed(context)) {
            println("No notifications permission bruh, fix this NOW")

            if (context is Activity) {
                requestNotificationPermission(context)
            }
            return@with
        }

        notify(notification.id, notification.builder.build())
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
fun createNotificationChannel(context: Context, name: String, descriptionText: String, channelId: ChannelIds) {
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel(channelId.id, name, importance).apply {
        description = descriptionText
    }

    val notificationManager: NotificationManager = context.getSystemService(
        NotificationManager::class.java
    ) as NotificationManager

    notificationManager.createNotificationChannel(channel)
}

fun scheduleReminder(context: Context, dateTime: Calendar, subscriptionId: Int): ScheduledNotification? {
    if (!areNotificationsEnabled(context)) return null

    val alarmManager = context.getSystemService(AlarmManager::class.java)

    current_notification_id++
    val scheduledNotification = ScheduledNotification(current_notification_id, dateTime.timeInMillis)

    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("EXTRA_SUBSCRIPTION_ID", subscriptionId)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        scheduledNotification.id,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    alarmManager.setAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP, scheduledNotification.triggerAtMillis, pendingIntent
    )

    return scheduledNotification
}