package com.example.subscriptionmanager.ui.subscriptionAdd

import android.app.Activity
import android.app.AppOpsManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subscriptionmanager.SubscriptionManagerApplication
import com.example.subscriptionmanager.data.analysis.getAppNameFromPackage
import com.example.subscriptionmanager.data.analysis.getPackageNameDisplayNameMap
import com.example.subscriptionmanager.data.analysis.getPackageNameList
import com.example.subscriptionmanager.data.analysis.isAppVisibleInMenu
import com.example.subscriptionmanager.data.analysis.requestPermissionForUsageDataAccess
import com.example.subscriptionmanager.data.entities.BillingPeriod
import com.example.subscriptionmanager.data.entities.Category
import com.example.subscriptionmanager.ui.common.GenericViewModelFactory
import com.example.subscriptionmanager.ui.common.fieldHeight
import com.example.subscriptionmanager.ui.common.padding
import com.example.subscriptionmanager.ui.subscriptionList.SubscriptionListViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionAddScreen(
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as SubscriptionManagerApplication).repository
    val viewModel: SubscriptionAddViewModel = viewModel(
        factory = GenericViewModelFactory { SubscriptionAddViewModel(repository) }
    )
    val appChoiceEnabled = requestPermissionForUsageDataAccess(context as Activity) == AppOpsManager.MODE_ALLOWED
    val appChoiceMap = getPackageNameDisplayNameMap(context)

    val name by viewModel.name.collectAsState()
    val category by viewModel.category.collectAsState()
    val price by viewModel.price.collectAsState()
    val billingPeriod by viewModel.billingPeriod.collectAsState()
    val nextRenewalDate by viewModel.nextRenewalDate.collectAsState()
    val error by viewModel.error.collectAsState()

    var appChoiceText by remember { mutableStateOf("") }
    var appChoiceExpanded by remember { mutableStateOf(false) }

    val appChoiceFilteredList: () -> Map<String, String> = {
        var count = 0
        appChoiceMap.filter {
            if (count > 5) {
                return@filter false
            }

            val include = it.value.contains(appChoiceText, ignoreCase = true) &&
                    isAppVisibleInMenu(context, it.key) &&
                    appChoiceText.length >= 2

            if (include) count += 1

            return@filter include
        }
    }

    var showDatePicker: Boolean by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Subscription",
                    style = typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)

                )
                Text(
                    text = "Cancel",
                    style = typography.titleSmall,
                    modifier = Modifier
                        .clickable { onCancel() }
                )
            }
            Text(
                text = "Subscription name",
                style = typography.bodySmall
            )
            CustomTextField(
                value = name,
                onValueChange = { viewModel.updateName(it) },
                placeholder = "Enter text here"
            )
            Text(
                text = "Category",
                style = typography.bodySmall,
                modifier = Modifier.padding()
            )
            CustomDropdownField(
                selected = category,
                placeholder = "Select category",
                options = Category.entries.filter { it != Category.NONE},
                toLabel = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                onSelect = { viewModel.updateCategory(it)}
            )
            Text(
                text = "Price",
                style = typography.bodySmall,
                modifier = Modifier.padding()
            )
            CustomTextField(
                value = price,
                onValueChange = { viewModel.updatePrice(it) },
                placeholder = "9.99"
            )
            Text(
                text = "Billing period",
                style = typography.bodySmall,
                modifier = Modifier.padding()
            )
            CustomDropdownField(
                selected = billingPeriod,
                placeholder = "Select period",
                options = BillingPeriod.entries,
                toLabel = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                onSelect = { viewModel.updateBillingPeriod(it) }
            )
            Text(
                text = "Next renewal date",
                style = typography.bodySmall,
                modifier = Modifier.padding()
            )
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
                    text = nextRenewalDate?.let { formatDate(it) } ?: "Select date",
                    color = if (nextRenewalDate == null) Color.LightGray else Color.Black,
                    fontSize = 14.sp
                )
            }
            Text(
                text = "Linked application",
                style = typography.bodySmall,
                modifier = Modifier.padding()
            )
            ExposedDropdownMenuBox(
                expanded = appChoiceExpanded && appChoiceEnabled,
                onExpandedChange = {
                    appChoiceExpanded = it && appChoiceEnabled
                }
            ) {
                TextField(
                    value = appChoiceText,
                    onValueChange = {
                        viewModel.updatePackageName(null)
                        appChoiceText = it
                        appChoiceExpanded = true && appChoiceEnabled
                    },
                    label = {
                        Text("Application name")
                    },
                    placeholder = {
                        Text("Enter name of the application")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryEditable),
                    singleLine = true,
                    enabled = appChoiceEnabled,
                    readOnly = true
                )
                ExposedDropdownMenu(
                    expanded = appChoiceExpanded,
                    onDismissRequest = {
                        appChoiceExpanded = false
                    }
                ) {
                    appChoiceFilteredList().forEach { item ->
                        DropdownMenuItem(
                            text = {
                                Text(item.value)
                            },
                            onClick = {
                                appChoiceText = item.value
                                viewModel.updatePackageName(item.key)
                                appChoiceExpanded = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            Text(
                text = error,
                color = Color.Red,
                style = typography.bodySmall
            )
            // Save Button
            Button(
                onClick = { viewModel.save(onSave) }
            ) {
                Text("Save Subscription", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { viewModel.updateRenewalDate(it) }
                            showDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

private fun formatDate(calendar: Calendar): String {
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val month = months[calendar.get(Calendar.MONTH)]
    val year = calendar.get(Calendar.YEAR)
    return "$day $month $year"
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
        placeholder = {
            Text(
                placeholder,
                style = typography.bodyMedium
            )
        },
        shape = RoundedCornerShape(padding),
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
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(padding)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldHeight)
                .padding(horizontal = padding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selected?.let(toLabel) ?: placeholder,
                fontSize = 14.sp,
                color = if (selected == null) Color.LightGray else Color.Black
            )
            Text(text = "▾", fontSize = 12.sp, color = Color.Gray)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(toLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}