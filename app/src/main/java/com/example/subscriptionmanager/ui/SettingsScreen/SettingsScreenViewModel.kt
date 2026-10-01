package com.example.subscriptionmanager.ui.SettingsScreen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SettingsUiState(
    val username: String = "Username",
    val email: String = "E-mail",
    val language: String = "English",
    val dateFormat: String = "DD/MM/YYYY",
    val timeFormat: String = "24-hour",
    val currency: String = "USD",
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    fun updateLanguage(value: String) { _uiState.value = _uiState.value.copy(language = value) }
    fun updateDateFormat(value: String) { _uiState.value = _uiState.value.copy(dateFormat = value) }
    fun updateTimeFormat(value: String) { _uiState.value = _uiState.value.copy(timeFormat = value) }
    fun updateCurrency(value: String) { _uiState.value = _uiState.value.copy(currency = value) }
    fun toggleDarkMode(enabled: Boolean) { _uiState.value = _uiState.value.copy(isDarkMode = enabled) }
    fun toggleNotifications(enabled: Boolean) { _uiState.value = _uiState.value.copy(notificationsEnabled = enabled) }
}