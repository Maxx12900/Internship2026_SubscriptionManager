package com.example.subscriptionmanager.data.analysis

import android.app.Activity
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import com.example.subscriptionmanager.notifications.createNotification
import com.example.subscriptionmanager.notifications.showNotification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.function.Predicate



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



fun analyzeSubscription(activity: Activity, packageName: String) {
    val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())  // for debugging purposes

    // TODO: get the intervalMillis from the database knowing the packageName and last renewal date
    val interval = 1000 * 60 * 60 * 24
    val usageStats = getApplicationsUsageData(
        activity,
        interval
    )

    if (usageStats[packageName] == null) {
        // this means one of two things:
        // 1) User denied granting permission
        // 2) The app is not installed on this device
        // in both cases it is meaningless to try and analyze the subscription usage
        // though, maybe this means we will be reminding the user about it regardless?
        // if we cannot check activity, I think it's best to keep notifying the user about renewals
        return
    }

    // analyze the subscription
    val appUsageStat = usageStats[packageName]!!

    // INFO: analysis is done in the following way: check for how many milliseconds the app has been used on this device
    // INFO: there will be a threshold below which we will notify the user about considering to remove the subscription
    // INFO: if the subscription is indeed used, then no notification will be sent on renewal
    // INFO: though, maybe there should be an option to send reminders about renewals anyway?

    val usageTime = appUsageStat.totalTimeVisible  // for how long the app has been actively used
    val usagePercentage = usageTime.toDouble() / interval.toDouble()
    val usageThreshold = 0.0005  // using the app for more that this amound will consider the app as 'actively used'

    if (usagePercentage < usageThreshold) {
        val n = createNotification(activity, "Unused Subscription", "You might want to cancel the $packageName subscription!")
        showNotification(activity, n)
    }
}
