package com.example.neutron.data.mapper

import com.example.neutron.data.local.entity.AttendanceEntity
import com.example.neutron.domain.model.Attendance
import com.example.neutron.domain.model.AttendanceStatus

fun AttendanceEntity.toAttendance(): Attendance {
    return Attendance(
        id = id,
        employeeId = employeeId, // 🔹 String to String (No mismatch!)
        date = date,
        status = try {
            AttendanceStatus.valueOf(status)
        } catch (e: Exception) {
            AttendanceStatus.ABSENT
        }
    )
}

fun Attendance.toAttendanceEntity(): AttendanceEntity {
    return AttendanceEntity(
        id = id,
        employeeId = employeeId, // 🔹 String to String (No mismatch!)
        date = date,
        status = status.name
    )
}