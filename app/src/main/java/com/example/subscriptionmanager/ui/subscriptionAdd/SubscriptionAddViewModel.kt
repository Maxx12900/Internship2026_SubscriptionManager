package com.example.subscriptionmanager.ui.subscriptionAdd

import androidx.lifecycle.ViewModel
import com.example.subscriptionmanager.data.model.Subscription
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SubscriptionAddViewModel : ViewModel() {
    // TODO: transform data from form into db record
    private val _formState = MutableStateFlow(SubscriptionFormState())
    val formState: StateFlow<SubscriptionFormState> = _formState


    fun updateName(value: String) {
        _formState.value = _formState.value.copy(name = value, error = null)
    }

    fun updateCategory(value: String) {
        _formState.value = _formState.value.copy(category = value)
    }

    fun updatePrice(value: String) {
        _formState.value = _formState.value.copy(price = value, error = null)
    }

    fun updateBillingPeriod(value: String) {
        _formState.value = _formState.value.copy(billingPeriod = value)
    }

    fun updateRenewalDate(value: String) {
        _formState.value = _formState.value.copy(nextRenewalDate = value)
    }

    fun applyPreset(name: String, price: String, category: String, billingPeriod: String) {
        _formState.value = _formState.value.copy(
            name = name, price = price, category = category, billingPeriod = billingPeriod
        )
    }

    fun save(onSuccess: (name: String, price: Double, billingPeriod: String, nextRenewalDate: String?) -> Unit) {
        val state = _formState.value
        val trimmedName = state.name.trim()
        val parsedPrice = state.price.trim().toDoubleOrNull()

        when {
            trimmedName.isEmpty() -> _formState.value = state.copy(error = "Enter a subscription name")
            parsedPrice == null -> _formState.value = state.copy(error = "Enter a valid price")
            else -> onSuccess(
                trimmedName,
                parsedPrice,
                state.billingPeriod.ifBlank { "Monthly" },
                state.nextRenewalDate.trim().ifBlank { null }
            )
        }
    }
}

data class SubscriptionFormState(
    val name: String = "",
    val category: String = "",
    val price: String = "",
    val billingPeriod: String = "",
    val nextRenewalDate: String = "",
    val error: String? = null
)