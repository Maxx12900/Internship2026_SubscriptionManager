package com.example.subscriptionmanager

import android.app.usage.UsageStats
import android.content.Context
import android.icu.util.Calendar
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.example.subscriptionmanager.data.getApplicationUsageData
import com.example.subscriptionmanager.notifications.createNotification
import com.example.subscriptionmanager.notifications.createNotificationChannel
import com.example.subscriptionmanager.notifications.showNotification
import com.example.subscriptionmanager.ui.theme.SubscriptionManagerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getInstalledApps(context: Context): Array<String> {
    val packageManager = context.packageManager
    val packages = packageManager.getInstalledPackages(0)
    return packages.map { it.packageName }.toTypedArray()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        createNotificationChannel(this)

        val testNotification = createNotification(this, "test", "I'm a text!")

        setContent {
            SubscriptionManagerTheme {
                SubscriptionManagerApp()
            }


            val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())

            Button(
                onClick = {
                    val usageStats = getApplicationUsageData(this)
                    println(usageStats)
                    println(usageStats.size)
                    for (usageStat: UsageStats in usageStats) {
                        if (usageStat.packageName.contains("discord")) {
                            println("Package name: " + usageStat.packageName)
                            print("Used for: ")
                            print(usageStat.totalTimeVisible / 1000)
                            print(" seconds\n")


                            print("Last time opened: ")
                            print(formatter.format(Date(usageStat.lastTimeVisible)))
                            println()
                            println("-----")
                        }
                    }
                },
                modifier = Modifier.offset(
                    x = 100.dp,
                    y = 100.dp
                )
            ) {
                Text("Request data")
            }
        }

        showNotification(this, testNotification)
        getApplicationUsageData(this)
    }
}

@PreviewScreenSizes
@Composable
fun SubscriptionManagerApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Greeting(
                name = "Android",
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Home", R.drawable.ic_home),
    FAVORITES("Favorites", R.drawable.ic_favorite),
    PROFILE("Profile", R.drawable.ic_account_box),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SubscriptionManagerTheme {
        Greeting("Android")
    }
}