package com.example.subscriptionmanager.navigation


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.subscriptionmanager.navigation.Screen

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(Screen.Home.route, "Home", Icons.Outlined.Home)
    object List : BottomNavItem(Screen.SubscriptionList.route, "List", Icons.Outlined.Menu)
    object Add : BottomNavItem(Screen.SubscriptionAdd.route, "Add", Icons.Outlined.Add)
    object Analytics : BottomNavItem(Screen.Analytics.route, "Analytics", Icons.Default.DateRange)
    object Settings : BottomNavItem(Screen.Settings.route, "Settings", Icons.Outlined.Settings)
}

@Composable
fun AppBottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.List,
        BottomNavItem.Add,
        BottomNavItem.Analytics,
        BottomNavItem.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF5E5CE6),
                        selectedTextColor = Color(0xFF5E5CE6),
                        unselectedIconColor = Color(0xFF8E8E93),
                        unselectedTextColor = Color(0xFF8E8E93),
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}