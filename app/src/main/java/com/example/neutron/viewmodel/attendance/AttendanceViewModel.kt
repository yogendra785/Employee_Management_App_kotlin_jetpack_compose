package com.example.neutron.viewmodel.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neutron.data.repository.AttendanceRepository
import com.example.neutron.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {

    // 1. Reactive State for the selected date
    private val _selectedDate = MutableStateFlow(startOfToday())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    /**
     * 2. attendanceList is a reactive stream that updates whenever the date changes.
     * We use flatMapLatest to cancel previous database fetches if the user quickly scrolls dates.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val attendanceList: StateFlow<List<Attendance>> =
        selectedDate
            .flatMapLatest { date ->
                repository.getAttendanceByDate(date)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun setDate(date: Long) {
        _selectedDate.value = date
    }

    /**
     * Persists attendance entry to the local database via the repository.
     */
    fun markAttendance(employeeId: Long, status: AttendanceStatus) {
        viewModelScope.launch {
            val attendance = Attendance(
                employeeId = employeeId,
                date = _selectedDate.value,
                status = status
            )
            repository.markAttendance(attendance)
        }
    }

    /**
     * Transforms raw attendance records into a formatted summary.
     * This logic is kept in the ViewModel to keep the UI layer (Screens) lightweight.
     */
    fun getEmployeeSummary(employeeId: Long): Flow<AttendaceSummary> {
        return repository.getAttendanceForEmployee(employeeId).map { records ->
            val totalP = records.count { it.status == AttendanceStatus.PRESENT }
            val totalA = records.count { it.status == AttendanceStatus.ABSENT }

            val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

            // Group logs by Month and Year for the UI history list
            val history = records.groupBy {
                val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                sdf.format(cal.time)
            }.map { (month, logs) ->
                MonthlyStats(
                    monthName = month,
                    presentCount = logs.count { it.status == AttendanceStatus.PRESENT },
                    absentCount = logs.count { it.status == AttendanceStatus.ABSENT }
                )
            }.sortedByDescending { it.monthName }

            AttendaceSummary(totalP, totalA, history)
        }
    }

    companion object {
        /**
         * Standard helper to normalize dates to the start of the day (midnight).
         * This prevents time-of-day offsets from breaking database queries.
         */
        fun startOfToday(): Long {
            return Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
    }
}