package com.example.subscriptionmanager.ui.subscriptionAdd

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val categories = listOf("Entertainment", "Productivity", "Utilities", "Other")
private val billingPeriods = listOf("Monthly", "Weekly", "Quarterly", "Yearly")

@Composable
fun SubscriptionAddScreen(
    viewModel: SubscriptionAddViewModel = viewModel(),
    onSave: (
        name: String,
        price: Double,
        billingPeriod: String,
        nextRenewalDate: String?
    ) -> Unit,
    onCancel: () -> Unit,
) {
    val formState by viewModel.formState.collectAsState()
    var showDatePicker: Boolean by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add Subscription",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F1F1F)
            )
            Text(
                text = "Cancel",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.clickable { onCancel() }
            )
        }

        // Quick Add Presets
        Text(
            text = "QUICK ADD",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(
                "Netflix" to "9.99",
                "Spotify" to "9.99",
                "ChatGPT" to "20.00",
                "Other" to ""
            ).forEach { (presetName, presetPrice) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(Color(0xFFF2F2F7), RoundedCornerShape(12.dp))
                        .clickable {
                            if (presetName != "Other") {
                                viewModel.updateName(presetName)
                                viewModel.updateName(presetPrice)
                                viewModel.updateCategory("Entertainment")
                                viewModel.updateBillingPeriod("Monthly")
                            } else {
                                viewModel.updateName("")
                                viewModel.updatePrice("")
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = presetName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Form Fields
        FormFieldLabel("Subscription name")
        CustomTextField(
            value = formState.name,
            onValueChange = { viewModel.updateName(it) },
            placeholder = "Netflix"
        )

        FormFieldLabel("Category")
        CustomDropdownField(
            selected = formState.category,
            placeholder = "Entertainment",
            options = categories,
            onSelect = { viewModel.updateCategory(it)}
        )

        FormFieldLabel("Price")
        CustomTextField(
            value = formState.price,
            onValueChange = { viewModel.updatePrice(it) },
            placeholder = "9.99"
        )

        FormFieldLabel("Billing period")
        CustomDropdownField(
            selected = formState.billingPeriod,
            placeholder = "Monthly",
            options = billingPeriods,
            onSelect = { viewModel.updateBillingPeriod(it)}
        )

        FormFieldLabel("Next renewal date")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(Color(0xFFF7F7F8), RoundedCornerShape(12.dp))
                .clickable { showDatePicker = true }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = formState.nextRenewalDate.ifEmpty { "Select date" },
                color = if (formState.nextRenewalDate.isEmpty()) Color.LightGray else Color.Black,
                fontSize = 14.sp
            )
        }

        FormFieldLabel("Payment method")

        formState.error?.let {
            Text(it, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Save Button
        Button(
            onClick = { viewModel.save(onSave) }
        ) {
            Text("Save Subscription", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }

    // Calendar Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            formatter.timeZone = TimeZone.getTimeZone("UTC")
                            viewModel.updateRenewalDate(formatter.format(Date(selectedMillis)))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun FormFieldLabel(label: String) {
    Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier.padding(top = 2.dp)
    )
}

//TODO: change event handling behavior
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        placeholder = { Text(placeholder, color = Color.LightGray, fontSize = 14.sp) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF7F7F8),
            unfocusedContainerColor = Color(0xFFF7F7F8),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    )
}

@Composable
private fun CustomDropdownField(
    selected: String,
    placeholder: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(Color(0xFFF7F7F8), RoundedCornerShape(12.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selected.ifBlank { placeholder },
                fontSize = 14.sp,
                color = if (selected.isBlank()) Color.LightGray else Color.Black
            )
            Text(text = "▾", fontSize = 12.sp, color = Color.Gray)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}