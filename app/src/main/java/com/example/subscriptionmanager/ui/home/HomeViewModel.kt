package com.example.subscriptionmanager.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subscriptionmanager.data.entities.BillingPeriod
import com.example.subscriptionmanager.data.entities.Category
import com.example.subscriptionmanager.data.entities.CategoryEntity
import com.example.subscriptionmanager.data.entities.SubscriptionEntity
import com.example.subscriptionmanager.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

enum class HomeTab { RECENT_PURCHASES, UPCOMING_PAYMENTS }

data class CategorySpend(
    val category: Category,
    val monthlyAmount: Double,
    val fractionOfTotal: Float
)

data class HomeSubscriptionRow(
    val id: Int,
    val name: String,
    val packageName: String?,
    val price: Double,
    val isActive: Boolean,
    val subtitle: String
)

data class HomeUiState(
    val monthlySpent: Double = 0.0,
    val yearlySpent: Double = 0.0,
    val activeCount: Int = 0,
    val categorySpends: List<CategorySpend> = emptyList(),
    val subscriptions: List<HomeSubscriptionRow> = emptyList()
)

class HomeViewModel(
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(HomeTab.UPCOMING_PAYMENTS)
    val selectedTab: StateFlow<HomeTab> = _selectedTab

    fun selectTab(tab: HomeTab) {
        _selectedTab.value = tab
    }

    val uiState: StateFlow<HomeUiState> = combine(
        subscriptionRepository.getAllSubscriptions(),
        subscriptionRepository.getAllCategories(),
        _selectedTab
    ) { subscriptions, categoryEntities, tab ->
        buildUiState(subscriptions, categoryEntities, tab)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    private fun buildUiState(
        subscriptions: List<SubscriptionEntity>,
        categoryEntities: List<CategoryEntity>,
        tab: HomeTab
    ): HomeUiState {
        val activeSubscriptions = subscriptions.filter { it.status }

        val categoryBySubscriptionId: Map<Int, Category> = categoryEntities.associate { entity ->
            entity.subscriptionId to (Category.entries.find { it.value == entity.category } ?: Category.NONE)
        }

        val monthlySpent = activeSubscriptions.sumOf { monthlyEquivalent(it.price, it.billingPeriod) }

        val spendByCategory: Map<Category, Double> = activeSubscriptions
            .groupBy { categoryBySubscriptionId[it.id] ?: Category.NONE }
            .mapValues { (_, subs) -> subs.sumOf { monthlyEquivalent(it.price, it.billingPeriod) } }

        val totalSpend = spendByCategory.values.sum()
        val categorySpends = spendByCategory
            .filter { it.value > 0.0 }
            .map { (category, amount) ->
                CategorySpend(
                    category = category,
                    monthlyAmount = amount,
                    fractionOfTotal = if (totalSpend > 0) (amount / totalSpend).toFloat() else 0f
                )
            }
            .sortedByDescending { it.monthlyAmount }

        val now = Calendar.getInstance()
        val rows = when (tab) {
            HomeTab.UPCOMING_PAYMENTS -> activeSubscriptions
                .filter { it.nextRenewalDate.timeInMillis >= now.timeInMillis }
                .sortedBy { it.nextRenewalDate.timeInMillis }
                .take(5)
                .map { it.toUpcomingRow(now) }

            HomeTab.RECENT_PURCHASES -> subscriptions
                .sortedByDescending { it.getLastPaidDate(now).timeInMillis }
                .take(5)
                .map { it.toRecentRow(now) }
        }

        return HomeUiState(
            monthlySpent = monthlySpent,
            yearlySpent = monthlySpent * 12,
            activeCount = activeSubscriptions.size,
            categorySpends = categorySpends,
            subscriptions = rows
        )
    }

    private fun SubscriptionEntity.getLastPaidDate(now: Calendar = Calendar.getInstance()): Calendar {
        val period = BillingPeriod.entries.getOrNull(billingPeriod) ?: BillingPeriod.MONTHLY
        var paymentDate = nextRenewalDate.clone() as Calendar

        while (paymentDate.after(now) && paymentDate.after(startDate)) {
            val prev = paymentDate.clone() as Calendar
            when (period) {
                BillingPeriod.WEEKLY -> prev.add(Calendar.WEEK_OF_YEAR, -1)
                BillingPeriod.MONTHLY -> prev.add(Calendar.MONTH, -1)
                BillingPeriod.THREE_MONTHS -> prev.add(Calendar.MONTH, -3)
                BillingPeriod.SIX_MONTHS -> prev.add(Calendar.MONTH, -6)
                BillingPeriod.YEARLY -> prev.add(Calendar.YEAR, -1)
            }
            if (prev.before(startDate)) {
                paymentDate = startDate
                break
            } else {
                paymentDate = prev
            }
        }
        return paymentDate
    }

    private fun SubscriptionEntity.toRecentRow(now: Calendar): HomeSubscriptionRow {
        val lastPaid = getLastPaidDate(now)
        val days = daysBetween(lastPaid, now)
        val subtitle = when {
            days <= 0 -> "Paid today"
            days == 1L -> "Paid yesterday"
            else -> "Paid $days days ago"
        }
        return HomeSubscriptionRow(id, name, packageName, price, status, subtitle)
    }


    private fun SubscriptionEntity.toUpcomingRow(now: Calendar): HomeSubscriptionRow {
        val days = daysBetween(now, nextRenewalDate)
        val subtitle = when {
            days <= 0 -> "Renews today"
            days == 1L -> "Renews tomorrow"
            else -> "Renews in $days days"
        }
        return HomeSubscriptionRow(id, name, packageName, price, status, subtitle)
    }


    private fun daysBetween(from: Calendar, to: Calendar): Long =
        (to.timeInMillis - from.timeInMillis) / (1000 * 60 * 60 * 24)

    private fun monthlyEquivalent(price: Double, billingPeriodOrdinal: Int): Double {
        val period = BillingPeriod.entries.getOrNull(billingPeriodOrdinal) ?: BillingPeriod.MONTHLY
        return when (period) {
            BillingPeriod.WEEKLY -> price * 52.0 / 12.0
            BillingPeriod.MONTHLY -> price
            BillingPeriod.THREE_MONTHS -> price / 3.0
            BillingPeriod.SIX_MONTHS -> price / 6.0
            BillingPeriod.YEARLY -> price / 12.0
        }
    }
}