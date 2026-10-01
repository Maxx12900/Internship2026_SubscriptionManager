package com.example.subscriptionmanager.notifications

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.subscriptionmanager.SubscriptionManagerApplication
import com.example.subscriptionmanager.data.analysis.AnalysisResponseType
import com.example.subscriptionmanager.data.analysis.analyzeSubscription
import com.example.subscriptionmanager.data.model.Subscription
import com.example.subscriptionmanager.data.repository.SubscriptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ReminderReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onReceive(context: Context, intent: Intent) {
        println("WHERE NOTIFICATION???")
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<RenewalNotificationWorker>().build()
        )
    }
}

class RenewalNotificationWorker(
    private val context: Context, workerParams: WorkerParameters
): CoroutineWorker(context, workerParams) {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun doWork(): Result {
        // TODO: currently this function analyses every subscription per every renewal date set
        // probably should make it so that only the subscription with the current renewal date reminer
        // set is analyzed

        val repository: SubscriptionRepository = (context.applicationContext as SubscriptionManagerApplication).repository

        var unusedSubscriptions = 0

        val subscriptions = repository.getSubscriptions().first()
        for (subscription in subscriptions) {
            val verdict = analyzeSubscription(context, subscription)

            when (verdict) {
                AnalysisResponseType.NOT_USED -> unusedSubscriptions++
                else -> {}
            }
        }


        val title = "Subscription Usage"
        var content = "Renewal soon"
        if (unusedSubscriptions > 0) {
            content += ". $unusedSubscriptions unused subscriptions."
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

        return Result.success()
    }
}