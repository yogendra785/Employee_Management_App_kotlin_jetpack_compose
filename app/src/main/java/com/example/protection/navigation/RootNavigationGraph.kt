package com.example.protection.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.protection.screens.admin.SiteDetailScreen
import com.example.protection.screens.admin.SiteListScreen // 🔹 Make sure this is imported
import com.example.protection.screens.auth.LoginScreen
import com.example.protection.screens.splash.SplashScreen
import com.example.protection.viewmodel.auth.AuthState
import com.example.protection.viewmodel.auth.AuthViewModel



@Composable
fun RootNavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    NavHost(
        navController = navController,
        route = "root_graph",
        startDestination = NavRoutes.SPLASH
    ) {
        // 1. SPLASH SCREEN
        composable(NavRoutes.SPLASH) {
            SplashScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // 2. UNIFIED LOGIN SCREEN (Admin + Employee)
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    // Logic handled inside LoginScreen navigation usually
                }
            )
        }

        // 3. MAIN APP SCAFFOLD (Dashboard, Tabs, etc.)
        composable(NavRoutes.MAIN_APP) {
            val authState by authViewModel.authState.collectAsState()

            // Safe cast to get the role
            val role = (authState as? AuthState.Authenticated)?.role ?: "EMPLOYEE"

            MainScaffold(
                rootNavController = navController,
                authViewModel = authViewModel,
                userRole = role
            )
        }


// Inside RootNavigationGraph
        composable(
            route = NavRoutes.SITE_DETAIL,
            arguments = listOf(navArgument("siteId") { type = NavType.StringType })
        ) {
            SiteDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }

    }
}