package com.example.subscriptionmanager.ui.subscriptionDetails

import androidx.lifecycle.ViewModel
import com.example.subscriptionmanager.data.model.Subscription
import com.example.subscriptionmanager.ui.subscriptionList.initialSubscriptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SubscriptionDetailsViewModel : ViewModel() {
    private val _subscription = MutableStateFlow<Subscription?>(null)
    val subscription: StateFlow<Subscription?> = _subscription

    fun loadSubscription(name: String) {
        // Fetch from the top-level list
        _subscription.value = initialSubscriptions.find { it.name == name }
    }

    fun deleteSubscription(onSuccess: () -> Unit) {
        onSuccess()
    }
}