package com.example.neutron.data.repository

import com.example.neutron.data.local.dao.AttendanceDao
import com.example.neutron.data.mapper.toAttendance
import com.example.neutron.data.mapper.toAttendanceEntity
import com.example.neutron.domain.model.Attendance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AttendanceRepository @Inject constructor(
    private val attendanceDao: AttendanceDao
) {
    /**
     * Fetches attendance for all employees on a specific date.
     * Used by the AttendanceScreen to display the daily list.
     */
    fun getAttendanceByDate(date: Long): Flow<List<Attendance>> {
        return attendanceDao.getAttendanceByDate(date)
            .map { entities ->
                entities.map { it.toAttendance() }
            }
    }

    /**
     * Fetches the complete attendance history for a single employee.
     * Used in the EmployeeDetailScreen to show the summary cards.
     */
    fun getAttendanceForEmployee(employeeId: Long): Flow<List<Attendance>> {
        return attendanceDao.getAttendanceForEmployee(employeeId)
            .map { entities ->
                entities.map { it.toAttendance() }
            }
    }

    /**
     * Helpful for general reporting or debugging the full database.
     */
    fun getAllAttendance(): Flow<List<Attendance>> {
        return attendanceDao.getAllAttendance()
            .map { entities ->
                entities.map { it.toAttendance() }
            }
    }

    /**
     * Marks or updates attendance. Using 'upsert' logic from the DAO
     * ensures we don't get duplicate entries for the same day.
     */
    suspend fun markAttendance(attendance: Attendance) {
        attendanceDao.upsertAttendance(attendance.toAttendanceEntity())
    }
}