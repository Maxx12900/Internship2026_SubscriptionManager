package com.example.subscriptionmanager.ui.subscriptionlist

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Subscription(
    val name: String        = "placeholder",
    val categories: String  = "placeholder",
    val price: String       = "placeholder",
    val isActive : Boolean  = true
)

class SubscriptionListViewModel : ViewModel() {

    private val _subscriptions = MutableStateFlow(
        listOf(
            Subscription("Netflix", price = "15.99"),
            Subscription("Spotify", price = "9.99"),
            Subscription("Disney+", price = "7.99")
        )
    )
    val subscriptions: StateFlow<List<Subscription>> = _subscriptions
}