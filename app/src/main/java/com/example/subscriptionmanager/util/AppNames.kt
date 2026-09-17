package com.example.subscriptionmanager.util

import android.content.Context

fun getInstalledApps(context: Context): Array<String> {
    val packageManager = context.packageManager
    val packages = packageManager.getInstalledPackages(0)
    return packages.map { it.packageName }.toTypedArray()
}