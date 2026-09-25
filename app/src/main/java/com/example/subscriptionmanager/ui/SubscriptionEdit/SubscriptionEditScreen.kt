package com.example.subscriptionmanager.ui.subscriptionEdit

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subscriptionmanager.SubscriptionManagerApplication
import com.example.subscriptionmanager.ui.common.GenericViewModelFactory
import com.example.subscriptionmanager.ui.common.fieldHeight
import com.example.subscriptionmanager.ui.common.padding

private val categories = listOf("Entertainment", "Productivity", "Utilities", "Other")
private val billingPeriods = listOf("Monthly", "Weekly", "Quarterly", "Yearly")

@Composable
fun SubscriptionEditScreen(
    subscriptionName: String,
    onSaveSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as SubscriptionManagerApplication).repository
    val viewModel: SubscriptionEditViewModel = viewModel(
        factory = GenericViewModelFactory { SubscriptionEditViewModel(repository) }
    )

    val formState by viewModel.formState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var isSubmitted by remember { mutableStateOf(false) }

    LaunchedEffect(subscriptionName) {
        viewModel.loadSubscription(subscriptionName)
    }

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
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Subscription",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.clickable {
                        if (!isSubmitted) {
                            isSubmitted = true
                            onCancel()
                        }
                    }
                )
            }

            Text(text = "Subscription name", style = MaterialTheme.typography.bodySmall)
            CustomTextField(
                value = formState.name,
                onValueChange = { viewModel.updateName(it) },
                placeholder = "Enter text here"
            )

            Text(text = "Category", style = MaterialTheme.typography.bodySmall)
            CustomDropdownField(
                selected = formState.category,
                placeholder = "Select category",
                options = categories,
                onSelect = { viewModel.updateCategory(it) }
            )

            Text(text = "Price", style = MaterialTheme.typography.bodySmall)
            CustomTextField(
                value = formState.price,
                onValueChange = { viewModel.updatePrice(it) },
                placeholder = "9.99"
            )

            Text(text = "Billing period", style = MaterialTheme.typography.bodySmall)
            CustomDropdownField(
                selected = formState.billingPeriod,
                placeholder = "Select period",
                options = billingPeriods,
                onSelect = { viewModel.updateBillingPeriod(it) }
            )

            Text(text = "Next renewal date", style = MaterialTheme.typography.bodySmall)
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

            // ACTIVE / INACTIVE STATUS TOGGLE FIELD
            Text(text = "Subscription status", style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fieldHeight)
                    .background(Color(0xFFF7F7F8), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (formState.status) "Active" else "Inactive",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (formState.status) Color(0xFF4CAF50) else Color.Red
                )
                Switch(
                    checked = formState.status,
                    onCheckedChange = { viewModel.updateStatus(it) }
                )
            }

            Button(
                onClick = {
                    if (!isSubmitted) {
                        isSubmitted = true
                        viewModel.save(onSuccess = onSaveSuccess)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Changes", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
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
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
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