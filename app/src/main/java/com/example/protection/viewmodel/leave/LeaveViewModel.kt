package com.example.protection.viewmodel.leave

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.protection.data.local.entity.LeaveEntity
import com.example.protection.data.repository.LeaveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaveViewModel @Inject constructor(
    private val repository: LeaveRepository
): ViewModel() {

    val allLeaves: Flow<List<LeaveEntity>> = repository.getAllLeaves()

    fun updateLeaveStatus(leave: LeaveEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateLeaveStatus(leave.copy(status = newStatus))
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
        }
    }

    fun getMyLeaveHistory(employeeId: String): Flow<List<LeaveEntity>> {
        return repository.getEmployeeLeaves(employeeId)
    }

    // 🔹 NEW: Trigger the sync
    fun refreshLeaves(employeeId: String) {
        viewModelScope.launch {
            repository.syncLeavesFromFirestore(employeeId)
        }
    }
}