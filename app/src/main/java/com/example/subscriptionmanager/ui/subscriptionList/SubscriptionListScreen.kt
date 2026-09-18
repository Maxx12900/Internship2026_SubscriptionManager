package com.example.subscriptionmanager.ui.subscriptionList

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subscriptionmanager.ui.components.AppIcon

val padding = 12.dp
@Composable
// Main function, draws the whole screen with list of subscriptions
fun SubscriptionListScreen(
    viewModel: SubscriptionListViewModel = viewModel()
) {
    val subscriptions by viewModel.subscriptions.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Text(
                text = "Subscription Manager",
                style = typography.headlineLarge,
                modifier = Modifier.padding(horizontal = padding)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = padding, vertical = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(padding)
            ) {
                items(
                    // placeholder
                    listOf("Entertainment", "Music")
                ) { category ->
                    FilterChip(
                        selected = category in selectedCategories,
                        onClick = { viewModel.toggleCategory(category) },
                        label = { Text(category) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(padding),
                verticalArrangement = Arrangement.spacedBy(padding)
            ) {
                items(subscriptions) { (name, packageName, _, price, isActive) ->
                    SubscriptionCard(name, packageName, price, isActive)
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
    price : String,
    isActive : Boolean
) {
    Card(
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