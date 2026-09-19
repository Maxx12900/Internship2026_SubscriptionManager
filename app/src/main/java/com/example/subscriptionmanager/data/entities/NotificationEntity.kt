package com.example.subscriptionmanager.data.entities

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = SubscriptionEntity::class,
            parentColumns = ["id"],
            childColumns = ["subscriptionId"],
            onDelete = ForeignKey.CASCADE // a row is deleted when the row in the subscriptions table with its id is deleted
        )
    ],
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subscriptionId: Int = 0,
    val shouldRemind: Boolean,
    val reminderDate: String, // date of the next reminder
    val daysBeforeToRemind: Int, // Works for every type of reminder
    val showPriceChanges: Boolean, // FIXME: we need to deal with this one :\
    val trialEndDate: String? = null,
)
