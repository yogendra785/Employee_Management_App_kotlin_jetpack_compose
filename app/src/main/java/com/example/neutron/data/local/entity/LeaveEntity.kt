package com.example.neutron.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leave_requests")
data class LeaveEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: Long,
    val employeeName: String,
    val startDate: Long,
    val endDate: Long,
    val reason: String,
    val status: String, // "PENDING", "APPROVED", "REJECTED"
    val requestDate: Long = System.currentTimeMillis()
)