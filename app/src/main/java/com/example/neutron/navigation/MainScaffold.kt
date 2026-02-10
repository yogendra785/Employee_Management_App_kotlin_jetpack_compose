package com.example.neutron.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.neutron.viewmodel.auth.AuthViewModel

@Composable
fun MainScaffold(
    rootNavController: NavHostController,
    authViewModel: AuthViewModel,
    userRole: String
) {
    // This controller manages the movement between screens inside the scaffold (e.g., Dashboard to Profile)
    val appNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            // Role-based BottomBar: Shows different tabs for ADMIN vs EMPLOYEE
            BottomBar(
                navController = appNavController,
                userRole = userRole
            )
        }
    ) { paddingValues ->

        // Internal Navigation Host
        NavHost(
            navController = appNavController,
            startDestination = NavRoutes.DASHBOARD,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Injects the verified appNavGraph we cleaned in the previous step
            appNavGraph(
                appNavController = appNavController,
                rootNavController = rootNavController,
                authViewModel = authViewModel,
                userRole = userRole
            )
        }
    }
}