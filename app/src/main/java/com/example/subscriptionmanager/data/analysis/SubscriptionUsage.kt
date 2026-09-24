package com.example.subscriptionmanager.data.analysis

import android.app.Activity
import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import androidx.core.net.toUri

fun requestPermissionForUsageDataAccess(activity: Activity): Int {
    val appOps = activity.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        activity.packageName
    )

    if (mode != AppOpsManager.MODE_ALLOWED) {
        // request permission
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            data = "package:${activity.packageName}".toUri()
        }
        activity.startActivity(intent)
    }

    return mode
}


fun getApplicationsUsageData(activity: Activity, intervalMillis: Int): Map<String, UsageStats> {
    // intervalMillis - how long is the interval you want to analyze (in milliseconds).

    if (requestPermissionForUsageDataAccess(activity) != AppOpsManager.MODE_ALLOWED) {
        return mapOf()  // womp womp
    }

    val usageStatsManager = activity.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    val now = System.currentTimeMillis()

    return usageStatsManager.queryAndAggregateUsageStats(
        now - intervalMillis,
        now
    )
}