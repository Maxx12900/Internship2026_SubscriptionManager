package com.example.subscriptionmanager.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.subscriptionmanager.ui.subscriptionAdd.SubscriptionAddScreen
import com.example.subscriptionmanager.ui.subscriptionDetails.SubscriptionDetailsScreen
import com.example.subscriptionmanager.ui.subscriptionList.SubscriptionListScreen

sealed class Screen(val route: String) {
    object Home                 : Screen("home")
    object SubscriptionList     : Screen("subscription_list")
    object SubscriptionAdd      : Screen("add_subscription")
    object Analytics            : Screen("analytics")
    object Settings             : Screen("settings")
    object SubscriptionDetails  : Screen("subscription_details/{id}") {
        fun createRoute(id: String) = "subscription_details/$id"
    }
    object SubscriptionEdit     : Screen("subscription_details/{id}/edit") {
        fun createRoute(id: String) = "subscription_details/$id/edit"
    }
}

@Composable
fun AppNavGraph() {
    val navController: NavHostController = rememberNavController()

    // Observe current route to show/hide the bottom navigation bar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Top-level tabs where the Bottom Navigation Bar should be visible
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
            startDestination = Screen.SubscriptionList.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Home Screen
            composable(Screen.Home.route) {

            }

            // Subscription List Screen
            composable(Screen.SubscriptionList.route) {
                SubscriptionListScreen(
                    onAddClick = { navController.navigate(Screen.SubscriptionAdd.route) },
                    onSubscriptionClick = { name ->
                        navController.navigate(Screen.SubscriptionDetails.createRoute(name))
                    }
                )
            }

            // Analytics Screen
            composable(Screen.Analytics.route) {

            }

            // Settings Screen
            composable(Screen.Settings.route) {

            }

            // Subscription Add Screen
            composable(Screen.SubscriptionAdd.route) {
                SubscriptionAddScreen(
                    onSave = { name, price, billingPeriod, nextRenewalDate ->
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

            // Subscription Details Screen
            composable(
                route = Screen.SubscriptionDetails.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                SubscriptionDetailsScreen(
                    subscriptionName = id,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Screen.SubscriptionEdit.createRoute(id)) },
                    onDelete = { navController.popBackStack() }
                )
            }
        }
    }
}