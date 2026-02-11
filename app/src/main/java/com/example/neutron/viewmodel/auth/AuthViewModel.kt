package com.example.neutron.viewmodel.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neutron.data.repository.EmployeeRepository
import com.example.neutron.domain.model.Employee
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val employeeRepository: EmployeeRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<Employee?>(null)
    val currentUser: StateFlow<Employee?> = _currentUser.asStateFlow()

    /**
     * 🔹 ADMIN LOGIN
     */
    fun adminLogin(email: String, passwordInput: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = firebaseAuth.signInWithEmailAndPassword(email.trim(), passwordInput.trim()).await()
                val uid = result.user?.uid

                if (uid != null) {
                    val document = firestore.collection("users").document(uid).get().await()
                    val role = document.getString("role") ?: "ADMIN"

                    if (role == "ADMIN") {
                        syncUserToLocal(
                            uid = uid,
                            role = "ADMIN",
                            email = email,
                            name = document.getString("name") ?: "Admin",
                            employeeId = document.getString("employeeId") ?: "ADMIN001"
                        )
                    } else {
                        firebaseAuth.signOut()
                        _authState.value = AuthState.Error("Access Denied: Admins only")
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Admin Login Failed")
            }
        }
    }

    /**
     * 🔹 EMPLOYEE LOGIN (Robust Fix)
     */
    fun employeeLogin(employeeId: String, passwordInput: String) {
        val cleanEmployeeId = employeeId.trim()
        val cleanPassword = passwordInput.trim()

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // 1. Query Firestore for the document with the matching custom Employee ID
                val querySnapshot = firestore.collection("users")
                    .whereEqualTo("employeeId", cleanEmployeeId)
                    .get()
                    .await()

                if (querySnapshot.isEmpty) {
                    _authState.value = AuthState.Error("Employee ID '$cleanEmployeeId' not found.")
                    return@launch
                }

                val document = querySnapshot.documents.first()

                // 🔹 ROBUST PASSWORD CHECK:
                // We handle cases where password might be stored as Number or String, and trim spaces.
                val rawDbPassword = document.get("password")?.toString() ?: ""
                val dbPassword = rawDbPassword.trim()

                Log.d("AUTH_DEBUG", "Input: '$cleanPassword' | DB (Trimmed): '$dbPassword'")

                // 2. Validate Password
                if (dbPassword == cleanPassword) {
                    val roleFromDb = document.getString("role") ?: "EMPLOYEE"
                    val uid = document.getString("firebaseUid") ?: document.id
                    val name = document.getString("name") ?: "Staff"
                    val email = document.getString("email") ?: ""

                    // 3. Sync to local and Update State
                    syncUserToLocal(
                        uid = uid,
                        role = roleFromDb,
                        email = email,
                        name = name,
                        isEmployeeLogin = true,
                        department = document.getString("department") ?: "General",
                        salary = document.getDouble("salary") ?: 0.0,
                        employeeId = cleanEmployeeId
                    )
                } else {
                    _authState.value = AuthState.Error("Incorrect Password")
                }

            } catch (e: Exception) {
                _authState.value = AuthState.Error("Connection Error: ${e.localizedMessage}")
            }
        }
    }

    // ... (Keep syncUserToLocal, signup, and logout exactly as they were) ...

    private suspend fun syncUserToLocal(
        uid: String,
        role: String,
        email: String,
        name: String,
        isEmployeeLogin: Boolean = false,
        department: String = "Management",
        salary: Double = 0.0,
        employeeId: String = ""
    ) {
        try {
            val employee = Employee(
                id = 0,
                employeeId = if (isEmployeeLogin) employeeId else "ADMIN",
                firebaseUid = uid,
                name = name,
                email = email,
                role = role,
                department = department,
                salary = salary,
                password = "",
                isActive = true
            )

            employeeRepository.insertEmployeeWithImage(employee, null)
            _currentUser.value = employee
            _authState.value = AuthState.Authenticated(role)
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Local Sync Failed", e)
            _authState.value = AuthState.Error("Local database error")
        }
    }

    fun signup(email: String, password: String, name: String, employeeId: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password.trim()).await()
                val uid = result.user?.uid ?: return@launch

                val userMap = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "role" to "ADMIN",
                    "employeeId" to employeeId,
                    "firebaseUid" to uid,
                    "department" to "Management",
                    "salary" to 0.0,
                    "isActive" to true,
                    "createdAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(uid).set(userMap).await()

                syncUserToLocal(uid, "ADMIN", email, name, false, "Management", 0.0, employeeId)

            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Signup failed")
            }
        }
    }

    fun logout() {
        firebaseAuth.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
    }
}