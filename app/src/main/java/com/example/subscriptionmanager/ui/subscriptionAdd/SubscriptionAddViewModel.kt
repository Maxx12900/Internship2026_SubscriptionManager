package com.example.subscriptionmanager.ui.subscriptionAdd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subscriptionmanager.data.entities.BillingPeriod
import com.example.subscriptionmanager.data.entities.Category
import com.example.subscriptionmanager.data.entities.CategoryEntity
import com.example.subscriptionmanager.data.entities.NotificationEntity
import com.example.subscriptionmanager.data.entities.SubscriptionEntity
import com.example.subscriptionmanager.data.repository.SubscriptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

data class SubscriptionFormState(
    val id: Int = 0,
    val name: String = "",
    val category: Category = Category.NONE,
    val packageName: String? = null,
    val price: String = "",
    val billingPeriod: BillingPeriod = BillingPeriod.MONTHLY,
    val daysBeforeToRemind: Int? = 3,
    val description: String = "",
    val status: Boolean = true,
    val error: String? = null
)

class SubscriptionAddViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(SubscriptionFormState())
    val formState: StateFlow<SubscriptionFormState> = _formState

    private var originalEntity: SubscriptionEntity? = null

    // Loads existing DB record by unique row ID
    fun loadSubscription(subscriptionId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = repository.getSubscriptionById(subscriptionId)

            if (entity != null) {
                originalEntity = entity

                // 1. Map Billing Period Enum
                val periodEnum =
                    BillingPeriod.entries.getOrNull(entity.billingPeriod) ?: BillingPeriod.MONTHLY

                // 2. Fetch Category using bitwise check (> 0)
                val categoryEntity = repository.getCategoryForSubscription(entity.id)
                val matchedCategoryEnum = if (categoryEntity != null) {
                    Category.entries.find { it != Category.NONE && (categoryEntity.category and it.value) > 0L }
                        ?: Category.NONE
                } else {
                    Category.NONE
                }

                // 3. Fetch Notifications for daysBeforeToRemind
                val notifications =
                    repository.getNotificationsForSubscription(entity.id).firstOrNull()
                val notif = notifications?.firstOrNull()

                _formState.value = SubscriptionFormState(
                    id = entity.id,
                    name = entity.name,
                    category = matchedCategoryEnum,
                    packageName = entity.packageName,
                    price = entity.price.toString(),
                    billingPeriod = periodEnum,
                    daysBeforeToRemind = notif?.daysBeforeToRemind ?: 3,
                    description = entity.description ?: "",
                    status = entity.status
                )
            }
        }
    }

    fun updateName(value: String) {
        _formState.value = _formState.value.copy(name = value, error = null)
    }

    fun updateCategory(value: Category) {
        _formState.value = _formState.value.copy(category = value)
    }

    fun updatePackageName(value: String?) {
        _formState.value = _formState.value.copy(packageName = value)
    }

    fun updatePrice(value: String) {
        _formState.value = _formState.value.copy(price = value, error = null)
    }

    fun updateBillingPeriod(value: BillingPeriod) {
        _formState.value = _formState.value.copy(billingPeriod = value)
    }

    fun updateDaysBeforeToRemind(value: Int?) {
        _formState.value = _formState.value.copy(daysBeforeToRemind = value)
    }

    fun updateDescription(value: String) {
        _formState.value = _formState.value.copy(description = value)
    }

    fun updateStatus(value: Boolean) {
        _formState.value = _formState.value.copy(status = value)
    }

    fun save(onSuccess: () -> Unit) {
        val state = _formState.value
        val trimmedName = state.name.trim()
        val cleanPrice = state.price.trim().replace("$", "").replace(",", ".")
        val parsedPrice = cleanPrice.toDoubleOrNull()

        when {
            trimmedName.isEmpty() -> _formState.value =
                state.copy(error = "Enter a subscription name")

            parsedPrice == null -> _formState.value = state.copy(error = "Enter a valid price")
            else -> {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val original = originalEntity
                        val existingId = state.id
                        val daysBefore = state.daysBeforeToRemind ?: 3

                        if (existingId > 0) {
                            // Edit Mode: Update existing record
                            val updatedEntity = SubscriptionEntity(
                                id = existingId,
                                name = trimmedName,
                                packageName = state.packageName ?: original?.packageName,
                                price = parsedPrice,
                                billingPeriod = state.billingPeriod.ordinal,
                                nextRenewalDate = original?.nextRenewalDate
                                    ?: Calendar.getInstance().apply { add(Calendar.MONTH, 1) },
                                status = state.status,
                                startDate = original?.startDate ?: Calendar.getInstance(),
                                score = original?.score ?: 0,
                                description = state.description
                            )
                            repository.updateSubscription(updatedEntity)

                            // Update Category
                            val existingCategory = repository.getCategoryForSubscription(existingId)
                            if (existingCategory != null) {
                                repository.updateCategory(existingId, state.category.value)
                            } else {
                                repository.insertCategory(
                                    CategoryEntity(
                                        subscriptionId = existingId,
                                        category = state.category.value
                                    )
                                )
                            }

                            // Update Notification
                            val existingNotifications =
                                repository.getNotificationsForSubscription(existingId).firstOrNull()
                            val existingNotif = existingNotifications?.firstOrNull()
                            if (existingNotif != null) {
                                repository.insertNotification(
                                    existingNotif.copy(daysBeforeToRemind = daysBefore)
                                )
                            } else {
                                val reminderCal = (original?.nextRenewalDate?.clone() as? Calendar
                                    ?: Calendar.getInstance()).apply {
                                    add(Calendar.DAY_OF_MONTH, -daysBefore)
                                }
                                repository.insertNotification(
                                    NotificationEntity(
                                        subscriptionId = existingId,
                                        shouldRemind = true,
                                        reminderDate = reminderCal,
                                        daysBeforeToRemind = daysBefore,
                                        showPriceChanges = true
                                    )
                                )
                            }
                        } else {
                            // Add Mode: Insert new record + category + notification
                            val newSubscriptionEntity = SubscriptionEntity(
                                id = 0,
                                name = trimmedName,
                                packageName = state.packageName,
                                price = parsedPrice,
                                billingPeriod = state.billingPeriod.ordinal,
                                nextRenewalDate = Calendar.getInstance()
                                    .apply { add(Calendar.MONTH, 1) },
                                status = state.status,
                                startDate = Calendar.getInstance(),
                                score = 0,
                                description = state.description
                            )
                            val newSubscriptionId =
                                repository.insertSubscription(newSubscriptionEntity).toInt()

                            repository.insertCategory(
                                CategoryEntity(
                                    subscriptionId = newSubscriptionId,
                                    category = state.category.value
                                )
                            )

                            val reminderCal = Calendar.getInstance().apply {
                                add(Calendar.MONTH, 1)
                                add(Calendar.DAY_OF_MONTH, -daysBefore)
                            }
                            repository.insertNotification(
                                NotificationEntity(
                                    subscriptionId = newSubscriptionId,
                                    shouldRemind = true,
                                    reminderDate = reminderCal,
                                    daysBeforeToRemind = daysBefore,
                                    showPriceChanges = true
                                )
                            )
                        }

                        withContext(Dispatchers.Main) {
                            onSuccess()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            _formState.value = _formState.value.copy(
                                error = e.message ?: "Failed to save subscription"
                            )
                        }
                    }
                }
            }
        }
    }
}