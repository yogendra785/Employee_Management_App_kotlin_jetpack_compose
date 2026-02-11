package com.example.neutron.viewmodel.attendance

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neutron.data.repository.AttendanceRepository
import com.example.neutron.data.repository.EmployeeRepository
import com.example.neutron.domain.model.*
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val repository: AttendanceRepository,
    private val employeeRepository: EmployeeRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(startOfToday())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        Log.d("ATTENDANCE_VM", "AttendanceViewModel Created/Initialized")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val attendanceList: StateFlow<List<Attendance>> =
        selectedDate
            .flatMapLatest { date -> repository.getAttendanceByDate(date) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // 🔹 FIX: Accept UID as a parameter
    fun loadUserData(uid: String) {
        Log.d("ATTENDANCE_VM", "loadUserData() CALLED with UID: $uid")

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Try finding user in local DB
                val employees = employeeRepository.getAllEmployees().first()
                var myProfile = employees.find { it.firebaseUid == uid }

                // 2. Fallback: If not found locally, fetch from Cloud
                if (myProfile == null) {
                    Log.w("ATTENDANCE_VM", "User not found locally. Fetching from Cloud...")
                    val snapshot = firestore.collection("users").document(uid).get().await()

                    if (snapshot.exists()) {
                        val remoteProfile = Employee(
                            id = 0,
                            employeeId = snapshot.getString("employeeId") ?: "",
                            firebaseUid = snapshot.getString("firebaseUid") ?: uid,
                            name = snapshot.getString("name") ?: "Unknown",
                            email = snapshot.getString("email") ?: "",
                            role = snapshot.getString("role") ?: "EMPLOYEE",
                            department = snapshot.getString("department") ?: "",
                            salary = snapshot.getDouble("salary") ?: 0.0,
                            password = snapshot.getString("password") ?: "",
                            isActive = snapshot.getBoolean("isActive") ?: true
                        )
                        employeeRepository.insertEmployeeWithImage(remoteProfile, null)
                        myProfile = remoteProfile
                    }
                }

                // 3. Sync Attendance
                if (myProfile != null) {
                    Log.d("ATTENDANCE_VM", "Syncing attendance for ID: ${myProfile.employeeId}")
                    repository.syncAttendanceFromFirestore(myProfile.employeeId)
                } else {
                    Log.e("ATTENDANCE_VM", "Could not resolve Profile for UID: $uid")
                }

            } catch (e: Exception) {
                Log.e("ATTENDANCE_VM", "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setDate(date: Long) { _selectedDate.value = date }

    fun markAttendance(employeeId: String, status: AttendanceStatus) {
        viewModelScope.launch {
            val attendance = Attendance(employeeId = employeeId, date = _selectedDate.value, status = status)
            repository.markAttendance(attendance)
        }
    }

    fun getEmployeeSummary(employeeId: String): Flow<AttendanceSummary> {
        return repository.getAttendanceForEmployee(employeeId).map { records ->
            val totalP = records.count { it.status == AttendanceStatus.PRESENT }
            val totalA = records.count { it.status == AttendanceStatus.ABSENT }
            val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val history = records.groupBy {
                val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                sdf.format(cal.time)
            }.map { (month, logs) ->
                MonthlyStats(month, logs.count { it.status == AttendanceStatus.PRESENT }, logs.count { it.status == AttendanceStatus.ABSENT })
            }.sortedByDescending { it.monthName }
            AttendanceSummary(totalP, totalA, history)
        }
    }

    companion object {
        fun startOfToday(): Long = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}