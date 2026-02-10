package com.example.neutron.viewmodel.auth

sealed class AuthState {
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data class Authenticated(val userRole: String) : AuthState()
    data class Error(val message: String) : AuthState()
}