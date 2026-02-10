package com.example.neutron

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.neutron.navigation.NeutronNavHost
import com.example.neutron.ui.theme.NeutronTheme
import com.example.neutron.viewmodel.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            // Applies your custom Neutron Material 3 branding
            NeutronTheme {
                // The central navigation controller for the entire app session
                val navController = rememberNavController()

                /**
                 * 🔹 Using hiltViewModel() ensures that the AuthViewModel is
                 * scoped to the NavGraph/Activity and correctly injected by Hilt.
                 */
                val authViewModel: AuthViewModel = hiltViewModel()

                // The Root NavHost that manages the transition between Splash, Login, and Main App
                NeutronNavHost(
                    authViewModel = authViewModel,
                    navController = navController
                )
            }
        }
    }
}