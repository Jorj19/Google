package com.example.google_hack

import android.util.Log
import okhttp3.*
import org.json.JSONObject // Added for basic JSON parsing

class GeminiLiveClient(
    private val onInterruption: (String) -> Unit,
    private val onAudioReceived: (ByteArray) -> Unit // Added to pass audio to your AudioTrack
) {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private val apiKey = BuildConfig.GEMINI_API_KEY

    private val wssUrl = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent?key=$apiKey"
    fun connect() {
        val request = Request.Builder().url(wssUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("GeminiLive", "Connected! Sending Setup...")
                sendSetupMessage(webSocket)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)

                    // 1. Check if setup is complete
                    if (json.has("setupComplete")) {
                        Log.d("GeminiLive", "Setup complete. Ready to send/receive audio.")
                        return
                    }

                    // 2. Parse Server Content
                    if (json.has("serverContent")) {
                        val serverContent = json.getJSONObject("serverContent")

                        // AI has finished its turn
                        if (serverContent.optBoolean("turnComplete")) {
                            Log.d("GeminiLive", "AI finished speaking.")
                            return
                        }

                        // Extract AI Audio Chunks
                        if (serverContent.has("modelTurn")) {
                            val parts = serverContent.getJSONObject("modelTurn").getJSONArray("parts")
                            for (i in 0 until parts.length()) {
                                val part = parts.getJSONObject(i)
                                if (part.has("inlineData")) {
                                    val base64Audio = part.getJSONObject("inlineData").getString("data")
                                    val audioBytes = android.util.Base64.decode(base64Audio, android.util.Base64.DEFAULT)
                                    // Send bytes to your AudioTrack to play the AI's voice
                                    onAudioReceived(audioBytes)
                                }
                            }

                            // Trigger your interruption logic if needed (be careful, this streams rapidly)
                            onInterruption("AI Audio Receiving")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GeminiLive", "Error parsing message: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("GeminiLive", "Failure: ${t.message}")
            }
        })
    }

    private fun sendSetupMessage(ws: WebSocket) {
        // FIXED: responseModalities moved inside generationConfig and converted to camelCase
        val setupPayload = """
        {
          "setup": {
            "model": "models/gemini-2.0-flash-exp",
            "generationConfig": {
              "responseModalities": ["AUDIO"]
            }
          }
        }
        """.trimIndent()

        Log.d("GeminiLive", "Sending Handshake: $setupPayload")
        ws.send(setupPayload)
    }

    fun sendAudio(data: ByteArray) {
        val base64Data = android.util.Base64.encodeToString(data, android.util.Base64.NO_WRAP)
        val audioPayload = """
        {
          "realtimeInput": {
            "mediaChunks": [{
              "mimeType": "audio/pcm;rate=16000",
              "data": "$base64Data"
            }]
          }
        }
        """.trimIndent()
        webSocket?.send(audioPayload)
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnected")
        webSocket = null
    }
}