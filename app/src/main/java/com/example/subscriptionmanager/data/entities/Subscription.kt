package com.example.subscriptionmanager.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Subscription")
data class Subscription(@PrimaryKey(autoGenerate = true)
    val subscriptionId:Int = 0,
    val categoryId: Int? = null,
    val paymentMethodId: Int? = null,
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


