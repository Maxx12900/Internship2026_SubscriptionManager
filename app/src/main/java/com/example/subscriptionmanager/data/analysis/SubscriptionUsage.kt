package com.example.subscriptionmanager.data.analysis

import android.Manifest
import android.app.Activity
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import kotlin.collections.set

fun getApplicationsUsageData(activity: Activity, intervalMillis: Int): Map<String, UsageStats> {
    // intervalMillis - how long is the interval you want to analyze (in milliseconds).
    if (ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.PACKAGE_USAGE_STATS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // request permission
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            data = "package:${activity.packageName}".toUri()
        }
        activity.startActivity(intent)

        return mapOf()  // nothing to return
    }

    val usageStatsManager = activity.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    val now = System.currentTimeMillis()

    return usageStatsManager.queryAndAggregateUsageStats(
        now - intervalMillis,
        now
    )
}