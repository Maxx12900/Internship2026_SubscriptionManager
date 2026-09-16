package com.example.subscriptionmanager.ui.subscriptionlist

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Subscription(
    val name: String,
    val price: String
)

class SubscriptionListViewModel : ViewModel() {

    private val _subscriptions = MutableStateFlow(
        listOf(
            Subscription("Netflix", "15.99"),
            Subscription("Spotify", "9.99"),
            Subscription("Disney+", "7.99")
        )
    )
    val subscriptions: StateFlow<List<Subscription>> = _subscriptions
}