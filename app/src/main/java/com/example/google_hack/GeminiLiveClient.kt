package com.example.google_hack

import android.util.Log
import okhttp3.*

class GeminiLiveClient(
    private val onMessage: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onConnectionStatus: (Boolean) -> Unit
) {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private val apiKey = BuildConfig.GEMINI_API_KEY

    // The Gemini Multimodal Live API endpoint
    private val wssUrl = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent?key=$apiKey"

    fun connect() {
        val request = Request.Builder().url(wssUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("GeminiLive", "WebSocket Connected")
                onConnectionStatus(true)
                sendSetupMessage(webSocket)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("GeminiLive", "Received message: $text")
                onMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("GeminiLive", "WebSocket Failure: ${t.message}", t)
                onError(t.message ?: "Unknown error")
                onConnectionStatus(false)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("GeminiLive", "WebSocket Closing: $code / $reason")
                onConnectionStatus(false)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("GeminiLive", "WebSocket Closed: $code / $reason")
                onConnectionStatus(false)
            }
        })
    }

    private fun sendSetupMessage(ws: WebSocket) {
        // Initial setup message required by the Live API
        val setupJson = """
        {
          "setup": {
            "model": "models/gemini-2.5-flash"
          }
        }
        """.trimIndent()
        ws.send(setupJson)
    }

    fun sendText(text: String) {
        val textPayload = """
        {
          "clientContent": {
            "turns": [{
              "role": "user",
              "parts": [{"text": "$text"}]
            }]
          }
        }
        """.trimIndent()
        webSocket?.send(textPayload)
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
