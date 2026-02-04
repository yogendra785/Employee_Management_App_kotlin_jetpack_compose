package com.example.neutron.viewmodel.employee

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neutron.data.repository.EmployeeRepository
import com.example.neutron.domain.model.Employee
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

    fun onNameChange(value: String) {
        _uiState.update { it.copy(
            name = value,
            nameError = if (value.trim().length < 3) "Name too short" else null
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
            passwordError = if (value.length < 6) "Password too short" else null
        ) }
        updateSaveEnabledState()
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

    // New function to handle image selection from the UI
    fun onImageSelected(uri: Uri?) {
        _uiState.update { it.copy(selectedImageUri = uri) }
    }

    fun onSaveEmployee() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val employee = Employee(
                name = uiState.value.name,
                email = uiState.value.email,
                salary = uiState.value.salary.toDoubleOrNull() ?: 0.0,
                department = uiState.value.department,
                role = uiState.value.role,
                isActive = uiState.value.isActive,
                firebaseUid = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                password = uiState.value.password
            )

            // 1. Save to Local Room DB
            repository.insertEmployeeWithImage(employee, uiState.value.selectedImageUri)

            // 2. 🔹 TRIGGER CLOUD SYNC IMMEDIATELY
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid
            currentUid?.let { uid ->
                repository.syncEmployeesToCloud(uid)
            }

            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }

    fun resetSuccess() {
        _uiState.update { AddEmployeeUiState() }
    }

    private fun updateSaveEnabledState() {
        _uiState.update { state ->
            state.copy(isSaveEnabled = state.name.isNotBlank() &&
                    state.email.isNotBlank() &&
                    state.role.isNotBlank() &&
                    state.password.isNotBlank() &&
                    state.nameError == null &&
                    state.emailError == null &&
                    state.passwordError == null)
        }
    }

    private fun validate(state: AddEmployeeUiState): Boolean {
        val nameValid = state.name.trim().length >= 3
        val emailValid = isValidEmail(state.email.trim())
        val passwordValid = state.password.length >= 6
        return nameValid && emailValid && state.role.isNotBlank() && passwordValid
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    fun onSalaryChange(newSalary: String) {
        if (newSalary.all { it.isDigit() }) {
            _uiState.update { it.copy(salary = newSalary) }
        }
    }
}