package com.example.google_hack

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.google_hack.ui.theme.Google_HackTheme

class MainActivity : ComponentActivity() {

    private lateinit var liveClient: GeminiLiveClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            var responseText by remember { mutableStateOf("Ready to connect...") }
            var isConnected by remember { mutableStateOf(false) }

            // Initialize the client
            LaunchedEffect(Unit) {
                liveClient = GeminiLiveClient(
                    onMessage = { message ->
                        Log.d("MainActivity", "Message: $message")
                        responseText = "Server: $message"
                    },
                    onError = { error ->
                        Log.e("MainActivity", "Error: $error")
                        responseText = "Error: $error"
                    },
                    onConnectionStatus = { status ->
                        isConnected = status
                        responseText = if (status) "Connected to Gemini Live" else "Disconnected"
                    }
                )
            }

            Google_HackTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = responseText,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { liveClient.connect() },
                                enabled = !isConnected
                            ) {
                                Text("Connect")
                            }

                            Button(
                                onClick = { liveClient.sendText("Hello! Can you hear me?") },
                                enabled = isConnected
                            ) {
                                Text("Send Hello")
                            }

                            Button(
                                onClick = { liveClient.disconnect() },
                                enabled = isConnected
                            ) {
                                Text("Disconnect")
                            }
                        }
                    }
                }
            }
        }
    }
}
