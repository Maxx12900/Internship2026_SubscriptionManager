package com.example.subscriptionmanager.data

import android.Manifest
import android.app.Activity
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri

fun getApplicationUsageData(activity: Activity): List<UsageStats> {
    if (ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // request permission
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            data = "package:${activity.packageName}".toUri()
        }
        activity.startActivity(intent)

        return listOf()  // nothing to return
    }
    val usageStatsManager = activity.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    // get the beggining of the day
    val calendar = Calendar.getInstance()

    calendar.set(Calendar.DAY_OF_YEAR, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val startOfDay = calendar.timeInMillis
    val now = System.currentTimeMillis()

    return usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_BEST,
        startOfDay,
        now
    )
}