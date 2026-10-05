package com.example.subscriptionmanager.util

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

object CurrencyManager {
    private val _currency = MutableStateFlow("USD")
    val currency: StateFlow<String> = _currency

    // Default fallback exchange rates (relative to 1 USD)
    private val _exchangeRates = MutableStateFlow(
        mapOf(
            "USD" to 1.0,
            "EUR" to 0.92,
            "MDL" to 18.00,
        )
    )

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        _currency.value = prefs.getString("selected_currency", "USD") ?: "USD"

        // Fetch live exchange rates asynchronously in background
        //fetchLiveExchangeRates(context)
    }

    fun setCurrency(context: Context, currencyCode: String) {
        _currency.value = currencyCode
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit()
            .putString("selected_currency", currencyCode)
            .apply()
    }

    // Fetches live rates from free public Exchange Rates API
//    private fun fetchLiveExchangeRates(context: Context) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val apiUrl = "https://open.er-api.com/v6/latest/USD"
//                val responseText = URL(apiUrl).readText()
//                val json = JSONObject(responseText)
//                val ratesObj = json.getJSONObject("rates")
//
//                val liveRates = mutableMapOf<String, Double>()
//                val keys = listOf("USD", "EUR", "MDL",)
//
//                for (key in keys) {
//                    if (ratesObj.has(key)) {
//                        liveRates[key] = ratesObj.getDouble(key)
//                    }
//                }
//
//                if (liveRates.isNotEmpty()) {
//                    _exchangeRates.value = liveRates
//                }
//            } catch (e: Exception) {
//                // If offline or network error occurs, fall back gracefully to default rates
//                e.printStackTrace()
//            }
//        }
//    }

    // Formats base USD price into target currency using live rates
    fun formatPrice(
        basePriceInUsd: Double,
        targetCurrency: String = _currency.value
    ): String {
        val rates = _exchangeRates.value
        val rate = rates[targetCurrency] ?: 1.0
        val convertedPrice = basePriceInUsd * rate

        val symbol = when (targetCurrency) {
            "USD" -> "$"
            "EUR" -> "€"
            "MDL" -> "lei"
            else -> "€"
        }

        return if (targetCurrency == "MDL") {
            "%.2f %s".format(convertedPrice, symbol) // e.g. "989.82 lei"
        } else {
            "%s%.2f".format(symbol, convertedPrice) // e.g. "€50.59"
        }
    }
}