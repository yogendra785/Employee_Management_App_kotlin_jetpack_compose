package com.example.neutron.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth // 🔹 Injected via Hilt for cleaner architecture
) {

    /**
     * Returns the current Firebase user session.
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    /**
     * Authenticates a user with email and password.
     * Used by the AuthViewModel after the Employee ID lookup.
     */
    suspend fun login(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password.trim()).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creates a new Firebase Authentication account.
     */
    suspend fun signup(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password.trim()).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    /**
     * Simple check to see if a session exists.
     */
    fun isLoggedIn(): Boolean = auth.currentUser != null
}