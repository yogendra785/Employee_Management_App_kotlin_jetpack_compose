package com.example.protection.domain.model

data class MonthlyStats(
    val monthName: String,
    val presentCount: Int,
    val absentCount: Int
)

data class AttendanceSummary(
    val present: Int = 0,
    val absent: Int = 0,
    val halfDay: Int = 0,
    val holiday: Int = 0,
    val total: Int = 0,
    val title: String = "Overview",
    // 🔹 This is the list your History section needs
    val history: List<MonthlyStats> = emptyList()
) {
    // 🔹 These are the properties your stats card needs
    val totalPresent: Int get() = present
    val totalAbsent: Int get() = absent
}