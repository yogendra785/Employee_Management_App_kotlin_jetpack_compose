package com.example.protection.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.protection.data.local.dao.AttendanceDao
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.protection.data.local.dao.EmployeeDao
import com.example.protection.data.local.dao.SalaryDao
import com.example.protection.data.local.dao.LeaveDao
import com.example.protection.data.local.entity.LeaveEntity
import com.example.protection.data.local.entity.SalaryEntity
import com.example.protection.data.mapper.toEmployee
import com.example.protection.data.mapper.toEmployeeEntity
import com.example.protection.domain.model.AttendanceStatus
import com.example.protection.domain.model.DashboardStats
import com.example.protection.domain.model.Employee
import com.example.protection.domain.model.SalaryRecord
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class EmployeeRepository @Inject constructor(
    private val dao: EmployeeDao,
    private val salaryDao: SalaryDao,
    private val attendanceDao: AttendanceDao,
    @ApplicationContext private val context: Context,
    private val leaveDao: LeaveDao,

    private val firestore: FirebaseFirestore // 🔹 Injected is better, but local instance is fine for now
) {

    // --- Employee Core Logic ---

    fun getAllEmployees(): Flow<List<Employee>> =
        dao.getAllEmployeesFlow().map { entities -> entities.map { it.toEmployee() } }

    fun getEmployeeById(id: String): Flow<Employee?> =
        dao.getEmployeeById(id).map { it?.toEmployee() }

    suspend fun insertEmployeeWithImage(employee: Employee, imageUri: Uri?) {
        val finalPath = imageUri?.let { saveImageToInternalStorage(it) }
        val employeeToSave = if (finalPath != null) employee.copy(imagePath = finalPath) else employee
        dao.insertEmployee(employeeToSave.toEmployeeEntity())
    }

    private suspend fun saveImageToInternalStorage(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val fileName = "profile_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("EmployeeRepo", "Image Save Failed: ${e.message}")
            null
        }
    }

    suspend fun updateEmployee(employee: Employee) = dao.updateEmployee(employee.toEmployeeEntity())

    suspend fun deleteEmployee(employee: Employee) = withContext(Dispatchers.IO) {
        try {
            dao.deleteEmployee(employee.toEmployeeEntity())
            firestore.collection("users")
                .document(employee.email)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("NEUTRON_DELETE", "Deletion error: ${e.message}")
        }
    }

    suspend fun updateEmployeeStatus(id: String, isActive: Boolean) = dao.updateEmployeeStatus(id, isActive)

    // --- Salary Logic ---

    suspend fun addSalaryRecord(salary: SalaryRecord) = withContext(Dispatchers.IO) {
        val entity = SalaryEntity(
            employeeId = salary.employeeId,
            month = salary.month,
            baseSalary = salary.baseSalary,
            advancePaid = salary.advancePaid,
            absentDays = salary.absentDays,
            perDayDeduction = salary.perDayDeduction
        )
        salaryDao.insertSalary(entity)
    }

    suspend fun getAbsentCount(employeeId: String, month: String): Int = withContext(Dispatchers.IO) {
        val records = attendanceDao.getAttendanceByEmployee(employeeId).firstOrNull() ?: emptyList()
        records.count {
            it.status == AttendanceStatus.ABSENT.name && isDateInMonth(it.date, month)
        }
    }

    fun calculateNetSalary(base: Double, advance: Double, absentDays: Int, perDay: Double): Double =
        base - (absentDays * perDay) - advance

    // --- Dashboard & Sync Logic ---

    suspend fun getDashboardStats(month: String): DashboardStats = withContext(Dispatchers.IO) {
        try {
            val employees = dao.getAllEmployeesList()
            val attendance = attendanceDao.getAllAttendanceList()
            val activeOnes = employees.filter { it.isActive }
            val totalPayout = activeOnes.sumOf { it.salary }
            val monthAttendance = attendance.filter { isDateInMonth(it.date, month) }
            val rate = if (monthAttendance.isNotEmpty()) {
                (monthAttendance.count { it.status == AttendanceStatus.PRESENT.name }.toFloat() / monthAttendance.size) * 100
            } else 0f
            DashboardStats(employees.size, activeOnes.size, totalPayout, rate)
        } catch (e: Exception) {
            DashboardStats()
        }
    }

    suspend fun syncEmployeesToCloud(adminUid: String?) = withContext(Dispatchers.IO) {
        if (adminUid == null) return@withContext
        val localList = dao.getAllEmployeesList()
        localList.forEach { entity ->
            val data = hashMapOf(
                "employeeId" to entity.employeeId,
                "name" to entity.name,
                "email" to entity.email,
                "role" to entity.role,
                "password" to entity.password,
                "department" to entity.department,
                "isActive" to entity.isActive
            )
            try {
                firestore.collection("users").document(entity.email).set(data).await()
            } catch (e: Exception) {
                Log.e("NEUTRON_SYNC", "Sync Error: ${e.message}")
            }
        }
    }

    // --- 🔹 Leave Management Logic ---

    suspend fun applyForLeave(leave: LeaveEntity) = withContext(Dispatchers.IO) {
        try {
            leaveDao.insertLeave(leave)

            val leaveMap = hashMapOf(
                "employeeId" to leave.employeeId, // String
                "employeeName" to leave.employeeName,
                "startDate" to leave.startDate,
                "endDate" to leave.endDate,
                "reason" to leave.reason,
                "status" to "PENDING",
                "requestDate" to leave.requestDate
            )
            firestore.collection("leave_requests").add(leaveMap).await()
        } catch (e: Exception) {
            Log.e("LEAVE_REPO", "Failed to apply for leave: ${e.message}")
        }
    }

    fun getPendingLeaves(): Flow<List<LeaveEntity>> = callbackFlow {
        val listener = firestore.collection("leave_requests")
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshot, _ ->
                val requests = snapshot?.documents?.map { doc ->
                    LeaveEntity(
                        // 🔹 FIXED: Was getLong, now getString to match Entity
                        employeeId = doc.getString("employeeId") ?: "",
                        employeeName = doc.getString("employeeName") ?: "",
                        startDate = doc.getLong("startDate") ?: 0L,
                        endDate = doc.getLong("endDate") ?: 0L,
                        reason = doc.getString("reason") ?: "",
                        status = doc.getString("status") ?: "PENDING",
                        requestDate = doc.getLong("requestDate") ?: 0L
                    )
                } ?: emptyList()
                trySend(requests)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateLeaveStatus(leave: LeaveEntity, newStatus: String) = withContext(Dispatchers.IO) {
        try {
            val query = firestore.collection("leave_requests")
                .whereEqualTo("employeeId", leave.employeeId)
                .whereEqualTo("requestDate", leave.requestDate)
                .get()
                .await()

            for (doc in query.documents) {
                doc.reference.update("status", newStatus).await()
            }
            // 🔹 FIXED: Parameters matched DAO (String, Long, String)
            leaveDao.updateLeaveStatus(leave.employeeId, leave.requestDate, newStatus)
        } catch (e: Exception) {
            Log.e("LEAVE_REPO", "Status Update Error: ${e.message}")
        }
    }

    private fun isDateInMonth(timestamp: Long, monthYear: String): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
            .equals(monthYear, ignoreCase = true)
    }
}