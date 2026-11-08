package com.example.subscriptionmanager.ui.subscriptionDetails

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subscriptionmanager.data.entities.SubscriptionEntity
import com.example.subscriptionmanager.data.repository.SubscriptionRepository
import com.example.subscriptionmanager.notifications.cancelReminder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubscriptionDetailsViewModel(
    private val repository: SubscriptionRepository,
    private val appContext: Context
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
            // Cancel the pending reminder before the row (and its notification row) is deleted
            val reminder = repository.getNotificationsForSubscription(currentSub.id)
                .firstOrNull()?.firstOrNull()
            cancelReminder(appContext, currentSub.id)
            reminder?.scheduledNotificationId?.let { legacyId ->
                cancelReminder(appContext, currentSub.id, legacyId)
            }

            repository.deleteSubscription(currentSub)
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }
}