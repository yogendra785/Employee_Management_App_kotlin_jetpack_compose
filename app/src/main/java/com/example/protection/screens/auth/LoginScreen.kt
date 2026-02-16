package com.example.protection.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.protection.R
import com.example.protection.viewmodel.auth.AuthState
import com.example.protection.viewmodel.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onLoginSuccess()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 🔹 LOGO SECTION (Replaced Icon with Image)
                Image(
                    painter = painterResource(id = R.drawable.logo2), // Your custom logo
                    contentDescription = "24 Protection Logo",
                    modifier = Modifier.size(120.dp) // Good size for a main logo
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "24 PROTECTION", // Uppercase looks more like a security brand
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Secure Personnel Portal",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Company Email or ID") },
                            placeholder = { Text("guard@24protection.com") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email"
                                )
                            },
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Password"
                                )
                            },
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, contentDescription = null)
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (authState is AuthState.Loading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = { authViewModel.login(email, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = email.isNotBlank() && password.isNotBlank(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Secure Login", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                // Error Message Display
                if (authState is AuthState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = (authState as AuthState.Error).message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "Restricted Access • Authorized Only",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}