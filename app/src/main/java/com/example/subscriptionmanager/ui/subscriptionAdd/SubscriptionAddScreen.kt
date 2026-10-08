package com.example.subscriptionmanager.ui.subscriptionAdd

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subscriptionmanager.R
import com.example.subscriptionmanager.SubscriptionManagerApplication
import com.example.subscriptionmanager.data.entities.BillingPeriod
import com.example.subscriptionmanager.data.entities.Category
import com.example.subscriptionmanager.ui.common.AppIcon
import com.example.subscriptionmanager.ui.common.GenericViewModelFactory
import com.example.subscriptionmanager.ui.common.fieldHeight
import com.example.subscriptionmanager.ui.common.padding
import com.example.subscriptionmanager.util.getInstalledApps
import android.app.DatePickerDialog
import androidx.compose.foundation.layout.FlexDirection.Companion.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.subscriptionmanager.util.DateTimeManager
import com.example.subscriptionmanager.notifications.areNotificationsAllowed
import com.example.subscriptionmanager.notifications.areNotificationsEnabled
import com.example.subscriptionmanager.notifications.requestNotificationPermission
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun SubscriptionAddScreen(
    subscriptionId: Int? = null,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val application = context.applicationContext as SubscriptionManagerApplication
    val viewModel: SubscriptionAddViewModel = viewModel(
        factory = GenericViewModelFactory { SubscriptionAddViewModel(application) }
    )
    val formState by viewModel.formState.collectAsState()

    val installedApps = remember(context, formState.packageName, formState.name) {
        val systemApps = getInstalledApps(context)
        val currentPkg = formState.packageName

        val appsList = mutableListOf<Pair<String?, String>>()
        appsList.add(null to "None")

        if (!currentPkg.isNullOrBlank() && systemApps.none { it.first == currentPkg }) {
            val fallbackLabel = formState.name.ifBlank { currentPkg }
            appsList.add(currentPkg to fallbackLabel)
        }

        appsList.addAll(systemApps)
        appsList
    }

    LaunchedEffect(subscriptionId) {
        if (subscriptionId != null) {
            viewModel.loadSubscription(subscriptionId)
        }
    }

    val isEditMode = subscriptionId != null
    val screenTitle = if (isEditMode) stringResource(R.string.edit_subscription) else stringResource(R.string.add_subscription)
    val buttonText = if (isEditMode) stringResource(R.string.save_changes) else stringResource(R.string.save_subscription)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = screenTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.cancel),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { onCancel() }
                )
            }

            formState.error?.let { err ->
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // App Field
            FieldLabel(label = stringResource(R.string.app_label), isRequired = false)

            var appSearchQuery by remember { mutableStateOf("") }
            var appDropdownExpanded by remember { mutableStateOf(false) }

            // Sync initial value for Edit Mode
            LaunchedEffect(formState.id) {
                if (formState.id > 0 && formState.name.isNotBlank()) {
                    appSearchQuery = formState.name
                }
            }

            val filteredAppSuggestions = remember(appSearchQuery, installedApps) {
                if (appSearchQuery.isBlank()) {
                    installedApps
                } else {
                    installedApps.filter { (_, appName) ->
                        appName.contains(appSearchQuery, ignoreCase = true)
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = appSearchQuery,
                    onValueChange = { input ->
                        appSearchQuery = input
                        appDropdownExpanded = true
                        viewModel.updatePackageName(null)
                        viewModel.updateName(input)
                    },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.placeholder_select_app),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    // Dropdown menu and App Icon
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { appDropdownExpanded = !appDropdownExpanded }
                                .padding(end = 12.dp)
                        ) {
                            val currentPkg = formState.packageName
                            if (!currentPkg.isNullOrBlank()) {
                                AppIcon(
                                    packageName = currentPkg,
                                    fallbackLetter = appSearchQuery.take(1).ifBlank { "?" },
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(text = "▾", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(fieldHeight)
                )

                DropdownMenu(
                    expanded = appDropdownExpanded && filteredAppSuggestions.isNotEmpty(),
                    onDismissRequest = { appDropdownExpanded = false },
                    properties = PopupProperties(focusable = false),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .heightIn(max = 280.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                ) {
                    filteredAppSuggestions.forEachIndexed { index, (packageName, appName) ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        AppIcon(
                                            packageName = packageName,
                                            fallbackLetter = appName.take(1),
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Text(
                                        text = appName,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (packageName != null) {
                                        Spacer(modifier = Modifier.width(12.dp))
                                        AppIcon(
                                            packageName = packageName,
                                            fallbackLetter = appName.take(1).ifBlank { "?" },
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                appSearchQuery = appName
                                viewModel.updatePackageName(packageName)
                                viewModel.updateName(appName)
                                appDropdownExpanded = false
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                        )
                        if (index != filteredAppSuggestions.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color(0xFFF0F0F0)
                            )
                        }
                    }
                }
            }

            // Subscription name
            FieldLabel(label = stringResource(R.string.subscription_name), isRequired = true)
            CustomTextField(
                value = formState.name,
                onValueChange = { viewModel.updateName(it) },
                placeholder = stringResource(R.string.placeholder_enter_text)
            )

            // Category
            FieldLabel(label = stringResource(R.string.category), isRequired = true)
            CustomDropdownField(
                selected = formState.category,
                placeholder = stringResource(R.string.placeholder_select_category),
                options = Category.entries.filter { it != Category.NONE },
                toLabel = { it.toTranslatedString() },
                onSelect = { viewModel.updateCategory(it) }
            )

            // Price
            FieldLabel(label = stringResource(R.string.price), isRequired = true)
            CustomTextField(
                value = formState.price,
                onValueChange = { input ->
                    val filtered = input.filterIndexed { index, char ->
                        char.isDigit() || (char == '.' && input.indexOf('.') == index)
                    }
                    viewModel.updatePrice(filtered)
                },
                placeholder = "9.99",
                keyboardType = KeyboardType.Decimal
            )

            // Billing period
            FieldLabel(label = stringResource(R.string.billing_period), isRequired = true)
            CustomDropdownField(
                selected = formState.billingPeriod,
                placeholder = stringResource(R.string.placeholder_select_period),
                options = BillingPeriod.entries,
                toLabel = { it.toTranslatedString() },
                onSelect = { viewModel.updateBillingPeriod(it) }
            )

            // Start Date
            FieldLabel(label = stringResource(R.string.start_date), isRequired = true)
            var showDatePicker by remember { mutableStateOf(false) }
            val dateFormatPattern by DateTimeManager.dateFormatPattern.collectAsState()
            val startDateString = remember(formState.startDate, dateFormatPattern) {
                DateTimeManager.formatDate(formState.startDate, dateFormatPattern)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fieldHeight)
                    .background(Color(0xFFF7F7F8), RoundedCornerShape(12.dp))
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = startDateString,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
            if (showDatePicker) {
                val cal = formState.startDate
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val selectedCalendar = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        }
                        viewModel.updateStartDate(selectedCalendar)
                        showDatePicker = false
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).apply {
                    setOnDismissListener { showDatePicker = false }
                }.show()
            }

            Row {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FieldLabel(label = stringResource(R.string.remind_me), isRequired = false)
                    Switch(
                        checked = formState.shouldRemind,
                        onCheckedChange = { state ->
                            if (areNotificationsAllowed(context) && areNotificationsEnabled(context)) {
                                viewModel.updateShouldRemind(state)
                            }
                            if (!areNotificationsAllowed(context)) {
                                if (context is Activity && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    requestNotificationPermission(context)
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.padding(12.dp))

                // Days before to remind
                Column(
                    modifier = Modifier.weight(0.8f)
                ) {
                    FieldLabel(label = stringResource(R.string.days_before_remind), isRequired = false)
                    CustomTextField(
                        value = formState.daysBeforeToRemind?.toString() ?: "",
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }
                            viewModel.updateDaysBeforeToRemind(filtered.toIntOrNull())
                        },
                        placeholder = "3",
                        keyboardType = KeyboardType.Number,
                        enabled = formState.shouldRemind
                    )
                }
            }

            // Description
            FieldLabel(label = stringResource(R.string.description), isRequired = false)
            CustomTextField(
                value = formState.description,
                onValueChange = { viewModel.updateDescription(it) },
                placeholder = stringResource(R.string.placeholder_description)
            )

            // Active/Inactive Status Switch (In edit mode)
            if (isEditMode) {
                FieldLabel(label = "Active/Inactive", isRequired = false)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(fieldHeight)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (formState.status) stringResource(R.string.active) else stringResource(R.string.inactive),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = formState.status,
                        onCheckedChange = { viewModel.updateStatus(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            formState.error?.let { err ->
                Text(
                    text = err,
                    color = Color.Red,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Button(
                onClick = { viewModel.save(onSuccess = onSave) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = buttonText,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FieldLabel(label: String, isRequired: Boolean) {
    Row {
        //Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (isRequired) {
            Text(text = "*", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = Color.LightGray) },
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(fieldHeight),
        enabled = enabled
    )
}

@Composable
private fun <T> CustomDropdownField(
    selected: T?,
    placeholder: String,
    options: List<T>,
    toLabel: @Composable (T) -> String,
    toIcon: (@Composable (T) -> Unit)? = null,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayLabel = selected?.let { toLabel(it) }?.ifBlank { null }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldHeight)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = displayLabel ?: placeholder,
                fontSize = 14.sp,
                color = if (displayLabel == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (selected != null && toIcon != null) {
                    toIcon(selected)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = "▾", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = toLabel(option),
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            toIcon?.let { icon ->
                                Spacer(modifier = Modifier.width(12.dp))
                                icon(option)
                            }
                        }
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun BillingPeriod.toTranslatedString(): String {
    return when (this) {
        BillingPeriod.WEEKLY -> stringResource(R.string.weekly)
        BillingPeriod.MONTHLY -> stringResource(R.string.monthly)
        BillingPeriod.THREE_MONTHS -> stringResource(R.string.three_months)
        BillingPeriod.SIX_MONTHS -> stringResource(R.string.six_months)
        BillingPeriod.YEARLY -> stringResource(R.string.yearly)
    }
}

@Composable
private fun Category.toTranslatedString(): String {
    return when (this) {
        Category.STREAMING -> stringResource(R.string.category_streaming)
        Category.PRODUCTIVITY -> stringResource(R.string.category_productivity)
        Category.GAMES -> stringResource(R.string.category_games)
        Category.FOOD -> stringResource(R.string.category_food)
        else -> ""
    }
}