package com.example.subscriptionmanager.data.entities

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class DateConvertersTest {

    @Test
    fun longToCalendar_convertsLongToCalendarCorrectly() {
        val timestamp = 1716210000000L

        val calendar = DateConverters.longToCalendar(timestamp)

        assertEquals(timestamp, calendar?.timeInMillis)
    }

    @Test
    fun calendarToLong_convertsCalendarToLongCorrectly() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = 1716210000000L
        }

        val timestamp = DateConverters.calendarToLong(calendar)

        assertEquals(1716210000000L, timestamp)
    }

    @Test
    fun converters_handleNullValuesGracefully() {
        assertNull(DateConverters.longToCalendar(null))
        assertNull(DateConverters.calendarToLong(null))
    }
}