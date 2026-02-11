package com.example.neutron.domain.model

data class MonthlyStats (
    val monthName: String,
    val presentCount: Int,
    val absentCount: Int
)

data class AttendanceSummary(
    val totalPresent: Int,
    val totalAbsent: Int,
    val history: List<MonthlyStats>
)