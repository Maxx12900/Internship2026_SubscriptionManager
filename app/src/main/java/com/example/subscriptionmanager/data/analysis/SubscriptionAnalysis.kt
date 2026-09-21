package com.example.subscriptionmanager.data.analysis

import android.app.Activity
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun debugPrintPackageNames(activity: Activity) {
    val usageStats = getApplicationsUsageData(activity)

    val packageNames = mutableListOf<String>();

    for (usageStat: UsageStats in usageStats) {
        packageNames += usageStat.packageName
    }

    println(packageNames)
}


fun analyzeSubscription(activity: Activity, packageName: String) {
    val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())  // for debugging purposes

    // TODO: get the intervalMillis from the database knowing the packageName and last renewal date
    val interval = 1000 * 60 * 60 * 24
    val usageStats = getApplicationsUsageData(
        activity,
        UsageStatsManager.INTERVAL_BEST,
        interval
    )

    val appUsageStatList = usageStats.filter { it.packageName == packageName }

    if (appUsageStatList.isEmpty()) {
        // this means one of two things:
        // 1) User denied granting permission
        // 2) The app is not installed on this device
        // in both cases it is meaningless to try and analyze the subscription usage
        // though, maybe this means we will be reminding the user about it regardless?
        // if we cannot check activity, I think it's best to keep notifying the user about renewals
        return
    }

    if (appUsageStatList.size > 1) {
        // ideally we should not get more than 1 packageName
        // NOTE: this is assuming that UsageStatsManager.INTERVAL_BEST is selected at all times.
        // NOTE: Otherwise, feel free to remove this clause and adjust the function
        error("More than one package name was found for: $packageName")
    }

    // analyze the subscription
    val appUsageStat = appUsageStatList[0]

    // INFO: analysis is done in the following way: check for how many milliseconds the app has been used on this device
    // INFO: there will be a threshold below which we will notify the user about considering to remove the subscription
    // INFO: if the subscription is indeed used, then no notification will be sent on renewal
    // INFO: though, maybe there should be an option to send reminders about renewals anyway?

    val usageTime = appUsageStat.totalTimeVisible  // for how long the app has been actively used
    val usagePercentage = usageTime / interval
    val usageThreshold = 0.0005  // using the app for more that this amound will consider the app as 'actively used'
    if (usagePercentage < usageThreshold) {

    }

//    for (usageStat: UsageStats in usageStats) {
//        if (usageStat.packageName == packageName) {
//            println("Package name: " + usageStat.packageName)
//            print("Used for: ")
//            print(usageStat.totalTimeVisible / 1000)
//            print(" seconds\n")
//
//
//            print("Last time opened: ")
//            print(formatter.format(Date(usageStat.lastTimeVisible)))
//            println()
//            println("-----")
//        }
//    }
}
