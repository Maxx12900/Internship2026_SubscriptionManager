package com.example.subscriptionmanager.ui.addSubscription

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val categories = listOf("Entertainment", "Productivity", "Utilities", "Other")
private val billingPeriods = listOf("Monthly", "Weekly", "Quarterly", "Yearly")
private val paymentMethods = listOf("Visa •••• 4821", "Mastercard •••• 1234", "Cash")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionScreen(
    onSave: (
        name: String,
        price: Double,
        billingPeriod: String,
        nextRenewalDate: String?
    ) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var billingPeriod by remember { mutableStateOf("") }
    var nextRenewalDate by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
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
                                name = presetName
                                price = presetPrice
                                category = "Entertainment"
                                billingPeriod = "Monthly"
                            } else {
                                name = ""
                                price = ""
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
            value = name,
            onValueChange = { name = it },
            placeholder = "Netflix"
        )

        FormFieldLabel("Category")
        CustomDropdownField(
            selected = category,
            placeholder = "Entertainment",
            options = categories,
            onSelect = { category = it }
        )

        FormFieldLabel("Price")
        CustomTextField(
            value = price,
            onValueChange = { price = it },
            placeholder = "9.99"
        )

        FormFieldLabel("Billing period")
        CustomDropdownField(
            selected = billingPeriod,
            placeholder = "Monthly",
            options = billingPeriods,
            onSelect = { billingPeriod = it }
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
                text = if (nextRenewalDate.isEmpty()) "Select date" else nextRenewalDate,
                color = if (nextRenewalDate.isEmpty()) Color.LightGray else Color.Black,
                fontSize = 14.sp
            )
        }

        FormFieldLabel("Payment method")
        CustomDropdownField(
            selected = paymentMethod,
            placeholder = "Visa •••• 4821",
            options = paymentMethods,
            onSelect = { paymentMethod = it }
        )

        error?.let {
            Text(it, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Save Button
        Button(
            onClick = {
                val trimmedName = name.trim()
                val parsedPrice = price.trim().toDoubleOrNull()
                val selectedPeriod = billingPeriod.ifBlank { "Monthly" }
                when {
                    trimmedName.isEmpty() -> error = "Enter a subscription name"
                    parsedPrice == null -> error = "Enter a valid price"
                    else -> onSave(
                        trimmedName,
                        parsedPrice,
                        selectedPeriod,
                        nextRenewalDate.trim().ifBlank { null }
                    )
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B5BF0)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
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
                            nextRenewalDate = formatter.format(Date(selectedMillis))
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
                text = if (selected.isBlank()) placeholder else selected,
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