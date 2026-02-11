package com.example.neutron.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "employees",
    // 🔹 FIX: This tells Room that 'employeeId' is unique, allowing SalaryEntity to reference it.
    indices = [Index(value = ["employeeId"], unique = true)]
)

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
    val imagePath: String? = null,
    val parentName: String = "",
    val address: String = "",
    val aadharNumber: String = "",
    val panNumber: String = "",
    val contactNumber: String = "",
    val emergencyContact: String = ""
)