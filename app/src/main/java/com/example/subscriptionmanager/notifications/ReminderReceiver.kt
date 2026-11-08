package com.example.subscriptionmanager.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.subscriptionmanager.SubscriptionManagerApplication
import com.example.subscriptionmanager.data.analysis.AnalysisResponseType
import com.example.subscriptionmanager.data.analysis.analyzeSubscription
import com.example.subscriptionmanager.data.entities.BillingPeriod
import com.example.subscriptionmanager.data.entities.NotificationEntity
import com.example.subscriptionmanager.data.entities.SubscriptionEntity
import com.example.subscriptionmanager.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.first

class ReminderReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onReceive(context: Context, intent: Intent) {
        val subscriptionId: Int = intent.getIntExtra("EXTRA_SUBSCRIPTION_ID", -1)
        if (subscriptionId == -1) return // no point in going forward, I mean, we don't even know what subscription to analyze

        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<RenewalNotificationWorker>()
                .setInputData(
                    workDataOf("KEY_SUBSCRIPTION_ID" to subscriptionId)
                )
                .build()
        )
    }
}

class RenewalNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
): CoroutineWorker(context, workerParams) {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun doWork(): Result {
        val subscriptionId = inputData.getInt("KEY_SUBSCRIPTION_ID", -1)
        if (subscriptionId == -1) return Result.failure()

        val repository: SubscriptionRepository = (context.applicationContext as SubscriptionManagerApplication).repository

        val subscription: SubscriptionEntity =
            repository.getSubscriptionById(subscriptionId) ?: return Result.failure()

        val reminderSettings: NotificationEntity? =
            repository.getNotificationsForSubscription(subscriptionId).first().firstOrNull()

        // Reminder was switched off after the alarm was armed: don't notify, don't re-arm
        if (reminderSettings != null && !reminderSettings.shouldRemind) return Result.success()

        val verdict = analyzeSubscription(context, subscription)

        println("WHERE NOTIFICATION I'M ASKING")

        val title = "Renewal Reminder"
        val name = subscription.name
        val content: String = if (verdict == AnalysisResponseType.NOT_USED) {
            "You aren't using $name that often, maybe consider unsubscribing?"
        } else {
            "Renewal for $name soon"

        }

        val notification = createNotification(
            context,
            title,
            content,
            ChannelIds.RENEWAL
        )

        notification.builder
            .setAutoCancel(true)

        showNotification(context, notification)

        if (reminderSettings != null) {
            rescheduleNextReminder(subscription, reminderSettings, repository)
        }

        return Result.success()
    }

    /** Arms the reminder for the following billing cycle so reminders keep recurring. */
    private suspend fun rescheduleNextReminder(
        subscription: SubscriptionEntity,
        reminderSettings: NotificationEntity,
        repository: SubscriptionRepository
    ) {
        val billingPeriod =
            BillingPeriod.entries.getOrNull(subscription.billingPeriod) ?: BillingPeriod.MONTHLY

        val nextReminderDate = nextReminderDateAfter(
            subscription.nextRenewalDate,
            billingPeriod,
            reminderSettings.daysBeforeToRemind
        )

        val scheduled = scheduleReminder(context, nextReminderDate, subscription.id)

        repository.insertNotification(
            reminderSettings.copy(
                reminderDate = nextReminderDate,
                scheduledReminder = scheduled != null,
                scheduledNotificationId = scheduled?.id
            )
        )
    }
}