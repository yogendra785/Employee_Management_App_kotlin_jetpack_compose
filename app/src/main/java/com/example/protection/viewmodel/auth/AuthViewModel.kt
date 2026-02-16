package com.example.protection.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.protection.data.repository.EmployeeRepository
import com.example.protection.domain.model.Employee
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val role: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val employeeRepository: EmployeeRepository
) : ViewModel() {

    // 🔹 FIX 1: Start as LOADING. This stops the app from navigating immediately.
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<Employee?>(null)
    val currentUser: StateFlow<Employee?> = _currentUser.asStateFlow()

    // 🔹 FIX 2: Call this ONLY when Splash animation finishes
    fun checkAuthStatus() {
        viewModelScope.launch {
            val user = firebaseAuth.currentUser
            if (user != null) {
                try {
                    val document = firestore.collection("users").document(user.uid).get().await()
                    val role = document.getString("role") ?: "EMPLOYEE"
                    _authState.value = AuthState.Authenticated(role)
                } catch (e: Exception) {
                    _authState.value = AuthState.Unauthenticated
                }
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    // ... (Keep your login function exactly as it was) ...
    fun login(input: String, passwordInput: String) {
        if (input.contains("@")) adminLogin(input, passwordInput)
        else employeeLogin(input, passwordInput)
    }

    private fun adminLogin(email: String, passwordInput: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = firebaseAuth.signInWithEmailAndPassword(email.trim(), passwordInput.trim()).await()
                val uid = result.user?.uid
                if (uid != null) {
                    val document = firestore.collection("users").document(uid).get().await()
                    val role = document.getString("role") ?: "ADMIN"
                    syncUserToLocal(uid, role, email, document.getString("name")?:"Admin", employeeId = document.getString("employeeId")?:"ADM", department = "Management", salary = 0.0)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Login Failed")
            }
        }
    }

    private fun employeeLogin(employeeId: String, passwordInput: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val query = firestore.collection("users").whereEqualTo("employeeId", employeeId.trim()).get().await()
                if (query.isEmpty) { _authState.value = AuthState.Error("ID Not Found"); return@launch }
                val doc = query.documents.first()
                if (doc.getString("password")?.trim() == passwordInput.trim()) {
                    val role = doc.getString("role") ?: "EMPLOYEE"
                    syncUserToLocal(doc.getString("firebaseUid")?:doc.id, role, doc.getString("email")?:"", doc.getString("name")?:"", true, doc.getString("department")?:"General", doc.getDouble("salary")?:0.0, employeeId)
                } else { _authState.value = AuthState.Error("Incorrect Password") }
            } catch (e: Exception) { _authState.value = AuthState.Error("Error: ${e.localizedMessage}") }
        }
    }

    private suspend fun syncUserToLocal(uid: String, role: String, email: String, name: String, isEmployeeLogin: Boolean = false, department: String, salary: Double, employeeId: String) {
        try {
            val emp = Employee(firebaseUid = uid, name = name, email = email, role = role, department = department, salary = salary, password = "", employeeId = if(isEmployeeLogin) employeeId else "ADMIN", isActive = true, id = 0L)
            employeeRepository.insertEmployeeWithImage(emp, null)
            _currentUser.value = emp
            _authState.value = AuthState.Authenticated(role)
        } catch (e: Exception) { _authState.value = AuthState.Error("Database Error") }
    }

    fun logout() {
        firebaseAuth.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
    }
}