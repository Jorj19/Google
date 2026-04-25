package com.example.google_hack

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.google_hack.ui.theme.Google_HackTheme

class MainActivity : ComponentActivity() {

    // 1. Declare the client at the class level
    private lateinit var testClient: GeminiLiveClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- GEMINI LIVE TEST CODE ---
        // 2. Initialize the client. We pass a simple lambda that logs any AI interruptions.
        testClient = GeminiLiveClient { aiFeedback ->
            Log.d("MainActivity", "AI Interrupted with: $aiFeedback")
        }

        // 3. Fire off the connection to Google AI Studio!
        testClient.connect()
        // -----------------------------

        enableEdgeToEdge()
        setContent {
            Google_HackTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // A simple UI reminding you to check the logs
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Testing Gemini Live Connection...\n\nCheck Logcat for 'GeminiLive'!")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 4. Always be a good citizen and close your sockets!
        testClient.disconnect()
    }
}