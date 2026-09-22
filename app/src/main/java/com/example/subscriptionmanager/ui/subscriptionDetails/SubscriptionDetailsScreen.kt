package com.example.subscriptionmanager.ui.subscriptionDetails

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subscriptionmanager.ui.components.AppIcon

@Composable
fun SubscriptionDetailsScreen(
    subscriptionName: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    viewModel: SubscriptionDetailsViewModel = viewModel()
) {
    val subscription by viewModel.subscription.collectAsState()

    LaunchedEffect(subscriptionName) {
        viewModel.loadSubscription(subscriptionName)
    }

    subscription?.let { sub ->
        Scaffold(
            containerColor = Color(0xFFFBFBFF),
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp), // Tightened spacing
                contentPadding = PaddingValues(top = 0.dp, bottom = 32.dp) // Content moved up
            ) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.offset(x = (-12).dp, y = 8.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text(
                            text = sub.name,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 2.dp,
                            color = Color.White
                        ) {
                            AppIcon(
                                packageName = sub.packageName,
                                fallbackLetter = sub.name.take(1),
                                modifier = Modifier.size(80.dp).padding(12.dp)
                            )
                        }
                        Spacer(Modifier.width(20.dp))
                        Column {
                            Text(sub.category, color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = if (sub.isActive) "Active" else "Inactive",
                                color = if (sub.isActive) Color(0xFF4CAF50) else Color.Red,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            InfoCard("PRICE", "$${sub.price} / mo", Modifier.weight(1f))
                            InfoCard("NEXT RENEWAL", "Oct 1, 2026", Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            InfoCard("BILLING PERIOD", "Monthly", Modifier.weight(1f))
                            InfoCard("PAYMENT METHOD", "Visa •4821", Modifier.weight(1f))
                        }
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Usage", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0FF)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(Modifier.padding(20.dp)) {
                                Text("Last marked as used: 2 days ago")
                                Button(
                                    onClick = {},
                                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E5CE6)),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    Text("Mark as used today", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F8)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFE5E5))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF6961), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Analyzer note", color = Color(0xFFFF6961), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                Text("Used less than usual this month.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Notes", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = "Shared with family plan.",
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFF9F9F9),
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    }
                }

                // 7. Action Buttons
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Button(
                            onClick = onEdit,
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5E5EA)),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("Edit", color = Color.Black) }

                        Button(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6961)),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("Delete", color = Color.White) }
                    }
                }
            }
        }
    }
}
@Composable
fun InfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F7)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}