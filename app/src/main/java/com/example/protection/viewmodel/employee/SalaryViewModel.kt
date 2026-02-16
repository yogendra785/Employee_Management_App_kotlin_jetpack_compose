package com.example.protection.viewmodel.employee

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.protection.data.repository.EmployeeRepository
import com.example.protection.domain.model.Employee
import com.example.protection.domain.model.SalaryRecord
import com.example.protection.utils.PdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * UI State for salary processing.
 * Default deduction rate set to 500 as per project requirements.
 */
data class SalaryUiState(
    val baseSalary: String = "",
    val absences: String = "0",
    val deductionRate: String = "500",
    val advance: String = "0",
    val netPayable: Double = 0.0,
    val isSuccess: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class SalaryViewModel @Inject constructor(
    private val repository: EmployeeRepository
) : ViewModel() {

    // 1. Reactive stream of employees for the selection list
    val employees: StateFlow<List<Employee>> = repository.getAllEmployees()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 2. State for the Payroll Dialog
    private val _salaryUiState = MutableStateFlow(SalaryUiState())
    val salaryUiState = _salaryUiState.asStateFlow()

    fun onBaseSalaryChange(value: String) {
        _salaryUiState.update { it.copy(baseSalary = value) }
        calculateNet()
    }

    fun onAdvanceChange(value: String) {
        _salaryUiState.update { it.copy(advance = value) }
        calculateNet()
    }

    fun onAbsencesChange(value: String) {
        _salaryUiState.update { it.copy(absences = value) }
        calculateNet()
    }

    fun onDeductionRateChange(value: String) {
        _salaryUiState.update { it.copy(deductionRate = value) }
        calculateNet()
    }

    /**
     * Logic for calculating net payable.
     * Formula: Base - (Absences * Rate) - Advance.
     */
    private fun calculateNet() {
        val state = _salaryUiState.value
        val base = state.baseSalary.toDoubleOrNull() ?: 0.0
        val adv = state.advance.toDoubleOrNull() ?: 0.0
        val absCount = state.absences.toIntOrNull() ?: 0
        val currentRate = state.deductionRate.toDoubleOrNull() ?: 0.0

        val net = repository.calculateNetSalary(
            base = base,
            advance = adv,
            absentDays = absCount,
            perDay = currentRate
        )

        _salaryUiState.update { it.copy(netPayable = net) }
    }

    /**
     * Saves the salary record to the local database.
     */
    fun saveSalary(employeeId: String, employeeName: String, month: String) {
        val state = _salaryUiState.value
        viewModelScope.launch {
            _salaryUiState.update { it.copy(isLoading = true) }
            val record = SalaryRecord(
                employeeId = employeeId,
                employeeName = employeeName,
                month = month,
                baseSalary = state.baseSalary.toDoubleOrNull() ?: 0.0,
                advancePaid = state.advance.toDoubleOrNull() ?: 0.0,
                absentDays = state.absences.toIntOrNull() ?: 0,
                perDayDeduction = state.deductionRate.toDoubleOrNull() ?: 0.0,
                netPayable = state.netPayable
            )
            repository.addSalaryRecord(record)
            _salaryUiState.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }

    fun resetState() {
        _salaryUiState.value = SalaryUiState()
    }

    /**
     * Production Feature: Automatically pulls absent count from the Attendance table.
     */
    fun fetchAutomatedAbsences(employeeId: String, month: String) {
        viewModelScope.launch {
            val count = repository.getAbsentCount(employeeId, month)
            onAbsencesChange(count.toString())
        }
    }

    /**
     * Generates a PDF on the IO thread to avoid stuttering in the UI.
     */
    fun generatePdf(context: Context, record: SalaryRecord, name: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val pdfGenerator = PdfGenerator(context)
                pdfGenerator.generateSalarySlip(record, name)
            }
        }
    }
}