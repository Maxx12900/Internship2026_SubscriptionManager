package com.example.subscriptionmanager.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = SubscriptionEntity::class,
            parentColumns = ["subscriptionId"],
            childColumns = ["subscriptionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["subscriptionId"])]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val notificationId: Int = 0,
    val subscriptionId: Int = 0,
    val remindAt: Long,
    val isShown:Boolean = false,
    val status: String = "pending"
)
