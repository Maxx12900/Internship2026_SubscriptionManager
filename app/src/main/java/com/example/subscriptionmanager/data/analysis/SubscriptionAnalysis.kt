package com.example.subscriptionmanager.data.analysis

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import com.example.subscriptionmanager.data.entities.BillingPeriod
import com.example.subscriptionmanager.data.entities.SubscriptionEntity
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


fun backtrackTime(cal: Calendar, t: BillingPeriod): Calendar {
    when(t) {
        BillingPeriod.MONTHLY -> cal.add(Calendar.MONTH, -1)
        BillingPeriod.THREE_MONTHS -> cal.add(Calendar.MONTH, -3)
        BillingPeriod.SIX_MONTHS -> cal.add(Calendar.MONTH, -6)
        BillingPeriod.YEARLY -> cal.add(Calendar.YEAR, -1)
        BillingPeriod.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, -1)
    }

    return cal
}

@RequiresApi(Build.VERSION_CODES.Q)
fun analyzeSubscription(context: Context, subscription: SubscriptionEntity): AnalysisResponseType {
    if (subscription.packageName == null) return AnalysisResponseType.NO_PACKAGE

    var interval: Long = subscription.nextRenewalDate.timeInMillis - backtrackTime(subscription.nextRenewalDate,
        BillingPeriod.entries[subscription.billingPeriod]).timeInMillis

    val end = subscription.nextRenewalDate.timeInMillis
    val today = Calendar.getInstance().timeInMillis

    interval -= (end - today)  // we ignore the time remaining from today till the renewal date, since we haven't possibly used the app in the future

    val usageStats = getApplicationsUsageData(
        context,
        interval
    )

    if (usageStats[subscription.packageName] == null) {
        // this means one of two things:
        // 1) User denied granting permission
        // 2) The app is not installed on this device (unlikely this is the reason, but oh well)
        // in both cases it is meaningless to try and analyze the subscription usage
        // though, maybe this means we will be reminding the user about it regardless?
        // if we cannot check activity, I think it's best to keep notifying the user about renewals
        return AnalysisResponseType.NO_PACKAGE
    }

    // analyze the subscription
    val appUsageStat = usageStats[subscription.packageName]!!

    // INFO: analysis is done in the following way: check for how many milliseconds the app has been used on this device
    // INFO: there will be a threshold below which we will notify the user about considering to remove the subscription
    // INFO: if the subscription is indeed used, then no notification will be sent on renewal
    // INFO: though, maybe there should be an option to send reminders about renewals anyway?

    val usageTime = appUsageStat.totalTimeVisible  // for how long the app has been actively used

    //
    // Linear relation (the bigger the number, the bigger the score):
    // - usage time
    //
    // Inverse relation (the bigger the number, the smaller the score):
    // - price
    // - billing period
    //

    val usageScore: Double = 100000 * usageTime.toDouble() / (interval.toDouble() * subscription.price)
    val scoreThreshold = 5  // score above this to consider the app as "actively used"

    return if (usageScore > scoreThreshold) {
        AnalysisResponseType.USED
    } else {
        AnalysisResponseType.NOT_USED
    }
}
