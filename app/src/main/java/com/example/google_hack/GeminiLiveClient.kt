package com.example.google_hack

import android.util.Log
import okhttp3.*

class GeminiLiveClient(private val onInterruption: (String) -> Unit) {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    // Access the API key you set up in local.properties
    private val apiKey = BuildConfig.GEMINI_API_KEY

    // The specific WebSocket URL for the Gemini Multimodal Live API
    private val wssUrl = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent?key=$apiKey"

    fun connect() {
        Log.d("GeminiLive", "Attempting connection...")
        val request = Request.Builder().url(wssUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("GeminiLive", "Socket Opened! Sending Setup Message...")
                // You MUST send the setup payload immediately upon opening
                sendSetupMessage(webSocket)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                // This logs the raw JSON coming back from Google's servers
                Log.d("GeminiLive", "Received: $text")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("GeminiLive", "Socket Error: ${t.message}", t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("GeminiLive", "Socket Closed: $reason")
            }
        })
    }

    private fun sendSetupMessage(ws: WebSocket) {
        // The JSON payload instructing the AI how to behave
        val setupPayload = """
            {
              "setup": {
                "model": "models/gemini-2.0-flash-exp",
                "systemInstruction": {
                  "parts": [{"text": "You are an expert public speaking coach monitoring live audio. If the speaker uses excessive filler words, speaks too fast, or their heart rate spikes, immediately output a 3-word visual cue (e.g., 'SLOW DOWN', 'BREATHE'). If they are doing fine, output nothing."}]
                }
              }
            }
        """.trimIndent()

        ws.send(setupPayload)
    }

    fun disconnect() {
        webSocket?.close(1000, "User requested close")
        webSocket = null
    }
}