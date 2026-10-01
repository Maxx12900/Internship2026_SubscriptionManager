package com.example.subscriptionmanager.ui.subscriptionDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subscriptionmanager.data.entities.SubscriptionEntity
import com.example.subscriptionmanager.data.repository.SubscriptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubscriptionDetailsViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val _subscription = MutableStateFlow<SubscriptionEntity?>(null)
    val subscription: StateFlow<SubscriptionEntity?> = _subscription

    fun loadSubscription(subscriptionId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = repository.getSubscriptionById(subscriptionId)
            _subscription.value = entity
        }
    }

    fun deleteSubscription(onSuccess: () -> Unit) {
        val currentSub = _subscription.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSubscription(currentSub)
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }
}