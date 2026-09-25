package com.example.subscriptionmanager.data.analysis

import androidx.lifecycle.lifecycleScope
import android.app.Activity
import androidx.activity.ComponentActivity
import com.example.subscriptionmanager.SubscriptionManagerApplication
import com.example.subscriptionmanager.data.entities.BillingPeriod
import com.example.subscriptionmanager.data.entities.SubscriptionEntity
import com.example.subscriptionmanager.data.repository.SubscriptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.function.Predicate


enum class AnalysisResponseType {
    USED,
    NOT_USED,
    NO_PACKAGE
}

fun debugPrintPackageNames(activity: Activity, condition: Predicate<String>) {
    val usageStats = getApplicationsUsageData(
        activity,
        1000 * 60 * 60 * 24  // 1 day
    )

    val packageNames = mutableListOf<String>();

    for (packageName: String in usageStats.keys) {
        if (condition.test(packageName)) {
            packageNames += packageName
        }
    }

    println(packageNames)
}



fun analyzeSubscription(activity: ComponentActivity, packageName: String): AnalysisResponseType {
    val repository: SubscriptionRepository = (activity.applicationContext as SubscriptionManagerApplication).repository
    var activelyUsed: AnalysisResponseType = AnalysisResponseType.NO_PACKAGE
    activity.lifecycleScope.launch {
        // since we're working with applications on the phone, we need to get access to the subscription by the packageName
        val subscription: SubscriptionEntity? = withContext(Dispatchers.IO) {
            repository.getSubscriptionByPackageName(packageName)
        }
        if (subscription == null) return@launch

        var interval: Long = 0
        if (subscription.billingPeriod == BillingPeriod.MONTHLY.ordinal) {
            val i = subscription.nextRenewalDate
            i.add(Calendar.MONTH, -1)

            interval = subscription.nextRenewalDate.timeInMillis - i.timeInMillis
        } else if (subscription.billingPeriod == BillingPeriod.THREE_MONTHS.ordinal) {
            val i = subscription.nextRenewalDate
            i.add(Calendar.MONTH, -3)

            interval = subscription.nextRenewalDate.timeInMillis - i.timeInMillis
        } else if (subscription.billingPeriod == BillingPeriod.SIX_MONTHS.ordinal) {
            val i = subscription.nextRenewalDate
            i.add(Calendar.MONTH, -6)

            interval = subscription.nextRenewalDate.timeInMillis - i.timeInMillis
        } else if (subscription.billingPeriod == BillingPeriod.WEEKLY.ordinal) {
            val i = subscription.nextRenewalDate
            i.add(Calendar.WEEK_OF_YEAR, -1)

            interval = subscription.nextRenewalDate.timeInMillis - i.timeInMillis
        } else if (subscription.billingPeriod == BillingPeriod.YEARLY.ordinal) {
            val i = subscription.nextRenewalDate
            i.add(Calendar.YEAR, -1)

            interval = subscription.nextRenewalDate.timeInMillis - i.timeInMillis
        }

        val end = subscription.nextRenewalDate.timeInMillis
        val today = Calendar.getInstance().timeInMillis

        interval -= (end - today)  // we ignore the time remaining from today till the renewal date, since we haven't possibly used the app in the future


        val usageStats = getApplicationsUsageData(
            activity,
            interval
        )

        if (usageStats[packageName] == null) {
            // this means one of two things:
            // 1) User denied granting permission
            // 2) The app is not installed on this device (unlikely this is the reason, but oh well)
            // in both cases it is meaningless to try and analyze the subscription usage
            // though, maybe this means we will be reminding the user about it regardless?
            // if we cannot check activity, I think it's best to keep notifying the user about renewals
            return@launch
        }

        // analyze the subscription
        val appUsageStat = usageStats[packageName]!!

        // INFO: analysis is done in the following way: check for how many milliseconds the app has been used on this device
        // INFO: there will be a threshold below which we will notify the user about considering to remove the subscription
        // INFO: if the subscription is indeed used, then no notification will be sent on renewal
        // INFO: though, maybe there should be an option to send reminders about renewals anyway?

        val usageTime = appUsageStat.totalTimeVisible  // for how long the app has been actively used

        val usageScore: Double = 100000 * usageTime.toDouble() / (interval.toDouble() * subscription.price)
        val scoreThreshold = 5


        activelyUsed = if (usageScore > scoreThreshold) {
            AnalysisResponseType.USED
        } else {
            AnalysisResponseType.NOT_USED
        }
        return@launch
    }

    return activelyUsed
}
