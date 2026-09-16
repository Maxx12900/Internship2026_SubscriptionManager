package com.example.subscriptionmanager.ui.subscriptionlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SubscriptionListScreen(
    viewModel: SubscriptionListViewModel = viewModel()
) {
    val subscriptions by viewModel.subscriptions.collectAsState()
    Scaffold { innerPadding ->
        LazyColumn (
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(subscriptions) { (name, price) ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column (modifier = Modifier.padding(16.dp)) {
                        Text(name, style = MaterialTheme.typography.titleMedium)
                        Text("$$price / month", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

}