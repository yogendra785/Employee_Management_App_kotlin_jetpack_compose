package com.example.protection.screens.salary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.protection.domain.model.Employee
import com.example.protection.domain.model.SalaryRecord
import com.example.protection.utils.PdfGenerator
import com.example.protection.viewmodel.employee.SalaryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaryManagementScreen(
    onBackClick: () -> Unit,
    viewModel: SalaryViewModel = hiltViewModel()
) {
    val employees by viewModel.employees.collectAsState()
    val uiState by viewModel.salaryUiState.collectAsState()
    val context = LocalContext.current

    // State for Dialog
    var showDialog by remember { mutableStateOf(false) }
    var selectedEmployee by remember { mutableStateOf<Employee?>(null) }

    val currentMonth = remember {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Payroll Center", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Stats
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Processing Month",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        currentMonth,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Select Employee",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(employees) { employee ->
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            viewModel.resetState()
                            selectedEmployee = employee
                            // Auto-fill base salary
                            viewModel.onBaseSalaryChange(employee.salary.toString())
                            showDialog = true
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        employee.name.take(1).uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(employee.name, fontWeight = FontWeight.Bold)
                                Text(employee.department, style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Processing Dialog ---
    if (showDialog && selectedEmployee != null) {
        val emp = selectedEmployee!!

        // Calculate totals locally for immediate UI feedback if not in VM
        val absentDays = uiState.absences.toIntOrNull() ?: 0
        val deductionRate = uiState.deductionRate.toDoubleOrNull() ?: 0.0
        val totalPenalty = absentDays * deductionRate
        val baseSalary = uiState.baseSalary.toDoubleOrNull() ?: 0.0
        val advance = uiState.advance.toDoubleOrNull() ?: 0.0
        val netPayable = (baseSalary - totalPenalty - advance).coerceAtLeast(0.0)

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Column {
                    Text("Process Salary", style = MaterialTheme.typography.labelLarge)
                    Text(emp.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Base Salary Input
                    OutlinedTextField(
                        value = uiState.baseSalary,
                        onValueChange = viewModel::onBaseSalaryChange,
                        label = { Text("Base Salary") },
                        prefix = { Text("₹ ") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    // 2. Automated Attendance Fetch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.absences,
                            onValueChange = viewModel::onAbsencesChange,
                            label = { Text("Absent Days") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalIconButton(
                            onClick = {
                                // Trigger automated fetch
                                viewModel.fetchAutomatedAbsences(emp.employeeId, currentMonth)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(56.dp) // Match height of TextField
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = "Auto Sync")
                        }
                    }

                    // 3. Deduction Rate
                    OutlinedTextField(
                        value = uiState.deductionRate,
                        onValueChange = viewModel::onDeductionRateChange,
                        label = { Text("Deduction Per Day") },
                        prefix = { Text("₹ ") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    // 4. Advance
                    OutlinedTextField(
                        value = uiState.advance,
                        onValueChange = viewModel::onAdvanceChange,
                        label = { Text("Advance Paid") },
                        prefix = { Text("₹ ") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 5. Summary Box
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Penalty:", style = MaterialTheme.typography.bodyMedium)
                                Text("-₹${String.format("%.2f", totalPenalty)}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Net Payable:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("₹${String.format("%.2f", netPayable)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 6. PDF Button
                    Button(
                        onClick = {
                            val record = SalaryRecord(
                                employeeId = emp.employeeId,
                                employeeName = emp.name,
                                month = currentMonth,
                                baseSalary = baseSalary,
                                advancePaid = advance,
                                absentDays = absentDays,
                                perDayDeduction = deductionRate,
                                netPayable = netPayable
                            )
                            // Call the corrected PdfGenerator
                            val generator = PdfGenerator(context)
                            generator.generateSalarySlip(record, emp.name)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate PDF Slip")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveSalary(emp.employeeId, currentMonth, emp.name)
                        showDialog = false
                    }
                ) {
                    Text("Save Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}