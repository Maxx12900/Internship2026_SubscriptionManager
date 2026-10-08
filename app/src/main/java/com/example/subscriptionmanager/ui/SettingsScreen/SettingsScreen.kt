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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subscriptionmanager.R
import com.example.subscriptionmanager.ui.common.fieldHeight
import com.example.subscriptionmanager.ui.common.padding
import com.example.subscriptionmanager.util.AppThemeMode
import com.example.subscriptionmanager.util.CurrencyManager
import com.example.subscriptionmanager.util.DateTimeManager
import com.example.subscriptionmanager.util.ThemeManager
import java.util.Locale

private enum class PreferenceDialogType {
    LANGUAGE, DATE_FORMAT, CURRENCY, MODE, NOTIFICATIONS
}

fun changeAppLanguage(context: Context, languageName: String) {
    val languageCode = when (languageName) {
        "Romanian" -> "ro"
        "Russian" -> "ru"
        else -> "en"
    }
    val locale = Locale(languageCode)
    Locale.setDefault(locale)

    val resources = context.resources
    val config = resources.configuration
    config.setLocale(locale)
    resources.updateConfiguration(config, resources.displayMetrics)

    context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        .edit()
        .putString("selected_language", languageName)
        .apply()
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

    val currentDateLabel = when (state.dateFormat) {
        "MM/DD/YYYY" -> stringResource(R.string.date_format_mmddyyyy)
        "YYYY-MM-DD" -> stringResource(R.string.date_format_iso)
        else -> stringResource(R.string.date_format_ddmmyyyy)
    }

    val currentTimeLabel = if (state.timeFormat == "12-hour") {
        stringResource(R.string.time_12_hour)
    } else {
        stringResource(R.string.time_24_hour)
    }

    val themeMode by ThemeManager.themeMode.collectAsState()
    val currentModeLabel = when (themeMode) {
        AppThemeMode.SYSTEM -> stringResource(R.string.system_mode)
        AppThemeMode.LIGHT -> stringResource(R.string.light_mode)
        AppThemeMode.DARK -> stringResource(R.string.dark_mode)
    }

    val currentLanguageLabel = when (state.language) {
        "Romanian" -> stringResource(R.string.lang_romanian)
        "Russian" -> stringResource(R.string.lang_russian)
        else -> stringResource(R.string.lang_english)
    }

    val notificationStatusLabel = if (state.notificationsEnabled) {
        stringResource(R.string.on)
    } else {
        stringResource(R.string.off)
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
                    text = stringResource(R.string.settings),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // User Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = state.username.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Column {
                        Text(
                            text = state.username,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Preferences Section Title
            Text(
                text = stringResource(R.string.preferences),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Preference Options List
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PreferenceItem(
                    label = stringResource(R.string.language),
                    value = "$currentLanguageLabel >",
                    onClick = { activeDialog = PreferenceDialogType.LANGUAGE }
                )

                PreferenceItem(
                    label = stringResource(R.string.date_format),
                    value = "$currentDateLabel >",
                    onClick = { activeDialog = PreferenceDialogType.DATE_FORMAT }
                )


                PreferenceItem(
                    label = stringResource(R.string.currency),
                    value = "$selectedCurrency >",
                    onClick = { activeDialog = PreferenceDialogType.CURRENCY }
                )

                PreferenceItem(
                    label = stringResource(R.string.mode),
                    value = "$currentModeLabel >",
                    onClick = { activeDialog = PreferenceDialogType.MODE }
                )

                PreferenceItem(
                    label = stringResource(R.string.notifications),
                    value = "$notificationStatusLabel >",
                    onClick = { activeDialog = PreferenceDialogType.NOTIFICATIONS }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Log Out Button
            Button(
                onClick = {
                    context.getSharedPreferences("active_user", Context.MODE_PRIVATE)
                        .edit()
                        .clear()
                        .apply()
                    onLogOut()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEE6C6D))
            ) {
                Text(
                    text = stringResource(R.string.log_out),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Selection Dialogs
        when (activeDialog) {
            PreferenceDialogType.LANGUAGE -> {
                val languageOptions = listOf(
                    "English" to stringResource(R.string.lang_english),
                    "Romanian" to stringResource(R.string.lang_romanian),
                    "Russian" to stringResource(R.string.lang_russian)
                )
                PreferenceSelectionDialog(
                    title = stringResource(R.string.select_language),
                    currentValue = state.language,
                    optionsWithLabels = languageOptions,
                    onSelect = { selectedLanguage ->
                        viewModel.updateLanguage(selectedLanguage)
                        changeAppLanguage(context, selectedLanguage)
                    },
                    onDismiss = { activeDialog = null }
                )
            }
            PreferenceDialogType.DATE_FORMAT -> {
                val dateOptions = listOf(
                    "DD/MM/YYYY" to stringResource(R.string.date_format_ddmmyyyy),
                    "MM/DD/YYYY" to stringResource(R.string.date_format_mmddyyyy),
                    "YYYY-MM-DD" to stringResource(R.string.date_format_iso)
                )
                PreferenceSelectionDialog(
                    title = stringResource(R.string.select_date_format),
                    currentValue = when (state.dateFormat) {
                        "MM/DD/YYYY" -> stringResource(R.string.date_format_mmddyyyy)
                        "YYYY-MM-DD" -> stringResource(R.string.date_format_iso)
                        else -> stringResource(R.string.date_format_ddmmyyyy)
                    },
                    optionsWithLabels = dateOptions,
                    onSelect = { choice ->
                        viewModel.updateDateFormat(choice)
                        DateTimeManager.setDateFormat(context, choice)
                        activeDialog = null
                    },
                    onDismiss = { activeDialog = null }
                )
            }

            PreferenceDialogType.CURRENCY -> {
                PreferenceSelectionDialog(
                    title = stringResource(R.string.select_currency),
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
                val modeOptions = listOf(
                    "System" to stringResource(R.string.system_mode),
                    "Light" to stringResource(R.string.light_mode),
                    "Dark" to stringResource(R.string.dark_mode)
                )
                PreferenceSelectionDialog(
                    title = stringResource(R.string.select_mode),
                    currentValue = currentModeLabel,
                    optionsWithLabels = modeOptions,
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
                val notifOptions = listOf(
                    "On" to stringResource(R.string.on),
                    "Off" to stringResource(R.string.off)
                )
                PreferenceSelectionDialog(
                    title = stringResource(R.string.notifications),
                    currentValue = if (state.notificationsEnabled) "On" else "Off",
                    optionsWithLabels = notifOptions,
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
private fun PreferenceSelectionDialog(
    title: String,
    currentValue: String,
    options: List<String> = emptyList(),
    optionsWithLabels: List<Pair<String, String>>? = null,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val items = optionsWithLabels ?: options.map { it to it }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { (rawKey, displayLabel) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(rawKey)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = rawKey == currentValue || displayLabel == currentValue,
                            onClick = {
                                onSelect(rawKey)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = displayLabel, fontSize = 16.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}