package com.example.subscriptionmanager.ui.subscriptionList

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subscriptionmanager.SubscriptionManagerApplication
import com.example.subscriptionmanager.data.entities.Category
import com.example.subscriptionmanager.data.entities.SortBy
import com.example.subscriptionmanager.ui.common.AppIcon
import com.example.subscriptionmanager.ui.common.GenericViewModelFactory
import com.example.subscriptionmanager.ui.common.padding
import com.example.subscriptionmanager.util.CurrencyManager

import com.example.subscriptionmanager.util.BankStatementParser
import com.example.subscriptionmanager.util.DetectedSubscription
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.subscriptionmanager.util.PendingStatementImport
@Composable
// Main function, draws the whole screen with list of subscriptions
fun SubscriptionListScreen(
    onAddClick: () -> Unit,
    onSubscriptionClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as SubscriptionManagerApplication).repository
    val viewModel: SubscriptionListViewModel = viewModel(
        factory = GenericViewModelFactory { SubscriptionListViewModel(repository) }
    )
    val subscriptions by viewModel.subscriptions.collectAsState()

    val draftCategories by viewModel.draftCategories.collectAsState()
    val draftSort by viewModel.draftSort.collectAsState()
    val draftAsc by viewModel.draftAsc.collectAsState()

    var showFilterDialog by remember { mutableStateOf(false) }
    var showAddOptionsDialog by remember { mutableStateOf(false) }
    var detectedSubscriptions by remember { mutableStateOf<List<DetectedSubscription>?>(null) }

    // Launcher for File Picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val isPdf = uri.toString().endsWith(".pdf", ignoreCase = true) ||
                        context.contentResolver.getType(uri)?.contains("pdf", ignoreCase = true) == true

                val results = if (isPdf) {
                    BankStatementParser.parsePdfStream(inputStream)
                } else {
                    BankStatementParser.parseCsvStream(inputStream)
                }
                detectedSubscriptions = results
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
        floatingActionButton = {
            // Tapping '+' opens Add Method Choice Dialog
            FloatingActionButton(
                onClick = { showAddOptionsDialog = true },
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(padding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Subscription List",
                    style = typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )

                TextButton(onClick = { showFilterDialog = true }) {
                    Text("Options")
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(padding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(subscriptions, key = { it.id }) { (id, name, packageName, price, isActive) ->
                    SubscriptionCard(
                        name = name,
                        packageName = packageName,
                        price = price,
                        isActive = isActive,
                        onClick = { onSubscriptionClick(id) }
                    )
                }
            }
        }

        // Add Option Choice Dialog (Manually vs Bank Statement)
        if (showAddOptionsDialog) {
            AlertDialog(
                onDismissRequest = { showAddOptionsDialog = false },
                title = { Text("Add Subscription", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Option 1: Manually
                        Card(
                            onClick = {
                                showAddOptionsDialog = false
                                onAddClick()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Add Manually", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("➔", color = Color.Gray)
                            }
                        }

                        // Option 2: Bank Statement
                        Card(
                            onClick = {
                                showAddOptionsDialog = false
                                filePickerLauncher.launch("*/*")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Import from Bank Statement", fontWeight = FontWeight.Bold)
                                    Text("Scan CSV or PDF file", fontSize = 12.sp, color = Color.Gray)
                                }
                                Text("➔", color = Color.Gray)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAddOptionsDialog = false }) { Text("Cancel") }
                }
            )
        }

        if (showFilterDialog) {
            FilterSortDialog(
                selectedCategories = draftCategories,
                sortOption = draftSort,
                isAscending = draftAsc,
                onToggleCategory = viewModel::toggleCategory,
                onSortSelected = viewModel::setSortingOption,
                onAscendingChange = viewModel::setAscending,
                onReset = viewModel::resetDialog,
                onApply = {
                    viewModel.applyDialog()
                    showFilterDialog = false
                },
                onDismiss =  { showFilterDialog = false }
            )
        }
        // Re-opens dialog if returning from inspecting an item
        LaunchedEffect(PendingStatementImport.activeList) {
            detectedSubscriptions = PendingStatementImport.activeList
        }


        // Show Detected Subscriptions Dialog
        detectedSubscriptions?.let { items ->
            DetectedSubscriptionsDialog(
                detectedItems = items,
                onInspectItem = { item ->
                    // Preserve active list and store item to inspect
                    PendingStatementImport.activeList = items
                    PendingStatementImport.item = item
                    onAddClick()
                },
                onConfirmImport = { selectedItems ->
                    viewModel.importDetectedSubscriptions(selectedItems)
                    detectedSubscriptions = null
                    PendingStatementImport.activeList = null
                },
                onDismiss = {
                    detectedSubscriptions = null
                    PendingStatementImport.activeList = null
                }
            )
        }
    }
}

@Composable
// Function that draws a box for a subscription in the list
fun SubscriptionCard(
    name : String,
    packageName : String?,
    price : Double,
    isActive : Boolean,
    onClick:() -> Unit
) {
    val currentCurrency by CurrencyManager.currency.collectAsState()
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(padding)

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(packageName, fallbackLetter = if (name.isBlank()) "?" else "${name.first()}")
//            AppIcon(
//                packageName = packageName,
//                fallbackLetter = if (name.isBlank()) "?" else "${name.first()}"
//            )

            Spacer(modifier = Modifier.size(padding))

            Text(
                text = name,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold
            )
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(text = CurrencyManager.formatPrice(price, currentCurrency))
                Text(
                    if (isActive) "Active" else "Inactive"
                )
//            Column(horizontalAlignment = Alignment.End) {
//                Text(text = "$$price")
//                Text(if (isActive) "Active" else "Inactive")
            }
        }
    }
}

@Composable
// Creates a dialog window
private fun FilterSortDialog(
    selectedCategories: Set<Category>,
    sortOption: SortBy,
    isAscending: Boolean,
    onToggleCategory: (Category) -> Unit,
    onSortSelected: (SortBy) -> Unit,
    onAscendingChange: (Boolean) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Options") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Categories", fontWeight = FontWeight.Bold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Category.entries.filter {
                        it != Category.NONE
                    }.forEach { category ->
                        FilterChip(
                            selected = category in selectedCategories,
                            onClick = { onToggleCategory(category) },
                            label = { Text(category.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }

                Text("Sort by", fontWeight = FontWeight.Bold)
                SortBy.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSortSelected(option) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = option == sortOption, onClick = { onSortSelected(option) })
                        Text(option.name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " "))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (isAscending) "Ascending" else "Descending")
                    Switch(checked = isAscending, onCheckedChange = onAscendingChange)
                }
            }
        },
        confirmButton = { TextButton(onClick = onApply) { Text("Done") } },
        dismissButton = { TextButton(onClick = onReset) { Text("Reset") } }
    )
}

@Composable
private fun DetectedSubscriptionsDialog(
    detectedItems: List<DetectedSubscription>,
    onInspectItem: (DetectedSubscription) -> Unit, // <--- TAP TO OPEN FULL EDIT SCREEN
    onConfirmImport: (List<DetectedSubscription>) -> Unit,
    onDismiss: () -> Unit
) {
    val items = remember(detectedItems) { mutableStateListOf(*detectedItems.toTypedArray()) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Found ${items.size} Subscriptions", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tap any subscription to open full Edit screen:")
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onInspectItem(item)
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.selected,
                            onCheckedChange = { isChecked ->
                                items[index] = item.copy(selected = isChecked)
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.name, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Paid: ${dateFormat.format(item.paymentDate.time)}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Text(text = "$${item.price}", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Text("➔", color = Color.Gray)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val selected = items.filter { it.selected }
                    onConfirmImport(selected)
                    onDismiss()
                }
            ) {
                Text("Import (${items.count { it.selected }})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}