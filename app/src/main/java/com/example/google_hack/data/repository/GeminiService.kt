package com.example.google_hack.data.repository

import com.example.google_hack.data.models.Project
import com.example.google_hack.data.models.WatchSnapshot
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService(
    apiKey: String = "AIzaSyBIxotAMvx5KMwVzbN0fmnVVgXwZE2E4nA" // USER_ACTION: Provide your Gemini API Key here
) {
    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    private var chat = model.startChat()

    suspend fun startSession(project: Project) = withContext(Dispatchers.IO) {
        val systemPrompt = """
            You are a public speaking coach. The user is practicing a speech 
            written for a ${project.targetAge} audience of many people. 
            The speech is in ${project.language}. Here is the full transcript: ${project.transcriptText}.
            Your job is to listen to how the user delivers this speech and give 
            short, specific, actionable feedback every 30 seconds. Also consider 
            their stress level and movement data from their smartwatch.
            Keep feedback under 2 sentences. Be encouraging but honest.
        """.trimIndent()
        
        chat = model.startChat(
            history = listOf(
                content(role = "user") { text(systemPrompt) },
                content(role = "model") { text("Understood. I am ready to provide feedback every 30 seconds. Please send the audio and watch data.") }
            )
        )
    }

    suspend fun getFeedback(
        audioData: String, // Simplified as string for now
        watchData: WatchSnapshot
    ): String? = withContext(Dispatchers.IO) {
        val prompt = """
            Audio chunk: $audioData
            Watch data: heart rate ${watchData.heartRate}, stress level ${watchData.stressEstimate}, movement level ${watchData.movementLevel}
            Give feedback on this segment.
        """.trimIndent()

        try {
            val response = chat.sendMessage(prompt)
            response.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun finishSession(): String? = withContext(Dispatchers.IO) {
        val finalPrompt = """
            Session is complete. Generate a full performance report with:
            - Overall score (0-10)
            - Pacing score
            - Clarity score  
            - Gesture score
            - Stress score
            - Filler word count
            - 3 things done well
            - 3 things to improve
            - General summary paragraph
        """.trimIndent()

        try {
            val response = chat.sendMessage(finalPrompt)
            response.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
