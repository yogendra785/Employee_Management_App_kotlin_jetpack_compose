package com.example.protection.viewmodel.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.protection.data.repository.EmployeeRepository
import com.example.protection.domain.model.DashboardStats
import com.example.protection.domain.model.Employee
import com.example.protection.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// 🔹 New State Class to hold UI Data cleanly
data class EmployeeListState(
    val employees: List<Employee> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EmployeeViewModel @Inject constructor(
    private val repository: EmployeeRepository
) : ViewModel() {

    private var recentlyDeletedEmployee: Employee? = null

    // 1. Dashboard Stats State
    private val _dashboardStats = MutableStateFlow(DashboardStats())
    val dashboardStats: StateFlow<DashboardStats> = _dashboardStats.asStateFlow()

    // 2. Employee List State (Now handles Loading & Errors)
    private val _employeeState = MutableStateFlow(EmployeeListState())
    val employeeState: StateFlow<EmployeeListState> = _employeeState.asStateFlow()

    // 🔹 Backward Compatibility:
    // If your UI still observes 'employees', we map it here so you don't have to rewrite your UI immediately.
    val employees: StateFlow<List<Employee>> = _employeeState
        .map { it.employees }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadEmployees()
        loadDashboardStats()
    }

    private fun loadEmployees() {
        viewModelScope.launch {
            repository.getAllEmployees().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _employeeState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _employeeState.update {
                            it.copy(isLoading = false, employees = result.data ?: emptyList())
                        }
                    }
                    is Resource.Error -> {
                        _employeeState.update {
                            it.copy(isLoading = false, error = result.message)
                        }
                    }
                }
            }
        }
    }

    fun loadDashboardStats() {
        viewModelScope.launch {
            val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

            // 🔹 Handle Resource Wrapper for Stats too
            val result = repository.getDashboardStats(currentMonth)
            if (result is Resource.Success) {
                _dashboardStats.value = result.data ?: DashboardStats()
            }
        }
    }

    fun insertEmployee(employee: Employee) {
        viewModelScope.launch {
            try {
                repository.insertEmployeeWithImage(employee, null)
                loadDashboardStats()
                // No need to reload employees manually; the Flow observer above updates automatically
            } catch (e: Exception) {
                _employeeState.update { it.copy(error = "Failed to add employee: ${e.message}") }
            }
        }
    }

    fun deleteEmployee(employee: Employee) {
        viewModelScope.launch {
            recentlyDeletedEmployee = employee
            repository.deleteEmployee(employee)
            loadDashboardStats()
        }
    }

    fun undoDelete() {
        recentlyDeletedEmployee?.let { employee ->
            viewModelScope.launch {
                repository.insertEmployeeWithImage(employee, null)
                recentlyDeletedEmployee = null
                loadDashboardStats()
            }
        }
    }

    fun toggleEmployeeStatus(employee: Employee) {
        viewModelScope.launch {
            repository.updateEmployeeStatus(
                id = employee.employeeId,
                isActive = !employee.isActive
            )
            loadDashboardStats()
        }
    }

    // 🔹 Updated to return Flow<Employee?> for Detail Screen
    // We unwrap the Resource here to keep DetailScreen logic simple for now
    fun getEmployeeById(id: String): Flow<Employee?> {
        return repository.getEmployeeById(id).map { result ->
            result.data
        }
    }

    fun triggerCloudSync() {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        user?.let {
            viewModelScope.launch {
                repository.syncEmployeesToCloud(it.uid)
            }
        }
    }
}