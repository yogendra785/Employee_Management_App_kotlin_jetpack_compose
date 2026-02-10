package com.example.neutron.viewmodel.employee

import android.net.Uri
import android.util.Log
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

    fun onEmployeeIdChange(value: String) {
        _uiState.update { it.copy(
            employeeId = value,

        ) }
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
        if (newSalary.isEmpty() || newSalary.all { it.isDigit() || it == '.' }) {
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
        if (!validate(currentState)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 🔹 Make sure your domain model 'Employee' has 'employeeId' as a parameter!
                val employee = Employee(
                    employeeId = currentState.employeeId.trim(),
                    name = currentState.name.trim(),
                    email = currentState.email.trim(),
                    salary = currentState.salary.toDoubleOrNull() ?: 0.0,
                    department = currentState.department.trim(),
                    role = currentState.role.trim(),
                    isActive = currentState.isActive,
                    firebaseUid = "",
                    password = currentState.password
                )

                repository.insertEmployeeWithImage(employee, currentState.selectedImageUri)

                val user = FirebaseAuth.getInstance().currentUser

                if (user != null) {
                    repository.syncEmployeesToCloud(user.uid)
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    Log.e("ADD_VM", "User not authenticated with Firebase")
                    _uiState.update { it.copy(isLoading = false) }
                    // Handle error: show a message that Admin session expired
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { AddEmployeeUiState() }
    }

    private fun updateSaveEnabledState() {
        _uiState.update { state ->
            state.copy(isSaveEnabled = state.employeeId.isNotBlank() &&
                    state.name.isNotBlank() &&
                    state.email.isNotBlank() &&
                    state.password.isNotBlank() &&
                    state.nameError == null &&
                    state.emailError == null &&
                    state.passwordError == null)
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