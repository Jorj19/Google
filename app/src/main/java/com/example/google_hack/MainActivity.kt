package com.example.google_hack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.google_hack.presentation.auth.AuthScreen
import com.example.google_hack.presentation.auth.AuthState
import com.example.google_hack.presentation.auth.AuthViewModel
import com.example.google_hack.presentation.home.HomeScreen
import com.example.google_hack.presentation.debug.DebugScreen
import com.example.google_hack.ui.theme.Google_HackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Google_HackTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    // Determine start destination based on INITIAL auth state
    // We use a LaunchedEffect to handle navigation after login
    androidx.compose.runtime.LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            navController.navigate("home") {
                popUpTo("auth") { inclusive = true }
            }
        } else if (authState is AuthState.Unauthenticated) {
            navController.navigate("auth") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = "auth") {
        composable("auth") {
            AuthScreen(
                viewModel = authViewModel,
                onSuccess = {
                    // This callback might redundant with LaunchedEffect but safe to keep
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                viewModel = authViewModel,
                onNavigateToDebug = {
                    navController.navigate("debug")
                },
                onSignOut = {
                    navController.navigate("auth") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
        composable("debug") {
            DebugScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
