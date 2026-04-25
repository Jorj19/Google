package com.example.google_hack.data.repository

import com.example.google_hack.data.models.Project
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProjectRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getProjects(userId: String): Flow<List<Project>> = callbackFlow {
        val subscription = firestore.collection("users")
            .document(userId)
            .collection("projects")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val projects = snapshot.toObjects(Project::class.java)
                    trySend(projects)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun createProject(userId: String, project: Project): String {
        val projectRef = firestore.collection("users")
            .document(userId)
            .collection("projects")
            .document()
        
        val newProject = project.copy(id = projectRef.id)
        projectRef.set(newProject).await()
        return projectRef.id
    }
}
