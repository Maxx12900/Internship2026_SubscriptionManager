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
import com.example.subscriptionmanager.data.entities.BillingPeriod
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

/**
 * Each subscription has at most one pending renewal reminder. The PendingIntent request code is
 * derived from the subscription id, so it is stable across process restarts: scheduling again
 * replaces the existing alarm, and [cancelReminder] can always find it.
 */
fun reminderRequestCode(subscriptionId: Int): Int = subscriptionId

private fun reminderIntent(context: Context, subscriptionId: Int): Intent =
    Intent(context, ReminderReceiver::class.java).apply {
        putExtra("EXTRA_SUBSCRIPTION_ID", subscriptionId)
    }

fun scheduleReminder(context: Context, dateTime: Calendar, subscriptionId: Int): ScheduledNotification? {
    if (!areNotificationsEnabled(context)) return null

    val alarmManager = context.getSystemService(AlarmManager::class.java)

    val scheduledNotification = ScheduledNotification(
        reminderRequestCode(subscriptionId),
        dateTime.timeInMillis
    )

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        scheduledNotification.id,
        reminderIntent(context, subscriptionId),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Same PendingIntent => AlarmManager replaces any alarm already set for this subscription
    alarmManager.setAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP, scheduledNotification.triggerAtMillis, pendingIntent
    )

    return scheduledNotification
}

/**
 * Cancels a pending reminder alarm. [requestCode] defaults to the stable per-subscription code;
 * pass a stored scheduledNotificationId to also clear alarms created by older builds.
 */
fun cancelReminder(
    context: Context,
    subscriptionId: Int,
    requestCode: Int = reminderRequestCode(subscriptionId)
) {
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        reminderIntent(context, subscriptionId),
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    ) ?: return

    context.getSystemService(AlarmManager::class.java).cancel(pendingIntent)
    pendingIntent.cancel()
}

fun addBillingPeriod(cal: Calendar, billingPeriod: BillingPeriod) {
    when (billingPeriod) {
        BillingPeriod.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
        BillingPeriod.MONTHLY -> cal.add(Calendar.MONTH, 1)
        BillingPeriod.THREE_MONTHS -> cal.add(Calendar.MONTH, 3)
        BillingPeriod.SIX_MONTHS -> cal.add(Calendar.MONTH, 6)
        BillingPeriod.YEARLY -> cal.add(Calendar.YEAR, 1)
    }
}

/**
 * Returns the first reminder date strictly after [now], starting from [renewalDate] and stepping
 * forward one billing period at a time. Used after a reminder fires to arm the next cycle.
 */
fun nextReminderDateAfter(
    renewalDate: Calendar,
    billingPeriod: BillingPeriod,
    daysBeforeToRemind: Int,
    now: Calendar = Calendar.getInstance()
): Calendar {
    val renewal = renewalDate.clone() as Calendar
    while (true) {
        val reminder = (renewal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, -daysBeforeToRemind)
        }
        if (reminder.after(now)) return reminder
        addBillingPeriod(renewal, billingPeriod)
    }
}
