package com.example.subscriptionmanager.data.analysis

import android.app.Activity
import android.app.usage.UsageStats
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
    val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())

    val usageStats = getApplicationsUsageData(activity)

    for (usageStat: UsageStats in usageStats) {
        if (usageStat.packageName == packageName) {
            println("Package name: " + usageStat.packageName)
            print("Used for: ")
            print(usageStat.totalTimeVisible / 1000)
            print(" seconds\n")


            print("Last time opened: ")
            print(formatter.format(Date(usageStat.lastTimeVisible)))
            println()
            println("-----")
        }
    }
}
