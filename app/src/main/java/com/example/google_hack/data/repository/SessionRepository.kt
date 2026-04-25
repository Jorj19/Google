package com.example.google_hack.data.repository

import com.example.google_hack.data.models.AiFeedbackMessage
import com.example.google_hack.data.models.SessionSummary
import com.example.google_hack.data.models.WatchSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SessionRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun createSession(
        userId: String,
        projectId: String,
        sessionSummary: SessionSummary
    ): String {
        val sessionRef = firestore.collection("users")
            .document(userId)
            .collection("projects")
            .document(projectId)
            .collection("sessions")
            .document()
        
        sessionRef.set(sessionSummary).await()
        return sessionRef.id
    }

    suspend fun saveWatchSnapshot(
        userId: String,
        projectId: String,
        sessionId: String,
        snapshot: WatchSnapshot
    ) {
        firestore.collection("users")
            .document(userId)
            .collection("projects")
            .document(projectId)
            .collection("sessions")
            .document(sessionId)
            .collection("watchSnapshots")
            .add(snapshot)
            .await()
    }

    suspend fun saveAiFeedbackMessage(
        userId: String,
        projectId: String,
        sessionId: String,
        message: AiFeedbackMessage
    ) {
        firestore.collection("users")
            .document(userId)
            .collection("projects")
            .document(projectId)
            .collection("sessions")
            .document(sessionId)
            .collection("aiFeedbackMessages")
            .add(message)
            .await()
    }

    suspend fun updateSessionSummary(
        userId: String,
        projectId: String,
        sessionId: String,
        summary: SessionSummary
    ) {
        firestore.collection("users")
            .document(userId)
            .collection("projects")
            .document(projectId)
            .collection("sessions")
            .document(sessionId)
            .set(summary)
            .await()
    }
}
