package com.example.subscriptionmanager

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.subscriptionmanager.credentials.LoginScreen
import com.example.subscriptionmanager.credentials.SignUpScreen
import com.example.subscriptionmanager.navigation.AppNavGraph
import com.example.subscriptionmanager.notifications.ChannelIds
import com.example.subscriptionmanager.notifications.createNotificationChannel
import com.example.subscriptionmanager.ui.theme.SubscriptionManagerTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannels(this)

        setContent {
            SubscriptionManagerTheme {
                val nav = rememberNavController()
                NavHost(nav, startDestination = "login") {
                    composable("login") {
                        LoginScreen(
                            onLoggedIn = { nav.navigate("home") { popUpTo("login") { inclusive = true } } },
                            onCreateAccount = { nav.navigate("signup") },
                        )
                    }
                    composable("signup") {
                        SignUpScreen(
                            onAccountCreated = { nav.navigate("home") { popUpTo("login") { inclusive = true } } },
                            onLogIn = { nav.popBackStack() },
                        )
                    }
                    composable("home") { AppNavGraph() }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun createNotificationChannels(context: Context) {
    createNotificationChannel(
        context,
        "Renewal Reminder",
        "Will remind the user N days in advance before renewals",
        ChannelIds.RENEWAL
    )
}