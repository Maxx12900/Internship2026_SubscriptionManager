package com.example.subscriptionmanager

import android.app.Application
import com.example.subscriptionmanager.data.AppDatabase
import com.example.subscriptionmanager.data.entities.CategoryEntity
import com.example.subscriptionmanager.data.entities.NotificationEntity
import com.example.subscriptionmanager.data.entities.SubscriptionEntity
import com.example.subscriptionmanager.data.repository.SubscriptionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar

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
// Remove everything below; just for testing purposes
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

        val seeds = listOf(
            Seed("Netflix", "com.netflix.mediaclient", 15.99, 3, dateOf(2026, 10, 21), true, dateOf(2024, 10, 21), 8, 0, true, dateOf(2026, 10, 18), 3, true, null),
            Seed("Spotify", "com.spotify.music", 10.99, 8, dateOf(2026, 10, 15), true, dateOf(2024, 10, 15), 9, 1, true, dateOf(2026, 10, 12), 3, true, null),
            Seed("Microsoft 365", "com.microsoft.office.officehub", 99.99, 8, dateOf(2027, 9, 21), true, dateOf(2024, 9, 21), 7, 2, true, dateOf(2027, 9, 18), 3, true, null),
            Seed("Adobe Creative Cloud", "com.adobe.creativecloud", 54.99, 6, dateOf(2026, 10, 10), true, dateOf(2024, 10, 10), 6, 3, false, dateOf(2026, 10, 7), 3, false, null),
            Seed("Disney+", "com.disney.disneyplus", 7.99, 7, dateOf(2026, 10, 25), true, dateOf(2024, 10, 25), 8, 0, true, dateOf(2026, 10, 22), 3, true, null),
            Seed("YouTube Premium", "com.google.android.youtube", 13.99, 6, dateOf(2026, 10, 5), true, dateOf(2024, 10, 5), 9, 0, true, dateOf(2026, 10, 2), 3, true, dateOf(2024, 11, 5)),
            Seed("Dropbox", "com.dropbox.android", 11.99, 7, dateOf(2026, 10, 18), false, dateOf(2024, 10, 18), 7, 4, true, dateOf(2026, 10, 15), 3, true, null),
            Seed("Audible", "com.audible.application", 14.95, 1, dateOf(2026, 10, 12), true, dateOf(2024, 10, 12), 8, 5, true, dateOf(2026, 10, 9), 3, false, null),
        )

        for (seed in seeds) {
            val subscriptionId = subscriptionDao.insertSubscription(
                SubscriptionEntity(
                    id = 0, // Room ignores this and auto-generates on insert
                    name = seed.name,
                    packageName = seed.packageName,
                    price = seed.price,
                    billingPeriod = seed.billingPeriod,
                    nextRenewalDate = seed.nextRenewalDate,
                    status = seed.status,
                    startDate = seed.startDate,
                    score = seed.score
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
            set(year, month - 1, day, 0, 0, 0) // Calendar months are 0-indexed
            set(Calendar.MILLISECOND, 0)
        }

}