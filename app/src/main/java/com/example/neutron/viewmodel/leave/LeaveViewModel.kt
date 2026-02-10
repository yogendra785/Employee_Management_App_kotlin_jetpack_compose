package com.example.neutron.viewmodel.leave

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neutron.data.local.entity.LeaveEntity
import com.example.neutron.data.repository.LeaveRepository // 🔹 Updated Import
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaveViewModel @Inject constructor(
    private val repository: LeaveRepository // 🔹 Injected the separate LeaveRepository
): ViewModel() {

    /**
     * 1. Stream of all leaves for Admin view.
     * Observations are made through the local Room database or Firestore.
     */
    val allLeaves: Flow<List<LeaveEntity>> = repository.getAllLeaves()

    /**
     * Updates status (APPROVED/REJECTED).
     * Used by Admins on the AdminLeaveListScreen.
     */
    fun updateLeaveStatus(leave: LeaveEntity, newStatus: String) {
        viewModelScope.launch {
            // We copy the entity with the new status and pass it to the repository
            repository.updateLeaveStatus(leave.copy(status = newStatus))
        }
    }

    /**
     * Submits a new leave request.
     * Called by the Employee from the ApplyLeaveScreen.
     */
    fun submitLeave(
        employeeId: Long,
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

    /**
     * Fetches only the current logged-in user's leaves.
     */
    fun getMyLeaveHistory(employeeId: Long): Flow<List<LeaveEntity>> {
        return repository.getEmployeeLeaves(employeeId)
    }
}