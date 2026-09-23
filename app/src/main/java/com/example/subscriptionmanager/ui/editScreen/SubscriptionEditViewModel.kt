package com.example.subscriptionmanager.ui.editScreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.subscriptionmanager.ui.subscriptionList.initialSubscriptions

class SubscriptionEditViewModel : ViewModel() {
    var name by mutableStateOf("")
    var category by mutableStateOf("Entertainment")
    var price by mutableStateOf("")
    var billingPeriod by mutableStateOf("Monthly")
    var nextRenewalDate by mutableStateOf("01 Oct 2026")
    var notes by mutableStateOf("")

    fun loadSubscription(subscriptionName: String) {
        // Find the subscription from our top-level initialSubscriptions list
        initialSubscriptions.find { it.name == subscriptionName }?.let { sub ->
            name = sub.name
            category = sub.category
            price = sub.price
            billingPeriod = "Monthly"
            nextRenewalDate = "Oct 1, 2026"
        }
    }

    fun saveSubscription(subscriptionName: String) {
        // Save logic placeholder
    }
}