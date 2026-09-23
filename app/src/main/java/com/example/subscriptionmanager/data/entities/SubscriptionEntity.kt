package com.example.subscriptionmanager.data.entities

import androidx.room3.ColumnTypeConverters
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import java.util.Calendar

@Entity(tableName = "subscriptions")
@ColumnTypeConverters(DateConverters::class)
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id:Int = 0,
    val name: String,
    val packageName: String,
    val price: Double,
    val billingPeriod: Int = BillingPeriod.MONTHLY.ordinal,
    val nextRenewalDate: Calendar, // We should keep this so that we don't have to calculate it
    val status: Boolean = true,
    val startDate: Calendar,
    val score: Int = 0,
)


