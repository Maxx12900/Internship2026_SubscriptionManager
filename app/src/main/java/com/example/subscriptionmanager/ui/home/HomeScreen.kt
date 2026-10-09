package com.example.subscriptionmanager.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subscriptionmanager.SubscriptionManagerApplication
import com.example.subscriptionmanager.data.entities.Category
import com.example.subscriptionmanager.ui.common.AppIcon
import com.example.subscriptionmanager.ui.common.GenericViewModelFactory
import com.example.subscriptionmanager.ui.common.padding
import com.example.subscriptionmanager.util.BudgetManager
import com.example.subscriptionmanager.util.CurrencyManager
import kotlin.math.roundToInt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.example.subscriptionmanager.R

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
            Text(
                text = stringResource(R.string.app_name),
                style = typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
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
                label = { Text(stringResource(R.string.recent_purchases)) },
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            )
            FilterChip(
                selected = selectedTab == HomeTab.UPCOMING_PAYMENTS,
                onClick = { viewModel.selectTab(HomeTab.UPCOMING_PAYMENTS) },
                label = { Text(stringResource(R.string.upcoming_payments)) },
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
    val monthlyBudget by BudgetManager.monthlyBudget.collectAsState()
    val context = LocalContext.current
    var showBudgetDialog by remember { mutableStateOf(false) }

    val hasBudget = monthlyBudget > 0.0
    val percentage = if (hasBudget) (monthlySpent / monthlyBudget).toFloat() else 0f
    val displayPercentage = (percentage * 100).roundToInt()

    val progressColor = when {
        percentage >= 1.0f -> MaterialTheme.colorScheme.error
        percentage >= 0.8f -> Color(0xFFFFB800)
        else -> Color(0xFF4CAF50)
    }

    Card(
        shape = RoundedCornerShape(padding),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.monthly_spent),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
                TextButton(
                    onClick = { showBudgetDialog = true },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(if (hasBudget) "Edit Budget" else "Set Budget", fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                if (hasBudget) {
                    Text(
                        text = "${CurrencyManager.formatPrice(monthlySpent, currentCurrency)} / ${CurrencyManager.formatPrice(monthlyBudget, currentCurrency)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$displayPercentage%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                } else {
                    Text(
                        text = CurrencyManager.formatPrice(monthlySpent, currentCurrency),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            // Progress Bar (Only when budget is set)
            if (hasBudget) {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(50))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(percentage.coerceIn(0f, 1f))
                            .background(progressColor, RoundedCornerShape(50))
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Bottom Row: Yearly Total & Active Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${CurrencyManager.formatPrice(yearlySpent, currentCurrency)} ${stringResource(R.string.per_year)}",
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = "$activeCount active",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showBudgetDialog) {
        var inputBudget by remember { mutableStateOf(if (hasBudget) monthlyBudget.toString() else "") }
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text(if (hasBudget) "Edit Monthly Budget" else "Set Monthly Budget", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = inputBudget,
                    onValueChange = { input ->
                        inputBudget = input.filterIndexed { index, char ->
                            char.isDigit() || (char == '.' && input.indexOf('.') == index)
                        }
                    },
                    label = { Text("Target Budget ($)") },
                    placeholder = { Text("Enter target budget (e.g. 300)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = inputBudget.toDoubleOrNull() ?: 0.0
                        BudgetManager.setBudget(context, parsed)
                        showBudgetDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (hasBudget) {
                            BudgetManager.setBudget(context, 0.0)
                        }
                        showBudgetDialog = false
                    }
                ) {
                    Text(if (hasBudget) "Clear Budget" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun CategoryBreakdown(categorySpends: List<CategorySpend>) {
    if (categorySpends.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.by_category),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
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
                Text(
                    text = "${spend.category.toTranslatedString()} - ${percentage}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
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
    val bg = if (isActive) Color(0xFFD0F0D0) else colorScheme.error
    val fg = if (isActive) Color(0xFF4CAF50) else colorScheme.onError

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (isActive) stringResource(R.string.active) else stringResource(R.string.inactive), // TRANSLATED
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
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

                val translatedSubtitle = when {
                    row.subtitle == "Renews today" -> stringResource(R.string.renews_today)
                    row.subtitle == "Renews tomorrow" -> stringResource(R.string.renews_tomorrow)
                    row.subtitle.startsWith("Renews in") -> {
                        val days = row.subtitle.filter { it.isDigit() }.toIntOrNull() ?: 0
                        stringResource(R.string.renews_in_days, days)
                    }
                    else -> row.subtitle
                }

                Text(text = translatedSubtitle, fontSize = 12.sp)
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
@Composable
private fun Category.toTranslatedString(): String {
    return when (this) {
        Category.STREAMING -> stringResource(R.string.category_streaming)
        Category.PRODUCTIVITY -> stringResource(R.string.category_productivity)
        Category.GAMES -> stringResource(R.string.category_games)
        Category.FOOD -> stringResource(R.string.category_food)
        else -> ""
    }
}