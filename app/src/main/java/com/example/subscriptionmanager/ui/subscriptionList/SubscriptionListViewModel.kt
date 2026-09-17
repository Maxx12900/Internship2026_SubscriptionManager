package com.example.subscriptionmanager.ui.subscriptionList

import androidx.lifecycle.ViewModel
import com.example.subscriptionmanager.data.Subscription
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SubscriptionListViewModel : ViewModel() {

    private val _subscriptions = MutableStateFlow(
        listOf(
            Subscription("Netflix", price = "15.99", packageName = "com.netflix.mediaclient"),
            Subscription("Spotify", price = "9.99",  packageName = "com.spotify.music"),
            Subscription("YouTube", price = "7.99",  packageName = "com.google.android.youtube")
        )
    )
    val subscriptions: StateFlow<List<Subscription>> = _subscriptions
}