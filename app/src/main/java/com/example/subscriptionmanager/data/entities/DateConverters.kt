package com.example.subscriptionmanager.data.entities

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room3.ColumnTypeConverter
import java.time.Instant

// use this to format the instant to whatever you want
//val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault()) // Or ZoneId.of("UTC")
//formatter.format(instant)

object DateConverters {
    @ColumnTypeConverter
    @RequiresApi(Build.VERSION_CODES.O)
    fun longToInstant(timestamp: Long?): Instant? {
        return timestamp?.let {
            val instant = Instant.ofEpochMilli(timestamp)
            return instant
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @ColumnTypeConverter
    fun instantToLong(date: Instant?): Long? {
        return date?.toEpochMilli()
    }
}