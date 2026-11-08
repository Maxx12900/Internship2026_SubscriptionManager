package com.example.subscriptionmanager.ui.subscriptionAdd

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subscriptionmanager.SubscriptionManagerApplication
import com.example.subscriptionmanager.data.entities.BillingPeriod
import com.example.subscriptionmanager.data.entities.Category
import com.example.subscriptionmanager.data.entities.CategoryEntity
import com.example.subscriptionmanager.data.entities.NotificationEntity
import com.example.subscriptionmanager.data.entities.SubscriptionEntity
import com.example.subscriptionmanager.data.repository.SubscriptionRepository
import com.example.subscriptionmanager.notifications.areNotificationsAllowed
import com.example.subscriptionmanager.notifications.areNotificationsEnabled
import com.example.subscriptionmanager.notifications.addBillingPeriod
import com.example.subscriptionmanager.notifications.cancelReminder
import com.example.subscriptionmanager.notifications.scheduleReminder
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
    val startDate: Calendar = Calendar.getInstance(),
    val daysBeforeToRemind: Int? = 3,
    val shouldRemind: Boolean = true,
    val description: String = "",
    val status: Boolean = true,
    val error: String? = null
)

class SubscriptionAddViewModel(
    private val application: Application
) : AndroidViewModel(application) {

    private val _formState = MutableStateFlow(
        SubscriptionFormState(
            shouldRemind = areNotificationsAllowed(application) && areNotificationsEnabled(application)
        )
    )

    val repository = (application.applicationContext as SubscriptionManagerApplication).repository
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
                    startDate = entity.startDate,
                    daysBeforeToRemind = notif?.daysBeforeToRemind ?: 3,
                    shouldRemind = notif?.shouldRemind ?: (areNotificationsEnabled(application) && areNotificationsAllowed(application)),
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

    fun updateStartDate(value: Calendar) {
        _formState.value = _formState.value.copy(startDate = value)
    }

    fun updateDaysBeforeToRemind(value: Int?) {
        _formState.value = _formState.value.copy(daysBeforeToRemind = value)
    }

    fun updateShouldRemind(value: Boolean) {
        _formState.value = _formState.value.copy(shouldRemind = value)
    }

    fun updateDescription(value: String) {
        _formState.value = _formState.value.copy(description = value)
    }

    fun updateStatus(value: Boolean) {
        _formState.value = _formState.value.copy(status = value)
    }

    private fun calculateNextRenewalDate(startDate: Calendar, billingPeriod: BillingPeriod): Calendar {
        val renewal = startDate.clone() as Calendar
        val today = Calendar.getInstance()
        addBillingPeriod(renewal, billingPeriod)
        while (renewal.before(today)) {
            addBillingPeriod(renewal, billingPeriod)
        }

        return renewal
    }

    /**
     * Persists the reminder settings and keeps the alarm in sync with them: any previously armed
     * alarm for this subscription is cancelled first, then a new one is armed only if reminders
     * are on for it.
     */
    private suspend fun saveNotification(
        notification: NotificationEntity,
        previous: NotificationEntity? = null
    ) {
        cancelReminder(application, notification.subscriptionId)
        previous?.scheduledNotificationId?.let { legacyId ->
            cancelReminder(application, notification.subscriptionId, legacyId)
        }

        val scheduledNotification = if (notification.shouldRemind) {
            scheduleReminder(
                application,
                notification.reminderDate,
                notification.subscriptionId
            )
        } else {
            null
        }

        repository.insertNotification(
            notification.copy(
                scheduledReminder = scheduledNotification != null,  // this can fail
                scheduledNotificationId = scheduledNotification?.id
            )
        )
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
                        val shouldRemind = state.shouldRemind
                        val calculatedRenewal = calculateNextRenewalDate(state.startDate, state.billingPeriod)

                        if (existingId > 0) {
                            // Edit Mode: Update existing record
                            val updatedEntity = SubscriptionEntity(
                                id = existingId,
                                name = trimmedName,
                                packageName = state.packageName ?: original?.packageName,
                                price = parsedPrice,
                                billingPeriod = state.billingPeriod.ordinal,
                                nextRenewalDate = calculatedRenewal,
                                status = state.status,
                                startDate = state.startDate,
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
                            val reminderCal = (calculatedRenewal.clone() as Calendar).apply {
                                add(Calendar.DAY_OF_MONTH, -daysBefore)
                            }
                            if (existingNotif != null) {
                                saveNotification(
                                    existingNotif.copy(
                                        shouldRemind = shouldRemind,
                                        reminderDate = reminderCal,
                                        daysBeforeToRemind = daysBefore
                                    ),
                                    previous = existingNotif
                                )
                            } else {
                                saveNotification(
                                    NotificationEntity(
                                        subscriptionId = existingId,
                                        shouldRemind = shouldRemind,
                                        reminderDate = reminderCal,
                                        daysBeforeToRemind = daysBefore,
                                        showPriceChanges = true,
                                        scheduledReminder = false,
                                        scheduledNotificationId = null
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
                                nextRenewalDate = calculatedRenewal,
                                status = state.status,
                                startDate = state.startDate,
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

                            val reminderCal = (calculatedRenewal.clone() as Calendar).apply {
                                add(Calendar.DAY_OF_MONTH, -daysBefore)
                            }
                            saveNotification(
                                NotificationEntity(
                                    subscriptionId = newSubscriptionId,
                                    shouldRemind = shouldRemind,
                                    reminderDate = reminderCal,
                                    daysBeforeToRemind = daysBefore,
                                    showPriceChanges = true,
                                    scheduledReminder = false,
                                    scheduledNotificationId = null
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