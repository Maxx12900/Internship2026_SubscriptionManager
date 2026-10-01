package com.example.subscriptionmanager

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.subscriptionmanager.data.analysis.analyzeSubscription
import com.example.subscriptionmanager.data.analysis.debugPrintPackageNames
import com.example.subscriptionmanager.navigation.AppNavGraph
import com.example.subscriptionmanager.notifications.createNotification
import com.example.subscriptionmanager.notifications.createNotificationChannel
import com.example.subscriptionmanager.notifications.sendNotification
import com.example.subscriptionmanager.ui.theme.SubscriptionManagerTheme
import com.example.subscriptionmanager.util.AppThemeMode
import com.example.subscriptionmanager.util.ThemeManager
import com.example.subscriptionmanager.util.CurrencyManager
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                AppNavGraph()
            }
        }
    }
}