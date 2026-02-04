package com.example.neutron.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomBar(
    navController: NavController,
    userRole: String // 🔹 Role is now a direct parameter
) {
    // 🔹 Re-calculate the list whenever userRole changes
    val items = remember(userRole) {
        val list = mutableListOf<BottomNavItem>()

        // Home is always visible
        list.add(BottomNavItem.Home)

        // 🔹 Show Employees ONLY if userRole is ADMIN
        if (userRole == "ADMIN") {
            list.add(BottomNavItem.Employees)
        }

        // Shared features
        list.add(BottomNavItem.Attendance)
        list.add(BottomNavItem.Profile)

        list
    }

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            // 🔹 FIX 1: Better selection logic
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                selected = isSelected,
                onClick = {

                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination (DASHBOARD)
                            // to avoid building up a large stack of screens
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = item.title)
                },
                label = {
                    Text(text = item.title)
                }
            )
        }
    }
}