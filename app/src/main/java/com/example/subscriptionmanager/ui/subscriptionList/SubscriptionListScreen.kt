package com.example.subscriptionmanager.ui.subscriptionList

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subscriptionmanager.SubscriptionManagerApplication
import com.example.subscriptionmanager.data.entities.Category
import com.example.subscriptionmanager.data.entities.SortBy
import com.example.subscriptionmanager.ui.common.AppIcon
import com.example.subscriptionmanager.ui.common.GenericViewModelFactory
import com.example.subscriptionmanager.ui.common.padding

@Composable
// Main function, draws the whole screen with list of subscriptions
fun SubscriptionListScreen(
    onAddClick: () -> Unit,
    onSubscriptionClick: (String) -> Unit
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


    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color(0xFFF2F2F7),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
            ) {
                Icon(Icons.Default.Add, contentDescription = "")
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
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { showFilterDialog = true }) { Text("Options") }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(padding)
            ) {
                items(subscriptions) { (name, packageName, price, isActive) ->
                    SubscriptionCard(name, packageName, price, isActive, onClick = {onSubscriptionClick(name)})
                }
            }
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
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(padding)

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(packageName, fallbackLetter = "${name.first()}")

            Spacer(modifier = Modifier.size(padding))

            Text(
                text = name,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold
            )
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(text = "$$price")
                Text(
                    if (isActive) "Active" else "Inactive"
                )
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
                        Text(option.name
                            .lowercase()
                            .replaceFirstChar { it.uppercase() }
                            .replace("_", " ")
                        )
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