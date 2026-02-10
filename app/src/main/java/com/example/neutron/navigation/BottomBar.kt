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
    userRole: String
) {
    // 🔹 Role-Based Visibility: Items are filtered once and remembered for performance
    val items = remember(userRole) {
        val list = mutableListOf<BottomNavItem>()

        list.add(BottomNavItem.Home)

        // Only Admins get the staff management tab
        if (userRole == "ADMIN") {
            list.add(BottomNavItem.Employees)
        }

        list.add(BottomNavItem.Attendance)
        list.add(BottomNavItem.Profile)

        list
    }

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            // 🔹 Improved Selection Logic:
            // Checks if the current route is the item route OR if it's a sub-route (like Employee Detail)
            val isSelected = currentRoute == item.route ||
                    (item.route == NavRoutes.EMPLOYEE && currentRoute?.startsWith("employee_detail") == true)

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    // Standard Android Bottom Navigation behavior
                    navController.navigate(item.route) {
                        // 1. Pop up to the start destination (Dashboard) to avoid building a huge stack
                        popUpTo(NavRoutes.DASHBOARD) {
                            saveState = true
                        }
                        // 2. Avoid multiple copies of the same screen when re-clicking the tab
                        launchSingleTop = true
                        // 3. Restore state (e.g., scroll position) when re-selecting a previous tab
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(text = item.title)
                }
            )
        }
    }
}