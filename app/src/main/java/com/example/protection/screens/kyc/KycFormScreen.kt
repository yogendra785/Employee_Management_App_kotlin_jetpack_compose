package com.example.protection.screens.kyc

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.protection.utils.KycPdfGenerator
import com.example.protection.viewmodel.employee.EmployeeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycFormScreen(
    employeeId: String,
    onBack: () -> Unit,
    employeeViewModel: EmployeeViewModel = hiltViewModel()
) {
    val employees by employeeViewModel.employees.collectAsState()
    val employee = employees.find { it.employeeId == employeeId }
    val context = LocalContext.current

    // --- Form State ---
    var age by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var emergencyPhone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    // --- Document Numbers (Text Only) ---
    var aadharNum by remember { mutableStateOf("") }
    var panNum by remember { mutableStateOf("") }
    var passportNum by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate Bio-Data", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. READ-ONLY HEADER
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Employee Details", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(employee?.name ?: "Unknown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("ID: ${employee?.employeeId}", style = MaterialTheme.typography.bodyMedium)
                    Text(employee?.email ?: "", style = MaterialTheme.typography.bodySmall)
                }
            }

            Text("Personal Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))

            // 2. PERSONAL INPUT FIELDS
            OutlinedTextField(
                value = age, onValueChange = { age = it }, label = { Text("Age") },
                modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true
            )
            OutlinedTextField(
                value = phone, onValueChange = { phone = it }, label = { Text("Contact Number") },
                modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true
            )
            OutlinedTextField(
                value = emergencyPhone, onValueChange = { emergencyPhone = it }, label = { Text("Emergency Contact") },
                modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true
            )
            OutlinedTextField(
                value = address, onValueChange = { address = it }, label = { Text("Permanent Address") },
                modifier = Modifier.fillMaxWidth(), minLines = 3
            )

            Text("Identity Document Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))

            // 3. DOCUMENT NUMBER FIELDS
            OutlinedTextField(
                value = aadharNum, onValueChange = { aadharNum = it },
                label = { Text("Aadhar Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = panNum, onValueChange = { panNum = it },
                label = { Text("PAN Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = passportNum, onValueChange = { passportNum = it },
                label = { Text("Passport Number (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. GENERATE BUTTON
            Button(
                onClick = {
                    if (employee != null) {
                        // Call the Text-Only PDF Generator
                        KycPdfGenerator.generateKycPdf(
                            context = context,
                            name = employee.name,
                            email = employee.email,
                            id = employee.employeeId,
                            age = age,
                            phone = phone,
                            emergency = emergencyPhone,
                            address = address,
                            passportNum = passportNum,
                            aadharNum = aadharNum,
                            panNum = panNum
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = age.isNotEmpty() && phone.isNotEmpty() && aadharNum.isNotEmpty()
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Bio-Data PDF")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}