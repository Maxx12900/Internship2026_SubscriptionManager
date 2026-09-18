package com.example.subscriptionmanager.ui.subscriptionList

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.subscriptionmanager.data.model.Subscription

class SubscriptionListViewModel : ViewModel() {

    private val _subscriptions = MutableStateFlow(
        listOf(
            Subscription("Netflix", price = "15.99", packageName = "com.netflix.mediaclient"),
            Subscription("Spotify", price = "9.99",  packageName = "com.spotify.music"),
            Subscription("YouTube", price = "7.99",  packageName = "com.google.android.youtube")
        )
    )
    val subscriptions: StateFlow<List<Subscription>> = _subscriptions.asStateFlow()

    fun addSubscription(
        name: String,
        price: Double,
        billingPeriod: String,
        nextRenewalDate: String?
    ) {
        val newItem = Subscription(
            name = name,
            price = price.toString(),
            packageName = null
        )
        _subscriptions.value = listOf(newItem) + _subscriptions.value
    }
}