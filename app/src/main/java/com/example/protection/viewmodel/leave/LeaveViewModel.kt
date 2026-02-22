package com.example.protection.viewmodel.leave

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.protection.data.local.entity.LeaveEntity
import com.example.protection.data.repository.LeaveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeaveState(
    val leaves: List<LeaveEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LeaveViewModel @Inject constructor(
    private val repository: LeaveRepository
): ViewModel() {

    private val _leaveState = MutableStateFlow(LeaveState())
    val leaveState: StateFlow<LeaveState> = _leaveState.asStateFlow()

    // Backward Compatibility (Exposes just the list for your UI)
    val allLeaves: StateFlow<List<LeaveEntity>> = _leaveState
        .map { it.leaves }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadLeaves()
    }

    fun loadLeaves() {
        viewModelScope.launch {
            _leaveState.update { it.copy(isLoading = true) }
            try {
                // 🔹 FIX: Collect the list directly (Room returns List, not Resource)
                repository.getAllLeaves().collect { list ->
                    _leaveState.update {
                        it.copy(
                            isLoading = false,
                            leaves = list,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _leaveState.update {
                    it.copy(isLoading = false, error = e.message ?: "Unknown Error")
                }
            }
        }
    }

    fun updateLeaveStatus(leave: LeaveEntity, newStatus: String) {
        viewModelScope.launch {
            try {
                // Optimistic Update (Update UI instantly)
                _leaveState.update { current ->
                    val updatedList = current.leaves.map {
                        if (it.employeeId == leave.employeeId && it.requestDate == leave.requestDate) {
                            it.copy(status = newStatus)
                        } else it
                    }
                    current.copy(leaves = updatedList)
                }
                // Update Database & Cloud
                repository.updateLeaveStatus(leave.copy(status = newStatus))
            } catch (e: Exception) {
                loadLeaves() // Revert if failed
            }
        }
    }

    fun submitLeave(
        employeeId: String,
        employeeName: String,
        startDate: Long,
        endDate: Long,
        reason: String
    ) {
        viewModelScope.launch {
            _leaveState.update { it.copy(isLoading = true) }
            try {
                val newLeave = LeaveEntity(
                    employeeId = employeeId,
                    employeeName = employeeName,
                    startDate = startDate,
                    endDate = endDate,
                    reason = reason,
                    status = "PENDING",
                    requestDate = System.currentTimeMillis()
                )
                repository.submitLeaveRequest(newLeave)
                _leaveState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _leaveState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun getMyLeaveHistory(employeeId: String): Flow<List<LeaveEntity>> {
        return repository.getEmployeeLeaves(employeeId)
    }

    // 🔹 This is the new sync function for Admin
    fun syncAllLeavesForAdmin() {
        viewModelScope.launch {
            _leaveState.update { it.copy(isLoading = true) }
            try {
                repository.syncAllLeavesFromFirestore()
                // No need to update 'leaves' manually here.
                // Since 'loadLeaves' is observing the Room DB,
                // as soon as sync writes to DB, the UI will update automatically!
                _leaveState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _leaveState.update { it.copy(isLoading = false, error = "Sync failed: ${e.message}") }
            }
        }


    }
    // 🔹 Restored: Sync function for individual Employees
    fun refreshLeaves(employeeId: String) {
        viewModelScope.launch {
            _leaveState.update { it.copy(isLoading = true) }
            try {
                repository.syncLeavesFromFirestore(employeeId)
                _leaveState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _leaveState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

}