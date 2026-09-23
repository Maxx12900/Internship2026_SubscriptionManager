package com.example.subscriptionmanager.data.entities

import androidx.room3.ColumnTypeConverter
import java.util.Calendar // Kotlin works with java libs

object DateConverters {
    @ColumnTypeConverter
    fun longToCalendar(timestamp: Long?): Calendar? {
        return timestamp?.let {
            val calendar = Calendar.getInstance().apply { timeInMillis = timestamp}
            calendar
        }
    }

    @ColumnTypeConverter
    fun calendarToLong(date: Calendar?): Long? {
        return date?.timeInMillis
    }
}
