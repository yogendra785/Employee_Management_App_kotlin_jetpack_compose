package com.example.neutron.screens.salary

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
import com.example.neutron.domain.model.Employee
import com.example.neutron.domain.model.SalaryRecord
import com.example.neutron.viewmodel.employee.SalaryViewModel
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
        Column(modifier = Modifier.padding(padding).padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Process Salaries for $currentMonth",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(employees) { employee ->
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        onClick = {
                            viewModel.resetState() // 🔹 Clean previous data before opening
                            selectedEmployee = employee
                            // Pre-fill base salary from employee record for faster processing
                            viewModel.onBaseSalaryChange(employee.salary.toString())
                            showDialog = true
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        employee.name.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(employee.name, fontWeight = FontWeight.Bold)
                                Text(employee.department, style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }

    if (showDialog && selectedEmployee != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Column {
                    Text("Payroll Processing", style = MaterialTheme.typography.titleSmall)
                    Text(selectedEmployee!!.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Base Salary
                    OutlinedTextField(
                        value = uiState.baseSalary,
                        onValueChange = viewModel::onBaseSalaryChange,
                        label = { Text("Standard Monthly Pay") },
                        prefix = { Text("₹") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // 2. Absences with Automation
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = uiState.absences,
                            onValueChange = viewModel::onAbsencesChange,
                            label = { Text("Absent Days") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalIconButton(
                            onClick = { viewModel.fetchAutomatedAbsences(selectedEmployee!!.employeeId, currentMonth) },
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = "Sync Attendance")
                        }
                    }

                    // 3. Deduction Rate
                    OutlinedTextField(
                        value = uiState.deductionRate,
                        onValueChange = viewModel::onDeductionRateChange,
                        label = { Text("Per Day Deduction") },
                        prefix = { Text("₹") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // 4. Calculations Summary
                    val totalDeduction = (uiState.absences.toIntOrNull() ?: 0) * (uiState.deductionRate.toDoubleOrNull() ?: 0.0)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Total Penalty:", style = MaterialTheme.typography.bodySmall)
                                Text("-₹$totalDeduction", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Net Payable:", fontWeight = FontWeight.Bold)
                                Text("₹${uiState.netPayable}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // 5. PDF Action
                    Button(
                        onClick = {
                            val record = SalaryRecord(
                                employeeId = selectedEmployee!!.employeeId,
                                employeeName = selectedEmployee!!.name,
                                month = currentMonth,
                                baseSalary = uiState.baseSalary.toDoubleOrNull() ?: 0.0,
                                advancePaid = uiState.advance.toDoubleOrNull() ?: 0.0,
                                absentDays = uiState.absences.toIntOrNull() ?: 0,
                                perDayDeduction = uiState.deductionRate.toDoubleOrNull() ?: 0.0,
                                netPayable = uiState.netPayable
                            )
                            viewModel.generatePdf(context, record, selectedEmployee!!.name)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Generate Salary Slip")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveSalary(selectedEmployee!!.employeeId,
                            month = currentMonth,
                            employeeName = selectedEmployee!!.name)
                        showDialog = false
                    }
                ) { Text("Save & Record") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Close") }
            }
        )
    }
}