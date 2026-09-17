package com.example.subscriptionmanager.data.dao

import androidx.room.*
import com.example.subscriptionmanager.data.entities.NotificationEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity):Long

    @Query("SELECT * FROM notifications WHERE subscriptionId = :subscriptionId")
    fun getNotificationsForSubscription(subscriptionId: Long): Flow<List<NotificationEntity>>


    @Delete
    suspend fun deleteNotification(notification: NotificationDao)

}