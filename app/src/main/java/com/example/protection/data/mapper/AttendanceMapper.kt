package com.example.protection.data.mapper

import com.example.protection.data.local.entity.AttendanceEntity
import com.example.protection.domain.model.Attendance
import com.example.protection.domain.model.AttendanceStatus

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