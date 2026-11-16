package com.example.subscriptionmanager

import android.app.Application
import com.example.subscriptionmanager.data.AppDatabase
import com.example.subscriptionmanager.data.entities.Category
import com.example.subscriptionmanager.data.entities.CategoryEntity
import com.example.subscriptionmanager.data.entities.NotificationEntity
import com.example.subscriptionmanager.data.entities.SubscriptionEntity
import com.example.subscriptionmanager.data.repository.SubscriptionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar

data class Seed(
    val name: String,
    val packageName: String,
    val price: Double,
    val billingPeriod: Int,
    val nextRenewalDate: Calendar,
    val status: Boolean,
    val startDate: Calendar,
    val score: Int,
    val category: Long,
    val shouldRemind: Boolean,
    val reminderDate: Calendar,
    val daysBeforeToRemind: Int,
    val showPriceChanges: Boolean,
    val trialEndDate: Calendar?
)

class SubscriptionManagerApplication : Application() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        SubscriptionRepository(
            database.subscriptionDao(),
            database.categoryDao(),
            database.notificationDao()
        )
    }

    private fun dateOf(year: Int, month: Int, day: Int): Calendar =
        Calendar.getInstance().apply {
            set(year, month - 1, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
}