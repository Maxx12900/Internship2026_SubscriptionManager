package com.example.subscriptionmanager.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK
}

object ThemeManager {
    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val savedMode = prefs.getString("theme_mode", AppThemeMode.SYSTEM.name)
        _themeMode.value = try {
            AppThemeMode.valueOf(savedMode ?: AppThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    fun setThemeMode(context: Context, mode: AppThemeMode) {
        _themeMode.value = mode
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit()
            .putString("theme_mode", mode.name)
            .apply()
    }
}