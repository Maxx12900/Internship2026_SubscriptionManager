package com.example.subscriptionmanager.data.dao

import androidx.room.*
import com.example.subscriptionmanager.data.entities.CategoryEntity
import com.example.subscriptionmanager.data.entities.NotificationEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface NotifactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)



}