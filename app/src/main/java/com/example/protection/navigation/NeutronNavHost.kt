package com.example.protection.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import com.example.protection.viewmodel.auth.AuthState
import com.example.protection.viewmodel.auth.AuthViewModel

@Composable
fun NeutronNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.collectAsState()

    // 🔹 Reactive Navigation: Responds immediately to login/logout events
    LaunchedEffect(authState) {
        when (authState) {
            // 🟢 FIXED: Changed 'Success' to 'Authenticated' to match your ViewModel
            is AuthState.Authenticated -> {
                navController.navigate(NavRoutes.MAIN_APP) {
                    // 1. Remove Splash and Login from history so user can't go back to them
                    popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    // 2. Ensure we don't create multiple copies of the main app
                    launchSingleTop = true
                }
            }
            is AuthState.Unauthenticated -> {
                navController.navigate(NavRoutes.LOGIN) {
                    // 3. Clear the entire backstack on logout for security
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is AuthState.Error -> {
                // Stay on current screen to show the error message
            }
            AuthState.Loading -> {
                // Stay put while loading
            }
        }
    }

    // This calls your RootNavigationGraph which contains the actual routes
    RootNavigationGraph(navController = navController, authViewModel = authViewModel)
}
