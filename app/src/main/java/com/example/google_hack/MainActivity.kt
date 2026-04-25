package com.example.google_hack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.google_hack.ui.BackgroundWrapper
import com.example.google_hack.ui.CreateSpeechScreen
import com.example.google_hack.ui.MainScreen
import com.example.google_hack.ui.WelcomeScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.google_hack.presentation.auth.AuthScreen
import com.example.google_hack.presentation.auth.AuthState
import com.example.google_hack.presentation.auth.AuthViewModel
import com.example.google_hack.presentation.home.HomeViewModel
import com.example.google_hack.data.models.Project
import com.example.google_hack.ui.PracticeScreen
import com.example.google_hack.ui.theme.Google_HackTheme
import kotlinx.coroutines.delay

enum class Screen {
    Welcome,
    Auth,
    Main,
    CreateSpeech,
    Practice
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Google_HackTheme {
                val authViewModel: AuthViewModel = viewModel()
                val homeViewModel: HomeViewModel = viewModel()
                val authState by authViewModel.authState.collectAsState()

                var currentScreen by remember { mutableStateOf(value = Screen.Welcome) }
                var selectedProject by remember { mutableStateOf<Project?>(null) }
                var nextScreen by remember { mutableStateOf<Screen?>(value = null) }
                var isTransitioning by remember { mutableStateOf(value = false) }

                // Navigation Coroutine for Animation
                LaunchedEffect(nextScreen) {
                    nextScreen?.let { target ->
                        isTransitioning = true
                        delay(timeMillis = 1000) // Match BackgroundWrapper animation duration
                        currentScreen = target
                        isTransitioning = false
                        nextScreen = null
                    }
                }

                // Handle Auth State changes for navigation
                LaunchedEffect(authState) {
                    if ((authState is AuthState.Authenticated) && (currentScreen == Screen.Auth)) {
                        nextScreen = Screen.Main
                    }
                }

                BackgroundWrapper(isTransitioning = isTransitioning) {
                    when (currentScreen) {
                        Screen.Welcome -> {
                            WelcomeScreen {
                                nextScreen = Screen.Main
                            }
                        }
                        Screen.Auth -> {
                            AuthScreen(
                                viewModel = authViewModel,
                                onSuccess = {
                                    // Managed by LaunchedEffect(authState)
                                },
                            )
                        }
                        Screen.Main -> {
                            MainScreen(
                                homeViewModel = homeViewModel,
                                onAddSpeechClick = {
                                    currentScreen = Screen.CreateSpeech
                                },
                                onSpeechClick = { project ->
                                    selectedProject = project
                                    currentScreen = Screen.Practice
                                }
                            )
                        }
                        Screen.CreateSpeech -> {
                            CreateSpeechScreen(
                                homeViewModel = homeViewModel,
                                onBackClick = { currentScreen = Screen.Main },
                                onSubmitClick = { 
                                    currentScreen = Screen.Main 
                                },
                            )
                        }
                        Screen.Practice -> {
                            selectedProject?.let { project ->
                                PracticeScreen(
                                    project = project,
                                    onBackClick = { currentScreen = Screen.Main }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
