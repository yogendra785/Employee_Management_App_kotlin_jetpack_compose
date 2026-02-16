package com.example.protection.viewmodel.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.protection.data.repository.EmployeeRepository
import com.example.protection.domain.model.DashboardStats
import com.example.protection.domain.model.Employee
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class EmployeeViewModel @Inject constructor(
    private val repository: EmployeeRepository
) : ViewModel() {

    // Placeholder for the "Undo" feature to temporarily store a deleted record
    private var recentlyDeletedEmployee: Employee? = null

    // 1. Dashboard Statistics State
    private val _dashboardStats = MutableStateFlow(DashboardStats())
    val dashboardStats: StateFlow<DashboardStats> = _dashboardStats.asStateFlow()

    // 2. Reactive Employee List State
    // Using stateIn ensures the list survives configuration changes (like rotation)
    val employees: StateFlow<List<Employee>> =
        repository.getAllEmployees()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    init {
        // Initial load of stats for the current month
        loadDashboardStats()
    }

    /**
     * Refreshes dashboard metrics.
     * Called after any data change to keep the UI "Overview" accurate.
     */
    fun loadDashboardStats() {
        viewModelScope.launch {
            val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
            _dashboardStats.value = repository.getDashboardStats(currentMonth)
        }
    }

    //delete employee


    /**
     * Adds an employee. In a real-world app, we refresh stats immediately
     * so the "Total Personnel" count updates without a manual pull-to-refresh.
     */
    fun insertEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.insertEmployeeWithImage(employee, null)
            loadDashboardStats()
        }
    }

    /**
     * Deletes an employee locally. We cache them in recentlyDeletedEmployee
     * to support the "Undo" snackbar feature.
     */
    fun deleteEmployee(employee: Employee) {
        viewModelScope.launch {
            recentlyDeletedEmployee = employee
            repository.deleteEmployee(employee)
            loadDashboardStats()
        }
    }

    /**
     * Restores the last deleted employee.
     */
    fun undoDelete() {
        recentlyDeletedEmployee?.let { employee ->
            viewModelScope.launch {
                repository.insertEmployeeWithImage(employee, null)
                recentlyDeletedEmployee = null
                loadDashboardStats()
            }
        }
    }

    /**
     * Toggles Active/Inactive status.
     * Updates both the database and the dashboard stats.
     */
    fun toggleEmployeeStatus(employee: Employee) {
        viewModelScope.launch {
            repository.updateEmployeeStatus(
                id = employee.employeeId,
                isActive = !employee.isActive
            )
            loadDashboardStats()
        }
    }

    /**
     * Fetches details for a single employee (used in Detail Screen).
     */
    fun getEmployeeById(id: String): Flow<Employee?> {
        return repository.getEmployeeById(id)
    }

    /**
     * Explicitly pushes local data to Firestore.
     * In a production environment, this ensures the Admin's cloud backup is current.
     */
    fun triggerCloudSync() {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        user?.let {
            viewModelScope.launch {
                repository.syncEmployeesToCloud(it.uid)
            }
        }
    }
}