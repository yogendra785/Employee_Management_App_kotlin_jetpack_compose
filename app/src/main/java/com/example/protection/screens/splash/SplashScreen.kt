package com.example.protection.screens.splash

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.protection.R
import com.example.protection.viewmodel.auth.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel // 🔹 REMOVED "= hiltViewModel()". Now it MUST be passed in.
) {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = { OvershootInterpolator(2f).getInterpolation(it) }
            )
        )

        delay(1500)

        // Now we are calling the check on the SHARED ViewModel
        authViewModel.checkAuthStatus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.logo2),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(150.dp)
                    .scale(scale.value)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "24 PROTECTION",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.scale(scale.value)
            )
        }
    }
}