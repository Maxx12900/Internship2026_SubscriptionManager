package com.example.subscriptionmanager.util
import android.content.Context
import android.content.pm.PackageManager
// Returns a list of  launchable installed apps sorted alphabetically.
fun getInstalledApps(context: Context): List<Pair<String, String>> {
    val pm = context.packageManager
    val packages = pm.getInstalledPackages(0)

    return packages.mapNotNull { pkg ->
        val appInfo = pkg.applicationInfo ?: return@mapNotNull null

        // Filter: Keep only apps that have a launch intent
        val launchIntent = pm.getLaunchIntentForPackage(pkg.packageName)
        if (launchIntent == null) return@mapNotNull null

        val label = pm.getApplicationLabel(appInfo).toString()
        if (label.isNotBlank()) pkg.packageName to label else null
    }.sortedBy { it.second.lowercase() }
}