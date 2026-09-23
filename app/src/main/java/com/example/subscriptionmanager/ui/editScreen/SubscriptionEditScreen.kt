package com.example.subscriptionmanager.ui.editScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SubscriptionEditScreen(
    subscriptionName: String,
    viewModel: SubscriptionEditViewModel = viewModel(),
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    LaunchedEffect(subscriptionName) {
        viewModel.loadSubscription(subscriptionName)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = "Edit Subscription",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 48.dp)
                )
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            InputField("SUBSCRIPTION NAME", viewModel.name) { viewModel.name = it }
            Spacer(modifier = Modifier.height(20.dp))

            InputField("CATEGORY", viewModel.category) { viewModel.category = it }
            Spacer(modifier = Modifier.height(20.dp))

            InputField("PRICE", viewModel.price) { viewModel.price = it }
            Spacer(modifier = Modifier.height(20.dp))

            InputField("BILLING PERIOD", viewModel.billingPeriod) { viewModel.billingPeriod = it }
            Spacer(modifier = Modifier.height(20.dp))

            InputField("NEXT RENEWAL DATE", viewModel.nextRenewalDate) { viewModel.nextRenewalDate = it }
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    viewModel.saveSubscription(subscriptionName)
                    onSave()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E5CE6))
            ) {
                Text(
                    text = "Save Subscription",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
@Composable
fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8F8F8), shape = RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            singleLine = true
        )
    }
}