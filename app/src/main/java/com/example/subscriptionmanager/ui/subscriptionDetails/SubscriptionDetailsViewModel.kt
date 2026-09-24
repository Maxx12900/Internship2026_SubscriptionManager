package com.example.subscriptionmanager.ui.subscriptionDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subscriptionmanager.data.entities.SubscriptionEntity
import com.example.subscriptionmanager.data.repository.SubscriptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubscriptionDetailsViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val _subscription = MutableStateFlow<SubscriptionEntity?>(null)
    val subscription: StateFlow<SubscriptionEntity?> = _subscription

    fun loadSubscription(subscriptionName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val intId = subscriptionName.toIntOrNull()
            val entity = if (intId != null) {
                repository.getSubscriptionById(intId)
            } else {
                repository.searchSubscriptions(subscriptionName).firstOrNull()?.firstOrNull { it.name == subscriptionName }
                    ?: repository.searchSubscriptions(subscriptionName).firstOrNull()?.firstOrNull()
            }
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