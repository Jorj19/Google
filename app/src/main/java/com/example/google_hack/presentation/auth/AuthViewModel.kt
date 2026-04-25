package com.example.google_hack.presentation.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkIfUserIsLoggedIn()
    }

    fun checkIfUserIsLoggedIn() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _authState.value = AuthState.Authenticated(currentUser)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credentialManager = CredentialManager.create(context)
                
                // Use the provided Web Client ID directly for verification
                val webClientId = "790058569573-0p57lj82l0o27k245jrqb6dv209m6046.apps.googleusercontent.com"

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                Log.d("AuthViewModel", "Credential received: ${credential.type}")

                if (credential is GoogleIdTokenCredential) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
                    val authResult = auth.signInWithCredential(firebaseCredential).await()
                    val user = authResult.user
                    
                    if (user != null) {
                        Log.d("AuthViewModel", "Firebase Auth successful for user: ${user.uid}")
                        saveUserToFirestore(user)
                        Log.d("AuthViewModel", "Setting AuthState to Authenticated")
                        _authState.value = AuthState.Authenticated(user)
                    } else {
                        _authState.value = AuthState.Error("User is null after sign in")
                    }
                } else if (credential.type == "com.google.android.libraries.identity.googleid.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL") {
                    // Fallback for cases where 'is' check might fail due to classloader or library version issues
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                    val authResult = auth.signInWithCredential(firebaseCredential).await()
                    val user = authResult.user
                    
                    if (user != null) {
                        Log.d("AuthViewModel", "Firebase Auth successful (fallback) for user: ${user.uid}")
                        saveUserToFirestore(user)
                        Log.d("AuthViewModel", "Setting AuthState to Authenticated (fallback)")
                        _authState.value = AuthState.Authenticated(user)
                    } else {
                        _authState.value = AuthState.Error("User is null after sign in (fallback)")
                    }
                } else {
                    _authState.value = AuthState.Error("Invalid credential type: ${credential.type}")
                }
            } catch (e: GetCredentialException) {
                Log.e("AuthViewModel", "Credential Manager error", e)
                _authState.value = AuthState.Error(e.message ?: "Credential Manager error")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Sign in error", e)
                _authState.value = AuthState.Error(e.message ?: "Sign in failed")
            }
        }
    }

    private suspend fun saveUserToFirestore(user: com.google.firebase.auth.FirebaseUser) {
        val userMap = hashMapOf(
            "uid" to user.uid,
            "name" to (user.displayName ?: ""),
            "email" to (user.email ?: ""),
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        try {
            firestore.collection("users")
                .document(user.uid)
                .set(userMap, SetOptions.merge())
                .await()
            Log.d("AuthViewModel", "User profile saved to Firestore")
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error saving user to Firestore", e)
        }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            auth.signOut()
            try {
                val credentialManager = CredentialManager.create(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error clearing credentials", e)
            }
            _authState.value = AuthState.Unauthenticated
        }
    }
}
