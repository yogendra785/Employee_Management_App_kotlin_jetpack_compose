package com.example.protection.viewmodel.site

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.protection.data.repository.EmployeeRepository
import com.example.protection.data.repository.SiteRepository
import com.example.protection.domain.model.Employee
import com.example.protection.domain.model.Site
import com.example.protection.utils.Resource // 🔹 Import Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SiteDetailViewModel @Inject constructor(
    private val siteRepository: SiteRepository,
    private val employeeRepository: EmployeeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val siteId: String = checkNotNull(savedStateHandle["siteId"])

    private val _site = MutableStateFlow<Site?>(null)
    val site: StateFlow<Site?> = _site.asStateFlow()

    // Guards currently working at THIS site
    private val _deployedGuards = MutableStateFlow<List<Employee>>(emptyList())
    val deployedGuards: StateFlow<List<Employee>> = _deployedGuards.asStateFlow()

    // Guards available to be added (Not at this site)
    private val _availableGuards = MutableStateFlow<List<Employee>>(emptyList())
    val availableGuards: StateFlow<List<Employee>> = _availableGuards.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // 1. Get Site Details
            val fetchedSite = siteRepository.getSiteById(siteId)
            _site.value = fetchedSite

            // 2. Get All Employees and Filter
            if (fetchedSite != null) {
                refreshGuardLists(fetchedSite.name)
            }
        }
    }

    private suspend fun refreshGuardLists(siteName: String) {
        // 🔹 FIX: Unwrap the Resource wrapper!
        val result = employeeRepository.getAllEmployees().first()

        // Extract the list safely. If loading or error, default to empty list.
        val allEmployees = result.data ?: emptyList()

        // Filter: Who is here vs Who is not
        _deployedGuards.value = allEmployees.filter { it.department == siteName }

        // Available = Active guards who are NOT already here
        _availableGuards.value = allEmployees.filter {
            it.department != siteName && it.isActive && it.role != "ADMIN"
        }
    }

    fun assignGuard(employee: Employee) {
        val currentSite = _site.value ?: return
        viewModelScope.launch {
            // Update the employee's department to this Site Name
            val updatedEmployee = employee.copy(department = currentSite.name)
            employeeRepository.updateEmployee(updatedEmployee)

            // Refresh lists
            refreshGuardLists(currentSite.name)
        }
    }

    fun removeGuard(employee: Employee) {
        val currentSite = _site.value ?: return
        viewModelScope.launch {
            // Clear the department (Unassign)
            val updatedEmployee = employee.copy(department = "")
            employeeRepository.updateEmployee(updatedEmployee)

            // Refresh lists
            refreshGuardLists(currentSite.name)
        }
    }
}