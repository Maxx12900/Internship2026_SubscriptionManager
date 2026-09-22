package com.example.subscriptionmanager.data.entities

import androidx.room3.ColumnTypeConverters
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.time.Instant

@Entity(tableName = "subscriptions")
@ColumnTypeConverters(DateConverters::class)
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id:Int = 0,
    val name: String,
    val packageName: String,
    val price: Double,
    val billingPeriod: BillingPeriod = BillingPeriod.MONTHLY,
    val nextRenewalDate: Instant, // We should keep this so that we don't have to calculate it
    val status: Boolean = true,
    val startDate: Instant,
    val score: Int = 0,
    val updatedAt: Long = System.currentTimeMillis() // Do we really need this? - Aiden
)


