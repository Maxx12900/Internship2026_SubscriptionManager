package com.example.subscriptionmanager.ui.subscriptionlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
// Main function, draws the whole screen with list of subscriptions
fun SubscriptionListScreen(
    viewModel: SubscriptionListViewModel = viewModel()
) {
    val subscriptions by viewModel.subscriptions.collectAsState()
    Scaffold { innerPadding ->
        LazyColumn (
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(subscriptions) { (name, category, price, isActive) ->
                SubscriptionCard(name, category, price, isActive)
            }
        }
    }
}

@Composable
// Function that draws a box for a subscription in the list
fun SubscriptionCard(
    name : String,
    category : String,
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder, replace with image
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Black, RoundedCornerShape(12.dp))
            ) { }
            Spacer(modifier = Modifier.size(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = name)
                Text(text = category)
            }
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