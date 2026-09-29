package com.example.subscriptionmanager

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import com.example.subscriptionmanager.navigation.AppNavGraph
import com.example.subscriptionmanager.ui.theme.SubscriptionManagerTheme

class NotificationActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val payload = intent.getStringExtra("NOTIFICATION_PAYLOAD")

        setContent {
            SubscriptionManagerTheme {
                Card {
                    Text(text = payload ?: "No data received")
                }
            }
        }
    }
}