package com.example.protection.viewmodel.leave

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.protection.data.local.entity.LeaveEntity
import com.example.protection.data.repository.LeaveRepository
import com.example.protection.utils.Resource
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

    // Backward Compatibility
    val allLeaves: StateFlow<List<LeaveEntity>> = _leaveState
        .map { it.leaves }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadLeaves()
    }

    fun loadLeaves() {
        viewModelScope.launch {
            repository.getAllLeaves().collect { result ->
                // 🔹 FIXED: Added <*> to handle Generics correctly
                when (result) {
                    is Resource.Loading<*> -> {
                        _leaveState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success<*> -> {
                        _leaveState.update {
                            it.copy(
                                isLoading = false,
                                // Explicit cast helps Kotlin compiler be sure
                                leaves = (result.data as? List<LeaveEntity>) ?: emptyList()
                            )
                        }
                    }
                    is Resource.Error<*> -> {
                        _leaveState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Unknown Error"
                            )
                        }
                    }
                }
            }
        }
    }

    fun updateLeaveStatus(leave: LeaveEntity, newStatus: String) {
        viewModelScope.launch {
            try {
                // Optimistic Update
                _leaveState.update { current ->
                    val updatedList = current.leaves.map {
                        if (it.employeeId == leave.employeeId && it.requestDate == leave.requestDate) {
                            it.copy(status = newStatus)
                        } else it
                    }
                    current.copy(leaves = updatedList)
                }
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

    fun refreshLeaves(employeeId: String) {
        viewModelScope.launch {
            repository.syncLeavesFromFirestore(employeeId)
        }
    }
}