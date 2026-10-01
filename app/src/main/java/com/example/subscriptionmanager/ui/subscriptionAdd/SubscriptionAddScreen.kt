package com.example.subscriptionmanager.ui.subscriptionAdd

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subscriptionmanager.SubscriptionManagerApplication
import com.example.subscriptionmanager.data.entities.BillingPeriod
import com.example.subscriptionmanager.data.entities.Category
import com.example.subscriptionmanager.ui.common.AppIcon
import com.example.subscriptionmanager.ui.common.GenericViewModelFactory
import com.example.subscriptionmanager.ui.common.fieldHeight
import com.example.subscriptionmanager.ui.common.padding
import com.example.subscriptionmanager.util.getInstalledApps

@Composable
fun SubscriptionAddScreen(
    subscriptionId: Int? = null,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as SubscriptionManagerApplication).repository
    val viewModel: SubscriptionAddViewModel = viewModel(
        factory = GenericViewModelFactory { SubscriptionAddViewModel(repository) }
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
    val screenTitle = if (isEditMode) "Edit Subscription" else "Add Subscription"
    val buttonText = if (isEditMode) "Save Changes" else "Save Subscription"

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
                    text = "Cancel",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onCancel() }
                )
            }

            formState.error?.let { err ->
                Text(
                    text = err,
                    color = Color.Red,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // App Field
            FieldLabel(label = "App", isRequired = false)

            var appSearchQuery by remember { mutableStateOf("") }
            var appDropdownExpanded by remember { mutableStateOf(false) }

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
                    placeholder = { Text("Select or type app name", style = MaterialTheme.typography.bodyMedium, color = Color.Gray) },
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
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    filteredAppSuggestions.forEach { (packageName, appName) ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = appName,
                                        fontSize = 14.sp,
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
                            }
                        )
                    }
                }
            }

            // Subscription name
            FieldLabel(label = "Subscription name", isRequired = true)
            CustomTextField(
                value = formState.name,
                onValueChange = { viewModel.updateName(it) },
                placeholder = "Enter text here"
            )

            // Category
            FieldLabel(label = "Category", isRequired = true)
            CustomDropdownField(
                selected = formState.category,
                placeholder = "Select category",
                options = Category.entries.filter { it != Category.NONE },
                toLabel = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                onSelect = { viewModel.updateCategory(it) }
            )

            // Price
            FieldLabel(label = "Price", isRequired = true)
            CustomTextField(
                value = formState.price,
                onValueChange = { viewModel.updatePrice(it) },
                placeholder = "9.99"
            )

            // Billing period
            FieldLabel(label = "Billing period", isRequired = true)
            CustomDropdownField(
                selected = formState.billingPeriod,
                placeholder = "Select period",
                options = BillingPeriod.entries,
                toLabel = { it.name.lowercase().replace('_', ' ').replaceFirstChar { c -> c.uppercase() } },
                onSelect = { viewModel.updateBillingPeriod(it) }
            )

            // Days before to remind
            FieldLabel(label = "Days before to remind", isRequired = false)
            CustomTextField(
                value = formState.daysBeforeToRemind?.toString() ?: "",
                onValueChange = { viewModel.updateDaysBeforeToRemind(it.toIntOrNull()) },
                placeholder = "3"
            )

            // Description
            FieldLabel(label = "Description", isRequired = false)
            CustomTextField(
                value = formState.description,
                onValueChange = { viewModel.updateDescription(it) },
                placeholder = "Optional description"
            )

            // Status Switch
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
                        text = if (formState.status) "Active" else "Inactive",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = formState.status,
                        onCheckedChange = { viewModel.updateStatus(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF635BFF)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.save(onSuccess = onSave) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF635BFF))
            ) {
                Text(
                    text = buttonText,
                    color = Color.White,
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
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (isRequired) {
            Text(text = "*", style = MaterialTheme.typography.bodySmall, color = Color(0xFFEE6C6D))
        }
    }
}

@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = Color.Gray) },
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(fieldHeight)
    )
}

@Composable
private fun <T> CustomDropdownField(
    selected: T?,
    placeholder: String,
    options: List<T>,
    toLabel: (T) -> String,
    toIcon: (@Composable (T) -> Unit)? = null,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayLabel = selected?.let(toLabel)?.ifBlank { null }

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
                color = if (displayLabel == null) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
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