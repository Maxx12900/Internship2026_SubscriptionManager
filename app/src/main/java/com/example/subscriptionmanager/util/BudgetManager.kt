package com.example.subscriptionmanager.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object BudgetManager {
    private val _monthlyBudget = MutableStateFlow(0.0) // Default 0.0 = Unset / Optional
    val monthlyBudget: StateFlow<Double> = _monthlyBudget

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        _monthlyBudget.value = prefs.getFloat("monthly_budget", 0.0f).toDouble()
    }

    fun setBudget(context: Context, amount: Double) {
        _monthlyBudget.value = amount
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit()
            .putFloat("monthly_budget", amount.toFloat())
            .apply()
    }
}