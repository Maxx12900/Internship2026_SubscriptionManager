package com.example.subscriptionmanager.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val notificationId: Int = 0,
    val subscriptionId: Int = 0,
    val remindAt: Long,
    val isShown:Boolean = false,
    val status: String = "pending"
)
