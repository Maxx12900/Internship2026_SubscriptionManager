package com.example.subscriptionmanager.ui.subscriptionList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subscriptionmanager.data.entities.Category
import com.example.subscriptionmanager.SubscriptionManagerApplication
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
    val selectedCategories by viewModel.selectedCategories.collectAsState()


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
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
            Text(
                text = "Subscription Manager",
                style = typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(padding)
            ) {
                items(
                    Category.entries
                ) { category ->
                    FilterChip(
                        selected = category in selectedCategories,
                        onClick = { viewModel.toggleCategory(category) },
                        label = { Text(category.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
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
        shape = RoundedCornerShape(12.dp)

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