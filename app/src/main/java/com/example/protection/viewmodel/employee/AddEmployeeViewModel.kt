package com.example.protection.viewmodel.employee

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.protection.data.repository.EmployeeRepository
import com.example.protection.domain.model.Employee
import com.example.protection.utils.clean // 🔹 Ensure StringExtensions.kt is created
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AddEmployeeViewModel @Inject constructor(
    private val repository: EmployeeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEmployeeUiState())
    val uiState: StateFlow<AddEmployeeUiState> = _uiState.asStateFlow()

    fun onEmployeeIdChange(value: String) {
        _uiState.update { it.copy(employeeId = value) }
        updateSaveEnabledState()
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(
            name = value,
            nameError = if (value.trim().length < 3) "Name must be at least 3 characters" else null
        ) }
        updateSaveEnabledState()
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(
            email = value,
            emailError = if (!isValidEmail(value.trim())) "Invalid email format" else null
        ) }
        updateSaveEnabledState()
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(
            password = value,
            passwordError = if (value.length < 6) "Password must be at least 6 characters" else null
        ) }
        updateSaveEnabledState()
    }

    fun onSalaryChange(newSalary: String) {
        if (newSalary.isEmpty() || (newSalary.count { it == '.' } <= 1 && newSalary.all { it.isDigit() || it == '.' })) {
            _uiState.update { it.copy(salary = newSalary) }
        }
    }

    fun onRoleChange(value: String) {
        _uiState.update { it.copy(role = value) }
        updateSaveEnabledState()
    }

    fun onDepartmentChange(value: String) {
        _uiState.update { it.copy(department = value) }
    }

    fun onActiveChange(value: Boolean) {
        _uiState.update { it.copy(isActive = value) }
    }

    fun onImageSelected(uri: Uri?) {
        _uiState.update { it.copy(selectedImageUri = uri) }
    }

    fun onSaveEmployee() {
        val currentState = uiState.value

        // 🛡️ 1. DEBOUNCE: Stop if already loading
        if (currentState.isLoading) return

        if (!validate(currentState)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 🛡️ 2. SANITIZATION: Clean inputs before saving
                // Requires the .clean() extension we created in StringExtensions.kt
                // If you haven't created it yet, use .trim().replace("\\s+".toRegex(), " ")
                val cleanName = currentState.name.clean()
                val cleanDepartment = currentState.department.clean()
                val cleanEmail = currentState.email.trim()
                val cleanId = currentState.employeeId.trim()
                val cleanPassword = currentState.password.trim()

                val employee = Employee(
                    id = 0,
                    employeeId = cleanId,
                    name = cleanName,
                    email = cleanEmail,
                    salary = currentState.salary.toDoubleOrNull() ?: 0.0,
                    department = cleanDepartment,
                    role = currentState.role.trim(),
                    isActive = currentState.isActive,
                    firebaseUid = "",
                    password = cleanPassword,
                    createdAt = System.currentTimeMillis()
                )

                // 📸 3. COMPRESSION: Handled automatically by Repository now!
                repository.insertEmployeeWithImage(employee, currentState.selectedImageUri)

                // ☁️ 4. SYNC
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    repository.syncEmployeesToCloud(user.uid)
                } else {
                    Log.e("ADD_VM", "User offline/unauthenticated. Saved locally.")
                }

                _uiState.update { it.copy(isLoading = false, isSuccess = true) }

            } catch (e: Exception) {
                Log.e("ADD_VM", "Save Error: ${e.message}")
                _uiState.update { it.copy(isLoading = false) } // Ensure loading stops on error
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { AddEmployeeUiState() }
    }

    private fun updateSaveEnabledState() {
        _uiState.update { state ->
            val isValid = state.employeeId.isNotBlank() &&
                    state.name.isNotBlank() &&
                    state.email.isNotBlank() &&
                    state.password.isNotBlank() &&
                    state.nameError == null &&
                    state.emailError == null &&
                    state.passwordError == null
            state.copy(isSaveEnabled = isValid)
        }
    }

    private fun validate(state: AddEmployeeUiState): Boolean {
        return state.employeeId.isNotBlank() &&
                state.name.trim().length >= 3 &&
                isValidEmail(state.email.trim()) &&
                state.password.length >= 6
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}