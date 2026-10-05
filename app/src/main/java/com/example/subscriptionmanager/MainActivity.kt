package com.example.subscriptionmanager

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.subscriptionmanager.credentials.LoginScreen
import com.example.subscriptionmanager.credentials.SignUpScreen
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.subscriptionmanager.navigation.AppNavGraph
import com.example.subscriptionmanager.notifications.ChannelIds
import com.example.subscriptionmanager.notifications.createNotificationChannel
import com.example.subscriptionmanager.ui.theme.SubscriptionManagerTheme
import com.example.subscriptionmanager.util.AppThemeMode
import com.example.subscriptionmanager.util.CurrencyManager
import com.example.subscriptionmanager.util.ThemeManager

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannels(this)

        ThemeManager.init(this)
        CurrencyManager.init(this)

        setContent {
            val themeMode by ThemeManager.themeMode.collectAsState()
            val isDark = when (themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            SubscriptionManagerTheme(darkTheme = isDark) {
                val rootNav = rememberNavController()
                NavHost(rootNav, startDestination = "login") {
                    composable("login") {
                        LoginScreen(
                            onLoggedIn = {
                                rootNav.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onCreateAccount = { rootNav.navigate("signup") }
                        )
                    }
                    composable("signup") {
                        SignUpScreen(
                            onAccountCreated = {
                                rootNav.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onLogIn = { rootNav.popBackStack() }
                        )
                    }
                    composable("home") {
                        AppNavGraph(
                            onLogOut = {
                                rootNav.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }
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