package com.example.protection.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.protection.data.local.dao.AttendanceDao
import com.example.protection.data.local.dao.EmployeeDao
import com.example.protection.data.local.dao.LeaveDao
import com.example.protection.data.local.dao.SalaryDao
import com.example.protection.data.local.entity.LeaveEntity
import com.example.protection.data.local.entity.SalaryEntity
import com.example.protection.data.mapper.toEmployee
import com.example.protection.data.mapper.toEmployeeEntity
import com.example.protection.domain.model.AttendanceStatus
import com.example.protection.domain.model.DashboardStats
import com.example.protection.domain.model.Employee
import com.example.protection.domain.model.SalaryRecord
import com.example.protection.utils.ImageUtils
import com.example.protection.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
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
    private val firestore: FirebaseFirestore
) {

    // --- 🛡️ SAFE FETCH: Get All Employees ---
    fun getAllEmployees(): Flow<Resource<List<Employee>>> = flow {
        emit(Resource.Loading<List<Employee>>())
        try {
            dao.getAllEmployeesFlow().collect { entities ->
                val employees = entities.map { it.toEmployee() }
                emit(Resource.Success(employees))
            }
        } catch (e: Exception) {
            // 🔹 FIX: Added <List<Employee>>
            emit(Resource.Error<List<Employee>>("Failed to load staff: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    // --- 🛡️ SAFE FETCH: Get Single Employee ---
    fun getEmployeeById(id: String): Flow<Resource<Employee>> = flow {
        emit(Resource.Loading<Employee>())
        try {
            dao.getEmployeeById(id).collect { entity ->
                if (entity != null) {
                    emit(Resource.Success(entity.toEmployee()))
                } else {
                    // 🔹 FIX: Added <Employee>
                    emit(Resource.Error<Employee>("Employee not found"))
                }
            }
        } catch (e: Exception) {
            // 🔹 FIX: Added <Employee>
            emit(Resource.Error<Employee>("Error: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    // --- Standard Suspend Functions ---

    suspend fun insertEmployeeWithImage(employee: Employee, imageUri: Uri?) = withContext(Dispatchers.IO) {
        try {
            val finalPath = imageUri?.let { saveImageToInternalStorage(it) }
            val employeeToSave = if (finalPath != null) employee.copy(imagePath = finalPath) else employee
            dao.insertEmployee(employeeToSave.toEmployeeEntity())
        } catch (e: Exception) {
            Log.e("REPO", "Insert Failed: ${e.message}")
            throw e
        }
    }
// In EmployeeRepository.kt

    private suspend fun saveImageToInternalStorage(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val fileName = "profile_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)

            val compressedBytes = ImageUtils.compressImage(context, uri)

            if (compressedBytes != null) {
                file.outputStream().use { output ->
                    output.write(compressedBytes)
                }
                file.absolutePath
            } else {
                null // Compression failed
            }
        } catch (e: Exception) {
            Log.e("EmployeeRepo", "Image Save Failed: ${e.message}")
            null
        }
    }

    suspend fun updateEmployee(employee: Employee) = dao.updateEmployee(employee.toEmployeeEntity())

    suspend fun deleteEmployee(employee: Employee) = withContext(Dispatchers.IO) {
        try {
            // 1. Delete the core employee record from local Room
            dao.deleteEmployee(employee.toEmployeeEntity())

            // 🛡️ 2. CLEANUP: Delete their history from other tables
            // Ensure these methods exist in your AttendanceDao and SalaryDao!
            attendanceDao.deleteAttendanceByEmployee(employee.employeeId)
            salaryDao.deleteSalaryByEmployee(employee.employeeId)

            // 3. Delete from Cloud (Firestore)
            firestore.collection("users")
                .document(employee.email)
                .delete()
                .await()

            Log.d("NEUTRON_DELETE", "Successfully wiped all records for: ${employee.name}")
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

    suspend fun getDashboardStats(month: String): Resource<DashboardStats> = withContext(Dispatchers.IO) {
        try {
            val employees = dao.getAllEmployeesList()
            val attendance = attendanceDao.getAllAttendanceList()
            val activeOnes = employees.filter { it.isActive }
            val totalPayout = activeOnes.sumOf { it.salary }
            val monthAttendance = attendance.filter { isDateInMonth(it.date, month) }
            val rate = if (monthAttendance.isNotEmpty()) {
                (monthAttendance.count { it.status == AttendanceStatus.PRESENT.name }.toFloat() / monthAttendance.size) * 100
            } else 0f

            Resource.Success(DashboardStats(employees.size, activeOnes.size, totalPayout, rate))
        } catch (e: Exception) {
            // 🔹 FIX: Added <DashboardStats>
            Resource.Error<DashboardStats>("Could not calculate stats: ${e.message}")
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

    // --- 🛡️ SAFE FETCH: Pending Leaves (Firestore) ---

    fun getPendingLeaves(): Flow<Resource<List<LeaveEntity>>> = callbackFlow {
        trySend(Resource.Loading<List<LeaveEntity>>())

        val listener = firestore.collection("leave_requests")
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // 🔹 FIX: Added <List<LeaveEntity>>
                    trySend(Resource.Error<List<LeaveEntity>>("Sync Error: ${error.message}"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val requests = snapshot.documents.map { doc ->
                        LeaveEntity(
                            employeeId = doc.getString("employeeId") ?: "",
                            employeeName = doc.getString("employeeName") ?: "",
                            startDate = doc.getLong("startDate") ?: 0L,
                            endDate = doc.getLong("endDate") ?: 0L,
                            reason = doc.getString("reason") ?: "",
                            status = doc.getString("status") ?: "PENDING",
                            requestDate = doc.getLong("requestDate") ?: 0L
                        )
                    }
                    trySend(Resource.Success(requests))
                }
            }
        awaitClose { listener.remove() }
    }

    // --- Leave Actions ---

    suspend fun applyForLeave(leave: LeaveEntity) = withContext(Dispatchers.IO) {
        try {
            leaveDao.insertLeave(leave)
            val leaveMap = hashMapOf(
                "employeeId" to leave.employeeId,
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
            throw e
        }
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