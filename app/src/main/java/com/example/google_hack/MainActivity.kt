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

    private lateinit var testClient: GeminiLiveClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- GEMINI LIVE TEST CODE ---
        // 2. Initialize the client.
        //    Now providing both required lambda parameters.
        testClient = GeminiLiveClient(
            onInterruption = { aiFeedback ->
                // This callback runs when the AI provides general status/interruption messages
                Log.d("MainActivity", "AI Interruption/Status: $aiFeedback")
                // You'll want to update UI on the main thread if needed:
                // runOnUiThread { /* Update UI here */ }
            },
            onAudioReceived = { audioBytes ->
                // This callback runs every time a new chunk of AI audio is received
                Log.d("MainActivity", "Received ${audioBytes.size} bytes of AI audio.")
                // TODO: Here is where you would typically pass 'audioBytes' to an AudioTrack
                //       for real-time playback.
                // You'll want to play audio on a separate thread or use a dedicated audio player.
            }
        )

        // Don't forget the wssUrl fix from the previous answer!
        // (Make sure your GeminiLiveClient uses v1alpha in its wssUrl)

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
                        Text(text = "Testing Gemini Live Connection...\n\nCheck Logcat for 'GeminiLive' and 'MainActivity'!")
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