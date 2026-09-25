package com.example.subscriptionmanager.ui.subscriptionEdit

import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subscriptionmanager.data.entities.BillingPeriod
import com.example.subscriptionmanager.data.entities.SubscriptionEntity
import com.example.subscriptionmanager.data.repository.SubscriptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class SubscriptionEditFormState(
    val id: Int = 0,
    val name: String = "",
    val category: String = "",
    val price: String = "",
    val billingPeriod: String = "",
    val nextRenewalDate: String = "",
    val status: Boolean = true, // Active status field
    val error: String? = null
)

class SubscriptionEditViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(SubscriptionEditFormState())
    val formState: StateFlow<SubscriptionEditFormState> = _formState

    private var originalEntity: SubscriptionEntity? = null

    fun loadSubscription(subscriptionName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val intId = subscriptionName.toIntOrNull()
            val entity = if (intId != null) {
                repository.getSubscriptionById(intId)
            } else {
                repository.searchSubscriptions(subscriptionName).firstOrNull()?.firstOrNull { it.name == subscriptionName }
                    ?: repository.searchSubscriptions(subscriptionName).firstOrNull()?.firstOrNull()
            }

            if (entity != null) {
                originalEntity = entity
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val billingPeriodName = BillingPeriod.entries.getOrNull(entity.billingPeriod)?.name
                    ?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Monthly"

                _formState.value = SubscriptionEditFormState(
                    id = entity.id,
                    name = entity.name,
                    price = entity.price.toString(),
                    billingPeriod = billingPeriodName,
                    nextRenewalDate = dateFormat.format(entity.nextRenewalDate.time),
                    status = entity.status // Loads existing status
                )
            }
        }
    }

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

    fun updateRenewalDate(millis: Long) {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        _formState.value = _formState.value.copy(nextRenewalDate = formatter.format(Date(millis)))
    }

    // Toggle Active / Inactive status
    fun updateStatus(value: Boolean) {
        _formState.value = _formState.value.copy(status = value)
    }

    fun save(onSuccess: () -> Unit) {
        val state = _formState.value
        val trimmedName = state.name.trim()
        val parsedPrice = state.price.trim().toDoubleOrNull()

        when {
            trimmedName.isEmpty() -> _formState.value = state.copy(error = "Enter a subscription name")
            parsedPrice == null -> _formState.value = state.copy(error = "Enter a valid price")
            else -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val original = originalEntity

                    val selectedBillingPeriodEnum = BillingPeriod.entries.find {
                        it.name.equals(state.billingPeriod, ignoreCase = true)
                    } ?: BillingPeriod.MONTHLY

                    val updatedEntity = SubscriptionEntity(
                        id = state.id,
                        name = trimmedName,
                        packageName = original?.packageName,
                        price = parsedPrice,
                        billingPeriod = selectedBillingPeriodEnum.ordinal,
                        nextRenewalDate = original?.nextRenewalDate ?: Calendar.getInstance().apply { add(Calendar.MONTH, 1) },
                        status = state.status, // Saves updated active status
                        startDate = original?.startDate ?: Calendar.getInstance(),
                        score = original?.score ?: 0
                    )

                    repository.updateSubscription(updatedEntity)

                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                }
            }
        }
    }
}