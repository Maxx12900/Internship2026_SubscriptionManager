package com.example.subscriptionmanager.ui.subscriptionDetails

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subscriptionmanager.R
import com.example.subscriptionmanager.SubscriptionManagerApplication
import com.example.subscriptionmanager.data.entities.BillingPeriod
import com.example.subscriptionmanager.ui.common.AppIcon
import com.example.subscriptionmanager.ui.common.GenericViewModelFactory
import com.example.subscriptionmanager.util.CurrencyManager
import com.example.subscriptionmanager.util.DateTimeManager
import androidx.compose.foundation.layout.IntrinsicSize

@Composable
fun SubscriptionDetailsScreen(
    subscriptionId: Int,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as SubscriptionManagerApplication).repository
    val currentCurrency by CurrencyManager.currency.collectAsState()

    val viewModel: SubscriptionDetailsViewModel = viewModel(
        factory = GenericViewModelFactory { SubscriptionDetailsViewModel(repository) }
    )

    val subscription by viewModel.subscription.collectAsState()


    LaunchedEffect(subscriptionId) {
        viewModel.loadSubscription(subscriptionId)
    }

    val dateFormatPattern by DateTimeManager.dateFormatPattern.collectAsState()

    subscription?.let { sub ->
        val formattedDate = remember(sub.nextRenewalDate, dateFormatPattern) {
            DateTimeManager.formatDate(sub.nextRenewalDate, dateFormatPattern)
        }


        val formattedStartDate = remember(sub.startDate, dateFormatPattern) {
            DateTimeManager.formatDate(sub.startDate, dateFormatPattern)
        }

        val billingPeriodText = BillingPeriod.entries.getOrNull(sub.billingPeriod)?.toTranslatedString() ?: ""

        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(top = 0.dp, bottom = 32.dp)
            ) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.offset(x = (-12).dp, y = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = sub.name,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 2.dp,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            AppIcon(
                                packageName = sub.packageName,
                                fallbackLetter = sub.name.take(1).ifBlank { "?" },
                                modifier = Modifier.size(80.dp).padding(12.dp)
                            )
                        }
                        Spacer(Modifier.width(20.dp))
                        Column {
                            Text(
                                text = if (sub.status) stringResource(R.string.active) else stringResource(R.string.inactive),
                                color = if (sub.status) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)
                        ) {
                            InfoCard(
                                label = stringResource(R.string.price_label),
                                value = "${CurrencyManager.formatPrice(sub.price, currentCurrency)} / mo",
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                            InfoCard(
                                label = stringResource(R.string.next_renewal_label),
                                value = formattedDate,
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)
                        ) {
                            InfoCard(
                                label = stringResource(R.string.billing_period),
                                value = billingPeriodText,
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                            InfoCard(
                                label = stringResource(R.string.added),
                                value = formattedStartDate,
                                Modifier.weight(1f)
                            )
                        }
                    }
                }

/* Undecided with which design should be used
item {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = "    DESCRIPTION",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(4.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 0.dp // Keeping it flat to match InfoCards
        ) {
            Text(
                text = sub.description ?: "No description provided",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant

            )
        }
    }
}
*/

item {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.description),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = sub.description?.takeIf { it.isNotBlank() } ?: stringResource(R.string.no_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

item {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
    ) {
        OutlinedButton(
            onClick = { onEdit(sub.id) },
            modifier = Modifier.weight(1f).height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(stringResource(R.string.edit))
        }
        Button(
            onClick = {
                viewModel.deleteSubscription(onSuccess = onDelete)
            },
            modifier = Modifier.weight(1f).height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            Text(stringResource(R.string.delete))
        }
    }
}
}
}
}
}

@Composable
private fun InfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
    Column(
        modifier = Modifier
        .fillMaxHeight()
        .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(4.dp))
    Text(
        value,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    }
    }
@Composable
private fun BillingPeriod.toTranslatedString(): String {
    return when (this) {
        BillingPeriod.WEEKLY -> stringResource(R.string.weekly)
        BillingPeriod.MONTHLY -> stringResource(R.string.monthly)
        BillingPeriod.THREE_MONTHS -> stringResource(R.string.three_months)
        BillingPeriod.SIX_MONTHS -> stringResource(R.string.six_months)
        BillingPeriod.YEARLY -> stringResource(R.string.yearly)
    }
}