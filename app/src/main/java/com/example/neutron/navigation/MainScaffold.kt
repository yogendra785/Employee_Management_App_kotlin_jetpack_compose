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
    userRole: String // 🔹 Role is now passed directly from NeutronNavHost
) {
    val appNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            // 🔹 The role is passed to the BottomBar
            BottomBar(
                navController = appNavController,
                userRole = userRole
            )
        }
    ) { paddingValues ->

        NavHost(
            navController = appNavController,
            startDestination = NavRoutes.DASHBOARD,
            route = "bottom_bar_graph",
            modifier = Modifier.padding(paddingValues)
        ) {
            appNavGraph(appNavController, rootNavController, authViewModel, userRole)
        }
    }
}