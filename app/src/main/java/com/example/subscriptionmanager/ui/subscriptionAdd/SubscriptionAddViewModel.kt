package com.example.subscriptionmanager.ui.subscriptionAdd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subscriptionmanager.data.entities.BillingPeriod
import com.example.subscriptionmanager.data.entities.Category
import com.example.subscriptionmanager.data.entities.CategoryEntity
import com.example.subscriptionmanager.data.entities.SubscriptionEntity
import com.example.subscriptionmanager.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone


class SubscriptionAddViewModel(
    private val subscriptionRepository: SubscriptionRepository
): ViewModel() {
    // TODO: transform data from form into db record
    private val _name = MutableStateFlow("Enter subscription name")
    val name: StateFlow<String> = _name

    private val _category = MutableStateFlow(Category.NONE)
    val category: StateFlow<Category> = _category

    private val _packageName = MutableStateFlow<String?>("")
    val packageName = _packageName
    private val _price = MutableStateFlow("")
    val price: StateFlow<String> = _price

    private val _billingPeriod = MutableStateFlow(BillingPeriod.MONTHLY)
    val billingPeriod: StateFlow<BillingPeriod> = _billingPeriod

    private val _nextRenewalDate = MutableStateFlow<Calendar?>(null)
    val nextRenewalDate: StateFlow<Calendar?> = _nextRenewalDate

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error

    fun updateName(value: String) {
        _name.value = value
    }

    fun updateCategory(value: Category) {
        _category.value = value
    }

    fun updatePackageName(value: String?) {
        _packageName.value = value
    }

    fun updatePrice(value: String) {
        _price.value = value
    }

    fun updateBillingPeriod(value: BillingPeriod) {
        _billingPeriod.value = value
    }

    fun updateRenewalDate(millis: Long) {
        val calendar = Calendar.getInstance().apply { timeInMillis = millis }
        _nextRenewalDate.value = calendar
    }

    fun save(
        onSuccess: () -> Unit
    ) {
        val trimmedName = _name.value.trim()
        val parsedPrice = _price.value.trim().toDoubleOrNull()
        val renewalDate = _nextRenewalDate.value

        when {
            trimmedName.isEmpty() -> _error.value = "Enter a subscription name"
            parsedPrice == null -> _error.value = "Enter a valid price"
            renewalDate == null -> _error.value = "Pick a renewal date"
            else -> {
                _error.value = ""
                viewModelScope.launch {
                    val subscriptionId = subscriptionRepository.insertSubscription(
                        SubscriptionEntity(
                            id = 0,
                            name = trimmedName,
                            packageName = _packageName.value,
                            price = parsedPrice,
                            billingPeriod = _billingPeriod.value.ordinal,
                            nextRenewalDate = renewalDate,
                            status = true,
                            startDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")),
                            score = 0
                        )
                    ).toInt()

                    subscriptionRepository.insertCategory(
                        CategoryEntity(
                            subscriptionId = subscriptionId,
                            category = _category.value.value
                        )
                    )
                    onSuccess()
                }
            }
        }
    }
}