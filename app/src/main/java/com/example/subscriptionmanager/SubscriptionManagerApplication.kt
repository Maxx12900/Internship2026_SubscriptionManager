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
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        SubscriptionRepository(
            database.subscriptionDao(),
            database.categoryDao(),
            database.notificationDao()
        )
    }

    val seeds = listOf(
        Seed("Netflix", "com.netflix.mediaclient", 15.99, 0, dateOf(2026, 10, 21), true, dateOf(2024, 10, 21), 8, Category.STREAMING.value, true, dateOf(2026, 10, 18), 3, true, null),
        Seed("Spotify", "com.spotify.music", 10.99, 1, dateOf(2026, 10, 15), true, dateOf(2024, 10, 15), 9, Category.STREAMING.value, false, dateOf(2026, 10, 12), 3, true, null),
        Seed("Microsoft 365", "com.microsoft.office.officehub", 99.99, 1, dateOf(2027, 9, 21), true, dateOf(2024, 9, 21), 7, Category.PRODUCTIVITY.value, false, dateOf(2027, 9, 18), 3, true, null),
        Seed("Adobe Creative Cloud", "com.adobe.creativecloud", 54.99, 2, dateOf(2026, 10, 10), true, dateOf(2024, 10, 10), 6, Category.PRODUCTIVITY.value, false, dateOf(2026, 10, 7), 3, false, null),
        Seed("Disney+", "com.disney.disneyplus", 7.99, 3, dateOf(2026, 10, 25), true, dateOf(2024, 10, 25), 8, Category.STREAMING.value, true, dateOf(2026, 10, 22), 3, true, null),
        Seed("YouTube Premium", "com.google.android.youtube", 13.99, 4, dateOf(2026, 10, 5), true, dateOf(2024, 10, 5), 9, Category.STREAMING.value, true, dateOf(2026, 10, 2), 3, true, dateOf(2024, 11, 5)),
        Seed("Dropbox", "com.dropbox.android", 11.99, 3, dateOf(2026, 10, 18), false, dateOf(2024, 10, 18), 7, Category.PRODUCTIVITY.value, true, dateOf(2026, 10, 15), 3, true, null),
        Seed("Audible", "com.audible.application", 14.95, 0, dateOf(2026, 10, 12), true, dateOf(2024, 10, 12), 8, Category.STREAMING.value, true, dateOf(2026, 10, 9), 3, false, null),
    )

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            database.clearAllTables()
            seedDatabase()
        }
    }

    private suspend fun seedDatabase() {
        val subscriptionDao = database.subscriptionDao()
        val categoryDao = database.categoryDao()
        val notificationDao = database.notificationDao()

        for (seed in seeds) {
            val subscriptionId = subscriptionDao.insertSubscription(
                SubscriptionEntity(
                    id = 0,
                    name = seed.name,
                    packageName = seed.packageName,
                    price = seed.price,
                    billingPeriod = seed.billingPeriod,
                    nextRenewalDate = seed.nextRenewalDate,
                    status = seed.status,
                    startDate = seed.startDate,
                    score = seed.score,
                    description = ""
                )
            ).toInt()

            categoryDao.insertCategory(
                CategoryEntity(subscriptionId = subscriptionId, category = seed.category)
            )

            notificationDao.insertNotification(
                NotificationEntity(
                    subscriptionId = subscriptionId,
                    shouldRemind = seed.shouldRemind,
                    reminderDate = seed.reminderDate,
                    daysBeforeToRemind = seed.daysBeforeToRemind,
                    showPriceChanges = seed.showPriceChanges,
                    trialEndDate = seed.trialEndDate
                )
            )
        }
    }

    private fun dateOf(year: Int, month: Int, day: Int): Calendar =
        Calendar.getInstance().apply {
            set(year, month - 1, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
}