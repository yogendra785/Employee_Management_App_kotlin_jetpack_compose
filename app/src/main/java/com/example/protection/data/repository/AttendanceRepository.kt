package com.example.protection.data.repository

import android.util.Log
import com.example.protection.data.local.dao.AttendanceDao
import com.example.protection.data.local.entity.AttendanceEntity
// 🔹 THESE ARE THE MISSING IMPORTS causing your error:
import com.example.protection.data.mapper.toAttendance
import com.example.protection.data.mapper.toAttendanceEntity
import com.example.protection.domain.model.Attendance
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val attendanceDao: AttendanceDao,
    private val firestore: FirebaseFirestore
) {

    fun getAttendanceByDate(date: Long): Flow<List<Attendance>> =
        attendanceDao.getAttendanceByDate(date).map { entities ->
            entities.map { it.toAttendance() } // 🔹 Now valid because of the import
        }

    fun getAttendanceForEmployee(employeeId: String): Flow<List<Attendance>> =
        attendanceDao.getAttendanceForEmployee(employeeId).map { entities ->
            entities.map { it.toAttendance() } // 🔹 Now valid
        }

    suspend fun markAttendance(attendance: Attendance) {
        try {
            // 🔹 Now valid because of the import
            attendanceDao.upsertAttendance(attendance.toAttendanceEntity())

            val attendanceMap = hashMapOf(
                "employeeId" to attendance.employeeId,
                "date" to attendance.date,
                "status" to attendance.status.name
            )

            val docId = "${attendance.employeeId}_${attendance.date}"
            firestore.collection("attendance").document(docId).set(attendanceMap).await()

        } catch (e: Exception) {
            Log.e("AttendanceRepo", "Error marking attendance: ${e.message}")
        }
    }

    suspend fun syncAttendanceFromFirestore(employeeId: String) {
        try {
            val snapshot = firestore.collection("attendance")
                .whereEqualTo("employeeId", employeeId)
                .get()
                .await()

            val remoteEntities = snapshot.documents.mapNotNull { doc ->
                val empId = doc.getString("employeeId") ?: return@mapNotNull null
                val date = doc.getLong("date") ?: return@mapNotNull null
                val status = doc.getString("status") ?: "ABSENT"

                AttendanceEntity(
                    employeeId = empId,
                    date = date,
                    status = status
                )
            }

            remoteEntities.forEach { entity ->
                attendanceDao.upsertAttendance(entity)
            }
        } catch (e: Exception) {
            Log.e("AttendanceRepo", "Sync failed: ${e.message}")
        }
    }
}