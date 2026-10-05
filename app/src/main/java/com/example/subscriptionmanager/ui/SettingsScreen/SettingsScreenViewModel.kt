package com.example.subscriptionmanager.ui.SettingsScreen

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SettingsUiState(
    val username: String = "User",
    val language: String = "English",
    val dateFormat: String = "DD/MM/YYYY",
    val timeFormat: String = "24-hour",
    val currency: String = "USD",
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true
)

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        val prefs = app.getSharedPreferences("active_user", Context.MODE_PRIVATE)
        val appPrefs = app.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        val savedUsername = prefs.getString("logged_in_username", "User") ?: "User"
        val notifEnabled = appPrefs.getBoolean("notifications_enabled", true)

        _uiState.value = _uiState.value.copy(
            username = savedUsername.replaceFirstChar { it.uppercase() },
            notificationsEnabled = notifEnabled
        )
    }

    fun updateLanguage(value: String) { _uiState.value = _uiState.value.copy(language = value) }
    fun updateDateFormat(value: String) { _uiState.value = _uiState.value.copy(dateFormat = value) }
    fun updateTimeFormat(value: String) { _uiState.value = _uiState.value.copy(timeFormat = value) }
    fun updateCurrency(value: String) { _uiState.value = _uiState.value.copy(currency = value) }
    fun toggleDarkMode(enabled: Boolean) { _uiState.value = _uiState.value.copy(isDarkMode = enabled) }

    fun toggleNotifications(context: Context, enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("notifications_enabled", enabled)
            .apply()
    }
    fun loadActiveUser() {
        val prefs = getApplication<Application>().getSharedPreferences("active_user", Context.MODE_PRIVATE)
        val savedUsername = prefs.getString("logged_in_username", "User") ?: "User"

        _uiState.value = _uiState.value.copy(
            username = savedUsername.replaceFirstChar { it.uppercase() }
        )
    }
}