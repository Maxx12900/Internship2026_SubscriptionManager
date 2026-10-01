package com.example.subscriptionmanager.data.entities

import androidx.room3.ColumnTypeConverter
import java.util.Calendar

object BillingPeriodConverters {
    @ColumnTypeConverter
    fun intToBillingPeriod(ordinal: Int?): BillingPeriod? {
        return ordinal?.let {
            val billingPeriod = BillingPeriod.entries[ordinal]
            billingPeriod
        }
    }

    @ColumnTypeConverter
    fun billingPeriodToInt(billingPeriod: BillingPeriod?): Int? {
        return billingPeriod?.ordinal
    }
}