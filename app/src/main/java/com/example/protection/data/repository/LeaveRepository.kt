package com.example.protection.data.repository

import android.util.Log
import com.example.protection.data.local.dao.LeaveDao
import com.example.protection.data.local.entity.LeaveEntity
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

    fun getAllLeaves(): Flow<List<LeaveEntity>> = leaveDao.getAllLeaves()

    fun getEmployeeLeaves(employeeId: String): Flow<List<LeaveEntity>> =
        leaveDao.getLeavesByEmployee(employeeId)

    suspend fun submitLeaveRequest(leave: LeaveEntity) = withContext(Dispatchers.IO) {
        leaveDao.insertLeave(leave)
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
            Log.e("LeaveRepo", "Submit Failed: ${e.message}")
        }
    }

    suspend fun updateLeaveStatus(leave: LeaveEntity) = withContext(Dispatchers.IO) {
        leaveDao.updateLeaveStatus(leave.employeeId, leave.requestDate, leave.status)
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
            Log.e("LeaveRepo", "Update Failed: ${e.message}")
        }
    }

    // 🔹 NEW: Sync function to pull latest leaves from Cloud
    suspend fun syncLeavesFromFirestore(employeeId: String) = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("leave_requests")
                .whereEqualTo("employeeId", employeeId)
                .get()
                .await()

            val remoteLeaves = snapshot.documents.mapNotNull { doc ->
                LeaveEntity(
                    employeeId = doc.getString("employeeId") ?: return@mapNotNull null,
                    employeeName = doc.getString("employeeName") ?: "",
                    startDate = doc.getLong("startDate") ?: 0L,
                    endDate = doc.getLong("endDate") ?: 0L,
                    reason = doc.getString("reason") ?: "",
                    status = doc.getString("status") ?: "PENDING",
                    requestDate = doc.getLong("requestDate") ?: 0L
                )
            }

            // Save to local database
            remoteLeaves.forEach { leaveDao.insertLeave(it) }
        } catch (e: Exception) {
            Log.e("LeaveRepo", "Sync Leaves Failed: ${e.message}")
        }
    }
}