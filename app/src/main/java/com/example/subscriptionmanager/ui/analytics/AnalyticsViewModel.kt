package com.example.subscriptionmanager.ui.analytics

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subscriptionmanager.data.entities.Category
import com.example.subscriptionmanager.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CategoryChartSlice(
    val category: Category,
    val amount: Double,
    val percentage: Float,
    val color: Color
)

data class MonthBarData(
    val monthName: String,
    val amount: Double
)

data class AnalyticsUiState(
    val totalMonthly: Double = 0.0,
    val totalYearly: Double = 0.0,
    val topCategory: String = "None",
    val pieSlices: List<CategoryChartSlice> = emptyList(),
    val monthlyHistory: List<MonthBarData> = emptyList()
)

class AnalyticsViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val categoryColors = mapOf(
        Category.STREAMING to Color(0xFF635BFF),
        Category.PRODUCTIVITY to Color(0xFF00C48C),
        Category.GAMES to Color(0xFFFF6B6B),
    )

    val uiState: StateFlow<AnalyticsUiState> = repository.getAllSubscriptions()
        .map { subscriptions ->
            val activeSubs = subscriptions.filter { it.status }
            val totalMonthly = activeSubs.sumOf { it.price }
            val totalYearly = totalMonthly * 12

            val categoryAmounts = activeSubs.groupBy { it.packageName ?: "Other" }
                .map { (pkg, subs) ->
                    val cat = when {
                        pkg.contains("netflix") || pkg.contains("spotify") || pkg.contains("youtube") || pkg.contains("disney") -> Category.STREAMING
                        pkg.contains("adobe") || pkg.contains("dropbox") || pkg.contains("microsoft") || pkg.contains("docs") -> Category.PRODUCTIVITY
                        pkg.contains("duolingo") -> Category.GAMES
                        else -> Category.STREAMING
                    }
                    cat to subs.sumOf { it.price }
                }
                .groupBy { it.first }
                .mapValues { entry -> entry.value.sumOf { it.second } }

            val slices = categoryAmounts.map { (cat, amount) ->
                val percentage = if (totalMonthly > 0) (amount / totalMonthly).toFloat() else 0f
                CategoryChartSlice(
                    category = cat,
                    amount = amount,
                    percentage = percentage,
                    color = categoryColors[cat] ?: Color(0xFF8E8E93)
                )
            }.sortedByDescending { it.amount }

            val topCategoryName = slices.firstOrNull()?.category?.name
                ?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "None"

            val history = listOf(
                MonthBarData("Jan", totalMonthly * 0.85),
                MonthBarData("Feb", totalMonthly * 0.90),
                MonthBarData("Mar", totalMonthly * 0.95),
                MonthBarData("Apr", totalMonthly * 0.92),
                MonthBarData("May", totalMonthly),
                MonthBarData("Jun", totalMonthly * 1.05)
            )

            AnalyticsUiState(
                totalMonthly = totalMonthly,
                totalYearly = totalYearly,
                topCategory = topCategoryName,
                pieSlices = slices,
                monthlyHistory = history
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AnalyticsUiState()
        )
}