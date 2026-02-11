package com.example.neutron.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomBar(
    navController: NavController,
    userRole: String
) {
    val items = remember(userRole) {
        val list = mutableListOf<BottomNavItem>()
        list.add(BottomNavItem.Home)
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
            // Highlight logic
            val isSelected = when (item) {
                BottomNavItem.Employees -> {
                    currentRoute == NavRoutes.EMPLOYEE ||
                            currentRoute == NavRoutes.ADD_EMPLOYEE ||
                            currentRoute == NavRoutes.ADMIN_LEAVE_LIST ||
                            currentRoute?.startsWith("employee_detail") == true
                }
                BottomNavItem.Attendance -> {
                    currentRoute == NavRoutes.ATTENDANCE ||
                            currentRoute == NavRoutes.LEAVE_REQUEST ||
                            currentRoute == NavRoutes.MY_LEAVE_HISTORY
                }
                else -> currentRoute == item.route
            }

            NavigationBarItem(
                selected = isSelected,
                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },


                onClick = {
                    navController.navigate(item.route) {
                        // 1. Always pop up to the start destination (Dashboard)
                        popUpTo(navController.graph.findStartDestination().id) {
                            // If it's the Home button, DON'T save the state of sub-screens (like Leave Request).
                            // We want to destroy them so we go back to a clean Dashboard.
                            saveState = (item != BottomNavItem.Home)
                        }

                        // 2. Avoid multiple copies
                        launchSingleTop = true

                        // 3. Only restore state for non-Home tabs (like Profile or Employees).
                        // For Home, we want a "Hard Reset" to the Dashboard.
                        restoreState = (item != BottomNavItem.Home)
                    }
                }
            )
        }
    }
}