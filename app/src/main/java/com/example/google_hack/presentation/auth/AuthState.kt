package com.example.google_hack.presentation.auth

import com.google.firebase.auth.FirebaseUser

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
