package com.example.subscriptionmanager.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subscriptionmanager.SubscriptionManagerApplication
import com.example.subscriptionmanager.data.entities.Category
import com.example.subscriptionmanager.ui.common.AppIcon
import com.example.subscriptionmanager.ui.common.GenericViewModelFactory
import com.example.subscriptionmanager.ui.common.padding
import com.example.subscriptionmanager.util.CurrencyManager
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    onSubscriptionClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as SubscriptionManagerApplication).repository
    val viewModel: HomeViewModel = viewModel(
        factory = GenericViewModelFactory { HomeViewModel(repository) }
    )

    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(padding)
    ) {
        Column {
            Text("Subscription Manager", style = typography.headlineLarge, fontWeight = FontWeight.Bold)
        }

        MonthlySpentCard(
            monthlySpent = uiState.monthlySpent,
            yearlySpent = uiState.yearlySpent,
            activeCount = uiState.activeCount
        )

        CategoryBreakdown(uiState.categorySpends)

        Row(horizontalArrangement = Arrangement.spacedBy(padding)) {
            FilterChip(
                selected = selectedTab == HomeTab.RECENT_PURCHASES,
                onClick = { viewModel.selectTab(HomeTab.RECENT_PURCHASES) },
                label = { Text("Recent purchases") },
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            )
            FilterChip(
                selected = selectedTab == HomeTab.UPCOMING_PAYMENTS,
                onClick = { viewModel.selectTab(HomeTab.UPCOMING_PAYMENTS) },
                label = { Text("Upcoming payments") },
                leadingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(padding)) {
            uiState.subscriptions.forEach { row ->
                HomeSubscriptionCard(row, onClick = { onSubscriptionClick(row.id) })
            }
        }
    }
}

@Composable
private fun MonthlySpentCard(monthlySpent: Double, yearlySpent: Double, activeCount: Int) {
    val currentCurrency by CurrencyManager.currency.collectAsState()
    Card(
        shape = RoundedCornerShape(padding),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant) // Theme-aware card

    ) {
        Column(modifier = Modifier.padding(padding)) {
            Text(
                "MONTHLY SPENT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary
            )
            Text(
                text = CurrencyManager.formatPrice(monthlySpent, currentCurrency),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.size(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${CurrencyManager.formatPrice(yearlySpent, currentCurrency)} / year",
                    fontSize = 13.sp,
                    color = Color(0xFF49454F)
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("$activeCount active", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdown(categorySpends: List<CategorySpend>) {
    if (categorySpends.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("BY CATEGORY", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        categorySpends.forEach { spend ->
            CategoryRow(spend)
        }
    }
}

@Composable
private fun CategoryRow(spend: CategorySpend) {
    val currentCurrency by CurrencyManager.currency.collectAsState()

    val percentage = (spend.fractionOfTotal * 100).roundToInt()

    Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${spend.category.displayName()} - ${percentage}%", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = CurrencyManager.formatPrice(spend.monthlyAmount, currentCurrency),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

        }

        Spacer(Modifier.size(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(spend.fractionOfTotal.coerceIn(0f, 1f))
                    .background(colorScheme.primary, RoundedCornerShape(50))
            )
        }
    }
}

private fun Category.displayName(): String =
    if (this == Category.NONE) "No category"
    else name.lowercase().replaceFirstChar { it.uppercase() }

@Composable
private fun StatusPill(isActive: Boolean) {
    // CHANGED: Use Theme colors instead of hardcoded hexes for inactive
    val bg = if (isActive) Color(0xFFD0F0D0) else colorScheme.error
    val fg = if (isActive) Color(0xFF4CAF50) else colorScheme.onError

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(if (isActive) "Active" else "Inactive", color = fg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HomeSubscriptionCard(
    row: HomeSubscriptionRow,
    onClick: () -> Unit
) {
    val currentCurrency by CurrencyManager.currency.collectAsState()
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(row.packageName, fallbackLetter = "${row.name.first()}")
            Spacer(Modifier.size(padding))
            Column(modifier = Modifier.weight(1f)) {
                Text(row.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(row.subtitle, fontSize = 12.sp)
            }
            Spacer(Modifier.size(8.dp))
            Text(
                text = CurrencyManager.formatPrice(row.price, currentCurrency),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(Modifier.size(8.dp))
            StatusPill(row.isActive)
        }
    }
}