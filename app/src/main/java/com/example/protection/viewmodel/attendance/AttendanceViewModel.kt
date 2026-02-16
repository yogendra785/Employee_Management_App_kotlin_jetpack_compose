package com.example.protection.viewmodel.attendance

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.protection.data.repository.AttendanceRepository
import com.example.protection.data.repository.EmployeeRepository
import com.example.protection.domain.model.* // 🔹 Imports AttendanceSummary & MonthlyStats from Step 1
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

    private val _summary = MutableStateFlow(AttendanceSummary())
    val summary: StateFlow<AttendanceSummary> = _summary.asStateFlow()

    init {
        Log.d("ATTENDANCE_VM", "AttendanceViewModel Initialized")
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

    // 🔹 Calculates History & Totals using the Domain Model
    fun getEmployeeSummary(employeeId: String): Flow<AttendanceSummary> {
        return repository.getAttendanceForEmployee(employeeId).map { records ->
            val p = records.count { it.status == AttendanceStatus.PRESENT }
            val a = records.count { it.status == AttendanceStatus.ABSENT }

            val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

            val historyList = records
                .groupBy {
                    val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                    sdf.format(cal.time)
                }
                .map { (month, logs) ->
                    MonthlyStats(
                        monthName = month,
                        presentCount = logs.count { it.status == AttendanceStatus.PRESENT },
                        absentCount = logs.count { it.status == AttendanceStatus.ABSENT }
                    )
                }
                .sortedByDescending { it.monthName }

            AttendanceSummary(
                present = p,
                absent = a,
                total = records.size,
                history = historyList
            )
        }
    }

    fun loadMonthlySummary(employeeId: String, currentMonthDate: Long) {
        viewModelScope.launch {
            repository.getAttendanceForEmployee(employeeId).collect { allRecords ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = currentMonthDate
                val currentMonth = cal.get(Calendar.MONTH)
                val currentYear = cal.get(Calendar.YEAR)

                val monthRecords = allRecords.filter {
                    cal.timeInMillis = it.date
                    cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
                }

                _summary.value = AttendanceSummary(
                    present = monthRecords.count { it.status == AttendanceStatus.PRESENT },
                    absent = monthRecords.count { it.status == AttendanceStatus.ABSENT },
                    total = monthRecords.size,
                    title = "My Monthly Record"
                )
            }
        }
    }

    fun loadUserData(uid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val employees = employeeRepository.getAllEmployees().first()
                var myProfile = employees.find { it.firebaseUid == uid }

                if (myProfile == null) {
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

                if (myProfile != null) {
                    repository.syncAttendanceFromFirestore(myProfile.employeeId)
                    loadMonthlySummary(myProfile.employeeId, _selectedDate.value)
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

    companion object {
        fun startOfToday(): Long = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}