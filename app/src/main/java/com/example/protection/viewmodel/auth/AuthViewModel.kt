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
import kotlinx.coroutines.flow.first
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

                    // 📸 🔹 THE FIX: Fetch the local user profile from Room so we get the imagePath!
                    val result = employeeRepository.getAllEmployees().first()
                    val allEmployees = result.data ?: emptyList()
                    val localUser = allEmployees.find { it.firebaseUid == user.uid }

                    if (localUser != null) {
                        // We found the local record with the image path!
                        _currentUser.value = localUser
                    } else {
                        // Fallback just in case the local database was cleared
                        _currentUser.value = Employee(
                            id = 0,
                            employeeId = document.getString("employeeId") ?: "",
                            firebaseUid = user.uid,
                            name = document.getString("name") ?: "Unknown",
                            email = document.getString("email") ?: "",
                            role = role,
                            department = document.getString("department") ?: "",
                            salary = document.getDouble("salary") ?: 0.0,
                            password = "",
                            isActive = true
                        )
                    }

                    _authState.value = AuthState.Authenticated(role)
                } catch (e: Exception) {
                    _authState.value = AuthState.Unauthenticated
                }
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }
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
                    // 🔹 THE FIX: Grab the imagePath from Firestore!
                    val imagePath = doc.getString("imagePath")

                    syncUserToLocal(
                        uid = doc.getString("firebaseUid") ?: doc.id,
                        role = role,
                        email = doc.getString("email") ?: "",
                        name = doc.getString("name") ?: "",
                        isEmployeeLogin = true,
                        department = doc.getString("department") ?: "General",
                        salary = doc.getDouble("salary") ?: 0.0,
                        employeeId = employeeId,
                        imagePath = imagePath // 🔹 Pass it to Room!
                    )
                } else { _authState.value = AuthState.Error("Incorrect Password") }
            } catch (e: Exception) { _authState.value = AuthState.Error("Error: ${e.localizedMessage}") }
        }
    }

    // 🔹 UPDATED signature to accept imagePath
    private suspend fun syncUserToLocal(uid: String, role: String, email: String, name: String, isEmployeeLogin: Boolean = false, department: String, salary: Double, employeeId: String, imagePath: String? = null) {
        try {
            val emp = Employee(firebaseUid = uid, name = name, email = email, role = role, department = department, salary = salary, password = "", employeeId = if(isEmployeeLogin) employeeId else "ADMIN", isActive = true, id = 0L, imagePath = imagePath)
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