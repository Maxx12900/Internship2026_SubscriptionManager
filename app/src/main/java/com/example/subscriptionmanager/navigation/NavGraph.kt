package com.example.subscriptionmanager.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.subscriptionmanager.ui.subscriptionList.SubscriptionListScreen

sealed class Screen(
    val route: String
) {
    object SubscriptionList     : Screen("subscription_list")
    object AddSubscription      : Screen("add_subscription")
    object SubscriptionDetails  : Screen("subscription_details/{id}") {
        fun createRoute(id: String) = "subscription_details/id"
    }
}

@Composable
fun AppNavGraph() {
    val navController : NavHostController = rememberNavController()

    // TODO: change to home screen
    NavHost(navController, startDestination = Screen.SubscriptionList.route) {
        composable(Screen.SubscriptionList.route) {
            SubscriptionListScreen(
                onAddClick = { navController.navigate(Screen.AddSubscription.route) }
            )
        }
    }
}