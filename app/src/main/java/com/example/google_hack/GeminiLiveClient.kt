package com.example.google_hack

import android.util.Log
import okhttp3.*
import okio.ByteString

class GeminiLiveClient(private val onInterruption: (String) -> Unit) {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private val apiKey = BuildConfig.GEMINI_API_KEY

    // Use the v1beta BidiGenerateContent endpoint (Bi-directional)
    private val wssUrl = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent?key=$apiKey"

    fun connect() {
        val request = Request.Builder().url(wssUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("GeminiLive", "Connected! Sending Setup...")
                sendSetupMessage(webSocket)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                // Here we parse the server response
                // If the server sends {"setupComplete": {}}, we are officially live.
                Log.d("GeminiLive", "Server: $text")
                if (text.contains("modelTurn")) {
                    // Extract text from the nested JSON
                    onInterruption("AI Response Received")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("GeminiLive", "Failure: ${t.message}")
            }
        })
    }

    private fun sendSetupMessage(ws: WebSocket) {
        // This is the equivalent of the 'generate_content_config' in the Python code
        val setupJson = """
        {
          "setup": {
            "model": "models/gemini-2.5-flash-lite"
          }
        }
        """.trimIndent()
        ws.send(setupJson)
    }

    fun sendAudio(data: ByteArray) {
        // The Live API expects audio in a specific realtimeInput wrapper
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