package com.example.subscriptionmanager.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.subscriptionmanager.ui.home.HomeScreen
import com.example.subscriptionmanager.ui.SettingsScreen.SettingsScreen
import com.example.subscriptionmanager.ui.subscriptionAdd.SubscriptionAddScreen
import com.example.subscriptionmanager.ui.subscriptionDetails.SubscriptionDetailsScreen
import com.example.subscriptionmanager.ui.subscriptionList.SubscriptionListScreen
import com.example.subscriptionmanager.ui.analytics.AnalyticsScreen // Add import

sealed class Screen(val route: String) {
    object Home                 : Screen("home")
    object SubscriptionList     : Screen("subscription_list")
    object SubscriptionAdd      : Screen("add_subscription")
    object Analytics            : Screen("analytics")
    object Settings             : Screen("settings")
    object SubscriptionDetails  : Screen("subscription_details/{id}") {
        fun createRoute(id: Int) = "subscription_details/$id"
    }
    object SubscriptionEdit     : Screen("subscription_details/{id}/edit") {
        fun createRoute(id: Int) = "subscription_details/$id/edit"
    }
}

@Composable
fun AppNavGraph(onLogOut:() -> Unit = {}) {
    val navController: NavHostController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val topLevelRoutes = listOf(
        Screen.Home.route,
        Screen.SubscriptionList.route,
        Screen.Analytics.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in topLevelRoutes) {
                AppBottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Home Screen
            composable(Screen.Home.route) {
                HomeScreen(
                    onSubscriptionClick = { id ->
                        navController.navigate(Screen.SubscriptionDetails.createRoute(id))
                    }
                )
            }

            // Subscription list screen
            composable(Screen.SubscriptionList.route) {
                SubscriptionListScreen(
                    onAddClick = { navController.navigate(Screen.SubscriptionAdd.route) },
                    onSubscriptionClick = { id ->
                        navController.navigate(Screen.SubscriptionDetails.createRoute(id))
                    }
                )
            }

            // Analytics screen
            composable(Screen.Analytics.route) {
                AnalyticsScreen()
            }

            // Settings screen
            composable(Screen.Settings.route) {}
            // Settings screen
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onLogOut = onLogOut
                )
            }

            // Subscription Add screen
            composable(Screen.SubscriptionAdd.route) {
                SubscriptionAddScreen(
                    onSave = {
                        if (navController.currentDestination?.route == Screen.SubscriptionAdd.route) {
                            navController.popBackStack()
                        }
                    },
                    onCancel = {
                        if (navController.currentDestination?.route == Screen.SubscriptionAdd.route) {
                            navController.popBackStack()
                        }
                    }
                )
            }

            // Subscription Details screen
            composable(
                route = Screen.SubscriptionDetails.route,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: -1
                SubscriptionDetailsScreen(
                    subscriptionId = id,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Screen.SubscriptionEdit.createRoute(id)) },
                    onDelete = { navController.popBackStack() }
                )
            }

            // Subscription Edit Screen
            composable(
                route = Screen.SubscriptionEdit.route,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: -1
                SubscriptionAddScreen(
                    subscriptionId = id,
                    onSave = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
        }
    }
}