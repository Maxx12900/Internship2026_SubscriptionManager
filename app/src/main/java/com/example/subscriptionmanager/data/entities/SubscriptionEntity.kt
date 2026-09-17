package com.example.subscriptionmanager.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subscriptions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["categoryId"])
    ]
)
data class SubscriptionEntity(@PrimaryKey(autoGenerate = true)
    val subscriptionId:Int = 0,
    val categoryId: Int? = null,
    val name: String,
    val price: Double,
    val currency: String = "USD",
    val billingPeriod: String = "monthly",
    val status:String = "active",
    val startDate: String,
    val nextRenewalDate: String? = null,
    val trialEndDate: String? = null,
    val isAutoRenewal: Boolean,
    val notes: String? = null,
    val score: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)


