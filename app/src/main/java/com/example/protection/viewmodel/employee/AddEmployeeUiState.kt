package com.example.protection.viewmodel.employee

import android.net.Uri

data class AddEmployeeUiState(
    val employeeId: String = "",
    val idError: String? = null,
    val name: String = "",
    val nameError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val role: String = "STAFF",
    val department: String = "",
    val salary: String = "",
    val isActive: Boolean = true,
    val selectedImageUri: Uri? = null,
    val isSaveEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false
)