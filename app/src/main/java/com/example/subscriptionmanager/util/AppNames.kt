package com.example.subscriptionmanager.util
import android.content.Context
import android.content.pm.PackageManager
import android.content.Intent
import android.os.Build

// Returns a list of  launchable installed apps sorted alphabetically.
//fun getInstalledApps(context: Context): List<Pair<String, String>> {
//    val pm = context.packageManager
//    val packages = pm.getInstalledPackages(0)
//
//    return packages.mapNotNull { pkg ->
//        val appInfo = pkg.applicationInfo ?: return@mapNotNull null
//
//        // Filter: Keep only apps that have a launch intent
//        val launchIntent = pm.getLaunchIntentForPackage(pkg.packageName)
//        if (launchIntent == null) return@mapNotNull null
//
//        val label = pm.getApplicationLabel(appInfo).toString()
//        if (label.isNotBlank()) pkg.packageName to label else null
//    }.sortedBy { it.second.lowercase() }
//}

fun getInstalledApps(context: Context): List<Pair<String, String>> {
    val pm = context.packageManager
    val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }

    val resolveInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        pm.queryIntentActivities(mainIntent, 0)
    }

    return resolveInfoList.mapNotNull { resolveInfo ->
        val packageName = resolveInfo.activityInfo.packageName
        val label = resolveInfo.loadLabel(pm).toString()
        if (packageName.isNotBlank() && label.isNotBlank()) {
            packageName to label
        } else null
    }.distinctBy { it.first }.sortedBy { it.second.lowercase() }
}