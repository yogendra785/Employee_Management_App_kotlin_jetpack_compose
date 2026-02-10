package com.example.neutron.domain.model

// 🔹 Remove the Room @Entity and @PrimaryKey imports
data class LeaveRequest(
    val id: Long = 0,
    val employeeId: Long,
    val employeeName: String,
    val startDate: Long,
    val endDate: Long,
    val reason: String,
    val status: String = "PENDING",
    val requestDate: Long = System.currentTimeMillis()
)