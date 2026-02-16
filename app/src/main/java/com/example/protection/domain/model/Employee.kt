package com.example.protection.domain.model

data class Employee(
    val id: Long = 0,
    val employeeId: String = "",
    val firebaseUid: String,
    val name: String,
    val email: String,
    val role: String,
    val salary: Double = 0.0,
    val department: String,
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