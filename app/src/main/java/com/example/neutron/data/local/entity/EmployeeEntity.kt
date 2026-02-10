package com.example.neutron.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: String = "",
    val firebaseUid: String,
    val name: String,
    val email: String,
    val role: String,
    val department: String,
    val salary: Double, // 🔹 Added this to match your domain model
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val password: String,
    val imagePath: String? = null
)