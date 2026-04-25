package com.example.google_hack.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class StorageRepository(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun uploadTranscript(
        userId: String,
        projectId: String,
        fileUri: Uri
    ): String {
        val storageRef = storage.reference.child("transcripts/$userId/$projectId/transcript.pdf")
        storageRef.putFile(fileUri).await()
        val downloadUrl = storageRef.downloadUrl.await().toString()
        
        updateProjectUrls(userId, projectId, transcriptUrl = downloadUrl)
        
        return downloadUrl
    }

    suspend fun uploadAudio(
        userId: String,
        projectId: String,
        fileUri: Uri
    ): String {
        val storageRef = storage.reference.child("audio/$userId/$projectId/speech_audio.mp3")
        storageRef.putFile(fileUri).await()
        val downloadUrl = storageRef.downloadUrl.await().toString()
        
        updateProjectUrls(userId, projectId, audioUrl = downloadUrl)
        
        return downloadUrl
    }

    private suspend fun updateProjectUrls(
        userId: String,
        projectId: String,
        transcriptUrl: String? = null,
        audioUrl: String? = null
    ) {
        val projectRef = firestore.collection("users")
            .document(userId)
            .collection("projects")
            .document(projectId)
        
        val updates = mutableMapOf<String, Any>()
        transcriptUrl?.let { updates["transcriptUrl"] = it }
        audioUrl?.let { updates["audioUrl"] = it }
        
        if (updates.isNotEmpty()) {
            projectRef.update(updates).await()
        }
    }
}
