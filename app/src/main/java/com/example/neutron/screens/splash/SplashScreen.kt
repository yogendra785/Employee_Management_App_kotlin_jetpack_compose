package com.example.neutron.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.neutron.navigation.NavRoutes
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {

    // LaunchedEffect handles the initialization logic once the screen is displayed
    LaunchedEffect(key1 = Unit) {
        // A short delay to allow the branding to be seen (standard UX practice)
        delay(2000)

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // User is authenticated, move to main app
            navController.navigate("main_app") {
                // Critical: Clear the Splash screen from the backstack so the user
                // doesn't return to it when pressing 'Back' from the Dashboard.
                popUpTo(NavRoutes.SPLASH) { inclusive = true }
            }
        } else {
            // No session found, send to Login
            navController.navigate(NavRoutes.LOGIN) {
                popUpTo(NavRoutes.SPLASH) { inclusive = true }
            }
        }
    }

    // Branding UI using your Material Theme primary color
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "NEUTRON",
            color = Color.White,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp // Added for a more premium "brand" feel
        )
    }
}