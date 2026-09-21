package com.example.subscriptionmanager.util

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

fun getAppIcon(
    context: Context,
    packageName: String
) : Drawable? {
    return try {
        context.packageManager.getApplicationIcon(packageName)
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }
}