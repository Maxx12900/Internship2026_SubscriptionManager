package com.example.subscriptionmanager.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.subscriptionmanager.ui.subscriptionAdd.SubscriptionAddScreen
import com.example.subscriptionmanager.ui.subscriptionDetails.SubscriptionDetailsScreen
import com.example.subscriptionmanager.ui.subscriptionEdit.SubscriptionEditScreen
import com.example.subscriptionmanager.ui.subscriptionList.SubscriptionListScreen

sealed class Screen(
    val route: String
) {
    object SubscriptionList     : Screen("subscription_list")
    object SubscriptionAdd      : Screen("add_subscription")
    object SubscriptionDetails  : Screen("subscription_details/{id}") {
        fun createRoute(id: String) = "subscription_details/$id"
    }
    object SubscriptionEdit     : Screen("subscription_details/{id}/edit") {
        fun createRoute(id: String) = "subscription_details/$id/edit"
    }
}

@Composable
fun AppNavGraph() {
    val navController : NavHostController = rememberNavController()

    // TODO: change to home screen
    NavHost(navController, startDestination = Screen.SubscriptionList.route) {
        composable(Screen.SubscriptionList.route) {
            SubscriptionListScreen(
                onAddClick = { navController.navigate(Screen.SubscriptionAdd.route) },
                onSubscriptionClick = { name ->
                    navController.navigate(Screen.SubscriptionDetails.createRoute(name))
                }
            )
        }
        composable(Screen.SubscriptionAdd.route) {
            SubscriptionAddScreen(
                onSave = {
                    if (navController.currentDestination?.route == Screen.SubscriptionAdd.route) {
                        navController.popBackStack()
                    }
                },
                onCancel = {
                    // Only pop backstack if we are currently on the Add screen
                    if (navController.currentDestination?.route == Screen.SubscriptionAdd.route) {
                        navController.popBackStack()
                    }
                }
            )
        }
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
        composable(
            route = Screen.SubscriptionEdit.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            SubscriptionEditScreen(
                subscriptionName = id,
                onSaveSuccess = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}