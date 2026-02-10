package com.example.neutron.data.repository

import com.example.neutron.data.local.dao.LeaveDao
import com.example.neutron.data.local.entity.LeaveEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaveRepository @Inject constructor(
    private val leaveDao: LeaveDao,
    private val firestore: FirebaseFirestore
) {

    // 1. Get all leaves (For Admin)
    fun getAllLeaves(): Flow<List<LeaveEntity>> = leaveDao.getAllLeaves()

    // 2. Get leaves for a specific employee (For Employee Dashboard)
    fun getEmployeeLeaves(employeeId: Long): Flow<List<LeaveEntity>> =
        leaveDao.getLeavesByEmployee(employeeId)

    /**
     * Submits a leave request to both local Room and Firestore.
     */
    suspend fun submitLeaveRequest(leave: LeaveEntity) = withContext(Dispatchers.IO) {
        // 🔹 Fix: Matches 'insertLeave' in your DAO
        leaveDao.insertLeave(leave)

        // Sync to Firestore
        try {
            val leaveMap = hashMapOf(
                "employeeId" to leave.employeeId,
                "employeeName" to leave.employeeName,
                "startDate" to leave.startDate,
                "endDate" to leave.endDate,
                "reason" to leave.reason,
                "status" to leave.status,
                "requestDate" to leave.requestDate
            )
            firestore.collection("leave_requests").add(leaveMap).await()
        } catch (e: Exception) {
            // Log error or handle offline state
        }
    }

    /**
     * Updates leave status in both local Room and Firestore.
     */
    suspend fun updateLeaveStatus(leave: LeaveEntity) = withContext(Dispatchers.IO) {
        // 🔹 Fix: Matches 'updateLeaveStatus' in your DAO
        leaveDao.updateLeaveStatus(leave.employeeId, leave.requestDate, leave.status)

        // Update in Firestore
        try {
            val query = firestore.collection("leave_requests")
                .whereEqualTo("employeeId", leave.employeeId)
                .whereEqualTo("requestDate", leave.requestDate)
                .get()
                .await()

            for (document in query.documents) {
                document.reference.update("status", leave.status).await()
            }
        } catch (e: Exception) {
            // Handle Firestore update error
        }
    }
}