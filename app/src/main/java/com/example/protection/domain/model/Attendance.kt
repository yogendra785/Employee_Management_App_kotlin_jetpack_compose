package com.example.protection.domain.model

enum class AttendanceStatus {
    PRESENT,
    ABSENT
}

data class Attendance(
    val id: Long = 0,
    val employeeId: String, // 🔹 FIXED: String (Source of Truth)
    val date: Long,
    val status: AttendanceStatus
)