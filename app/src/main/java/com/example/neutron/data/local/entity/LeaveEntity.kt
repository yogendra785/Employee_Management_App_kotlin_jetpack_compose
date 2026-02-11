package com.example.neutron.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "leave_requests",
    foreignKeys = [
        ForeignKey(
            entity = EmployeeEntity::class,
            parentColumns = ["employeeId"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("employeeId")] // Optimize search by employeeId
)
data class LeaveEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: String, // 🔹 FIXED: Changed Long -> String to match EmployeeEntity
    val employeeName: String,
    val startDate: Long,
    val endDate: Long,
    val reason: String,
    val status: String, // "PENDING", "APPROVED", "REJECTED"
    val requestDate: Long = System.currentTimeMillis()
)