package com.example.subscriptionmanager.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateTimeManager {
    private val _dateFormatOption = MutableStateFlow("DD/MM/YYYY")
    val dateFormatOption: StateFlow<String> = _dateFormatOption

    private val _dateFormatPattern = MutableStateFlow("dd/MM/yyyy")
    val dateFormatPattern: StateFlow<String> = _dateFormatPattern


    fun init(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val option = prefs.getString("date_format_option", "DD/MM/YYYY") ?: "DD/MM/YYYY"
        val pattern = prefs.getString("date_format_pattern", "dd/MM/yyyy") ?: "dd/MM/yyyy"
        val time = prefs.getString("time_format", "24-hour") ?: "24-hour"

        _dateFormatOption.value = option
        _dateFormatPattern.value = pattern

    }

    fun setDateFormat(context: Context, option: String) {
        val pattern = when (option) {
            "DD/MM/YYYY" -> "dd/MM/yyyy"
            "MM/DD/YYYY" -> "MM/dd/yyyy"
            "YYYY-MM-DD" -> "yyyy-MM-dd"
            else -> "dd/MM/yyyy"
        }
        _dateFormatOption.value = option
        _dateFormatPattern.value = pattern
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit()
            .putString("date_format_option", option)
            .putString("date_format_pattern", pattern)
            .apply()
    }

    fun formatDate(calendar: Calendar, pattern: String = _dateFormatPattern.value): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(calendar.time)
    }
}