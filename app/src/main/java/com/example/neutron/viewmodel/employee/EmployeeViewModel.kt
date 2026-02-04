package com.example.neutron.viewmodel.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neutron.data.repository.EmployeeRepository
import com.example.neutron.domain.model.DashboardStats
import com.example.neutron.domain.model.Employee
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmployeeViewModel @Inject constructor(
    private val repository: EmployeeRepository
) : ViewModel() {

    private var recentlyDeletedEmployee: Employee? = null

    private val _dashboardStats = MutableStateFlow(DashboardStats())
    val dashboardStats: StateFlow<DashboardStats> = _dashboardStats.asStateFlow()

    val employees: StateFlow<List<Employee>> =
        repository.getAllEmployees()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    init {
        loadDashboardStats()
    }

    fun loadDashboardStats() {
        viewModelScope.launch {
            val currentMonth = java.text.SimpleDateFormat(
                "MMMM yyyy", java.util.Locale.getDefault()
            ).format(java.util.Date())
            _dashboardStats.value = repository.getDashboardStats(currentMonth)
        }
    }

    fun insertEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.insertEmployeeWithImage(employee, null)
            loadDashboardStats() // Refresh dashboard after insertion
        }
    }

    fun deleteEmployee(employee: Employee) {
        viewModelScope.launch {
            recentlyDeletedEmployee = employee
            repository.deleteEmployee(employee)
            loadDashboardStats() // Optional: refresh stats after deletion
        }
    }

    fun undoDelete() {
        recentlyDeletedEmployee?.let { employee ->
            viewModelScope.launch {
                repository.insertEmployeeWithImage(employee, null)
                recentlyDeletedEmployee = null
                loadDashboardStats() // Refresh after undo
            }
        }
    }

    fun toggleEmployeeStatus(employee: Employee) {
        viewModelScope.launch {
            repository.updateEmployeeStatus(
                id = employee.id,
                isActive = !employee.isActive
            )
            loadDashboardStats() // Refresh after status change
        }
    }

    fun getEmployeeById(id: Long): Flow<Employee?> {
        return repository.getEmployeeById(id)
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