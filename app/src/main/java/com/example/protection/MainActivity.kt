package com.example.protection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.protection.navigation.NeutronNavHost
import com.example.protection.ui.theme.NeutronTheme
import com.example.protection.viewmodel.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            NeutronTheme {
                val navController = rememberNavController()

                // 🔹 FIX: Explicitly specify <AuthViewModel> to solve inference error
                val authViewModel = hiltViewModel<AuthViewModel>()

                NeutronNavHost(
                    authViewModel = authViewModel,
                    navController = navController
                )
            }
        }
    }
}