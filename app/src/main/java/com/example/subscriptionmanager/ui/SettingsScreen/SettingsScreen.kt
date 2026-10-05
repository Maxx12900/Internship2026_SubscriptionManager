package com.example.subscriptionmanager.ui.SettingsScreen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subscriptionmanager.ui.common.fieldHeight
import com.example.subscriptionmanager.ui.common.padding
import com.example.subscriptionmanager.util.AppThemeMode
import com.example.subscriptionmanager.util.CurrencyManager
import com.example.subscriptionmanager.util.ThemeManager

private enum class PreferenceDialogType {
    LANGUAGE, DATE_FORMAT, TIME_FORMAT, CURRENCY, MODE, NOTIFICATIONS
}

@Composable
fun SettingsScreen(
    onLogOut: () -> Unit = {}
) {
    val selectedCurrency by CurrencyManager.currency.collectAsState()
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadActiveUser()
    }

    val themeMode by ThemeManager.themeMode.collectAsState()
    val currentModeLabel = when (themeMode) {
        AppThemeMode.SYSTEM -> "System"
        AppThemeMode.LIGHT -> "Light"
        AppThemeMode.DARK -> "Dark"
    }
    var activeDialog by remember { mutableStateOf<PreferenceDialogType?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Top Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Preference Options List
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PreferenceItem(
                    label = "Language",
                    value = "${state.language} >",
                    onClick = { activeDialog = PreferenceDialogType.LANGUAGE }
                )

                PreferenceItem(
                    label = "Date format",
                    value = "${state.dateFormat} >",
                    onClick = { activeDialog = PreferenceDialogType.DATE_FORMAT }
                )

                PreferenceItem(
                    label = "Time format",
                    value = "${state.timeFormat} >",
                    onClick = { activeDialog = PreferenceDialogType.TIME_FORMAT }
                )

                PreferenceItem(
                    label = "Currency",
                    value = "$selectedCurrency >",
                    onClick = { activeDialog = PreferenceDialogType.CURRENCY }
                )

                PreferenceItem(
                    label = "Mode",
                    value = "$currentModeLabel >",
                    onClick = { activeDialog = PreferenceDialogType.MODE }
                )

                PreferenceItem(
                    label = "Notifications",
                    value = "${if (state.notificationsEnabled) "On" else "Off"} >",
                    onClick = { activeDialog = PreferenceDialogType.NOTIFICATIONS }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Log Out Button
            Button(
                onClick = {
                    onLogOut()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEE6C6D))
            ) {
                Text("Log out", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Selection Dialogs
        when (activeDialog) {
            PreferenceDialogType.LANGUAGE -> {
                PreferenceSelectionDialog(
                    title = "Select Language",
                    currentValue = state.language,
                    options = listOf("English", "Spanish", "German", "French", "Romanian"),
                    onSelect = { viewModel.updateLanguage(it) },
                    onDismiss = { activeDialog = null }
                )
            }
            PreferenceDialogType.DATE_FORMAT -> {
                PreferenceSelectionDialog(
                    title = "Select Date Format",
                    currentValue = state.dateFormat,
                    options = listOf("DD/MM/YYYY", "MM/DD/YYYY", "YYYY-MM-DD"),
                    onSelect = { viewModel.updateDateFormat(it) },
                    onDismiss = { activeDialog = null }
                )
            }
            PreferenceDialogType.TIME_FORMAT -> {
                PreferenceSelectionDialog(
                    title = "Select Time Format",
                    currentValue = state.timeFormat,
                    options = listOf("24-hour", "12-hour"),
                    onSelect = { viewModel.updateTimeFormat(it) },
                    onDismiss = { activeDialog = null }
                )
            }
            PreferenceDialogType.CURRENCY -> {
                PreferenceSelectionDialog(
                    title = "Select Currency",
                    currentValue = selectedCurrency,
                    options = listOf("USD", "EUR", "MDL"),
                    onSelect = { choice ->
                        viewModel.updateCurrency(choice)
                        CurrencyManager.setCurrency(context, choice)
                    },
                    onDismiss = { activeDialog = null }
                )
            }
            PreferenceDialogType.MODE -> {
                PreferenceSelectionDialog(
                    title = "Select Mode",
                    currentValue = currentModeLabel,
                    options = listOf("System", "Light", "Dark"),
                    onSelect = { modeChoice ->
                        val newMode = when (modeChoice) {
                            "Light" -> AppThemeMode.LIGHT
                            "Dark" -> AppThemeMode.DARK
                            else -> AppThemeMode.SYSTEM
                        }
                        ThemeManager.setThemeMode(context, newMode)
                    },
                    onDismiss = { activeDialog = null }
                )
            }
            PreferenceDialogType.NOTIFICATIONS -> {
                PreferenceSelectionDialog(
                    title = "Notifications",
                    currentValue = if (state.notificationsEnabled) "On" else "Off",
                    options = listOf("On", "Off"),
                    onSelect = { choice ->
                        val enabled = (choice == "On")
                        viewModel.toggleNotifications(context, enabled)
                    },
                    onDismiss = { activeDialog = null }
                )
            }
            null -> {}
        }
    }
}

@Composable
private fun PreferenceItem(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldHeight)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun <T> PreferenceSelectionDialog(
    title: String,
    currentValue: T,
    options: List<T>,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(option)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == currentValue,
                            onClick = {
                                onSelect(option)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = option.toString(), fontSize = 16.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}