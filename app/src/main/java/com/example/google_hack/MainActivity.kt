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
import com.example.google_hack.ui.theme.Google_HackTheme
import kotlinx.coroutines.delay

enum class Screen {
    Welcome,
    Main,
    CreateSpeech
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Google_HackTheme {
                var currentScreen by remember { mutableStateOf(Screen.Welcome) }
                var nextScreen by remember { mutableStateOf<Screen?>(null) }
                var isTransitioning by remember { mutableStateOf(false) }
                
                // Navigation Coroutine for Animation
                LaunchedEffect(nextScreen) {
                    nextScreen?.let { target ->
                        isTransitioning = true
                        delay(1000) // Match BackgroundWrapper animation duration
                        currentScreen = target
                        isTransitioning = false
                        nextScreen = null
                    }
                }

                BackgroundWrapper(isTransitioning = isTransitioning) {
                    when (currentScreen) {
                        Screen.Welcome -> {
                            WelcomeScreen(
                                onStartClick = { nextScreen = Screen.Main }
                            )
                        }
                        Screen.Main -> {
                            MainScreen(
                                onAddSpeechClick = { currentScreen = Screen.CreateSpeech }
                            )
                        }
                        Screen.CreateSpeech -> {
                            CreateSpeechScreen(
                                onBackClick = { currentScreen = Screen.Main },
                                onSubmitClick = { currentScreen = Screen.Main }
                            )
                        }
                    }
                }
            }
        }
    }
}
