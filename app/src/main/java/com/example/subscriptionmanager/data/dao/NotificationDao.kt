package com.example.subscriptionmanager.data.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.example.subscriptionmanager.data.entities.NotificationEntity
import com.example.subscriptionmanager.data.entities.SubscriptionEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity):Long

    @Query("SELECT * FROM notifications WHERE subscriptionId = :subscriptionId")
    fun getNotificationsForSubscription(subscriptionId: Int): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE reminderDate = :currentDate AND shouldRemind = daysBeforeToRemind")
    fun getNotificationsForReminders(currentDate: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE trialEndDate IS NOT NULL AND CAST(julianday(trialEndDate) - julianday(:currentDate) AS INTEGER) = daysBeforeToRemind")
    fun getNotificationsForTrialEnding(currentDate: String): Flow<List<NotificationEntity>>
}