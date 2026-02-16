package com.example.protection.screens.employee

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.protection.viewmodel.employee.AddEmployeeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmployeeScreen(
    viewModel: AddEmployeeViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onImageSelected(uri)
    }

    LaunchedEffect(key1 = uiState.isSuccess) {
        if (uiState.isSuccess) {
            onBack()
            viewModel.resetSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deploy New Guard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // --- 1. Photo & ID Section ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Photo Picker
                Box(contentAlignment = Alignment.Center) {
                    Surface(
                        onClick = { launcher.launch("image/*") },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(100.dp)
                    ) {
                        if (uiState.selectedImageUri != null) {
                            AsyncImage(
                                model = uiState.selectedImageUri,
                                contentDescription = "Profile Image",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.padding(28.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Employee ID
                OutlinedTextField(
                    value = uiState.employeeId,
                    onValueChange = { viewModel.onEmployeeIdChange(it) },
                    label = { Text("Employee ID") },
                    placeholder = { Text("e.g. 101") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    isError = uiState.idError != null,
                    supportingText = { uiState.idError?.let { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // --- 2. Personal Info ---
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // --- 3. ALLOCATED SITE (Mapped to Department Logic) ---
            // We use the existing 'department' variable but label it as 'Allocated Site'
            OutlinedTextField(
                value = uiState.department,
                onValueChange = viewModel::onDepartmentChange,
                label = { Text("📍 Allocated Site / Location") },
                placeholder = { Text("e.g. City Mall, Main Gate") },
                leadingIcon = { Icon(Icons.Default.Place, contentDescription = "Location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // --- 4. ROLE (Strictly Admin or Employee) ---
                OutlinedTextField(
                    value = uiState.role,
                    onValueChange = viewModel::onRoleChange,
                    label = { Text("System Role") },
                    placeholder = { Text("ADMIN / EMPLOYEE") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                // --- 5. Salary ---
                OutlinedTextField(
                    value = uiState.salary,
                    onValueChange = viewModel::onSalaryChange,
                    label = { Text("Salary") },
                    prefix = { Text("₹") },
                    modifier = Modifier.weight(0.8f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // --- 6. Login Details ---
            Text("Login Credentials", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email (Username)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                },
                singleLine = true
            )

            // Active Switch
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Active Status", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("User can access the app", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = uiState.isActive, onCheckedChange = viewModel::onActiveChange)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.onSaveEmployee() },
                enabled = uiState.isSaveEnabled && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Register Staff", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}