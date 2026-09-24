package com.example.subscriptionmanager.ui.subscriptionAdd

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme.typography
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subscriptionmanager.ui.common.fieldHeight
import com.example.subscriptionmanager.ui.common.padding

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
                style = typography.bodySmall,
                modifier = Modifier.padding()
            )
            CustomTextField(
                value = formState.name,
                onValueChange = { viewModel.updateName(it) },
                placeholder = "Enter text here"
            )
            Text(
                text = "Category",
                style = typography.bodySmall,
                modifier = Modifier.padding()
            )
            CustomDropdownField(
                selected = formState.category,
                placeholder = "Select category",
                options = categories,
                onSelect = { viewModel.updateCategory(it)}
            )
            Text(
                text = "Price",
                style = typography.bodySmall,
                modifier = Modifier.padding()
            )
            CustomTextField(
                value = formState.price,
                onValueChange = { viewModel.updatePrice(it) },
                placeholder = "9.99"
            )

            Text(
                text = "Billing period",
                style = typography.bodySmall,
                modifier = Modifier.padding()
            )
            CustomDropdownField(
                selected = formState.billingPeriod,
                placeholder = "Select period",
                options = billingPeriods,
                onSelect = { viewModel.updateBillingPeriod(it)}
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
                    text = formState.nextRenewalDate.ifEmpty { "Select date" },
                    color = if (formState.nextRenewalDate.isEmpty()) Color.LightGray else Color.Black,
                    fontSize = 14.sp
                )
            }

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
                .height(fieldHeight)
                .background(Color(0xFFF7F7F8), RoundedCornerShape(padding))
                .clickable { expanded = !expanded }
                .padding(horizontal = padding),
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