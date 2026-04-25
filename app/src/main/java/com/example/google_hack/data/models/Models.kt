package com.example.google_hack.data.models

import com.google.firebase.Timestamp

data class WatchSnapshot(
    val timestamp: Timestamp = Timestamp.now(),
    val heartRate: Int = 0,
    val accelerometerX: Float = 0f,
    val accelerometerY: Float = 0f,
    val accelerometerZ: Float = 0f,
    val gyroscopeX: Float = 0f,
    val gyroscopeY: Float = 0f,
    val gyroscopeZ: Float = 0f,
    val stressEstimate: Float = 0f,
    val movementLevel: Float = 0f
)

data class AiFeedbackMessage(
    val timestamp: Timestamp = Timestamp.now(),
    val message: String = "",
    val type: String = "",
    val triggeredBy: String = ""
)

data class SessionSummary(
    val sessionType: String = "",
    val startTime: Timestamp = Timestamp.now(),
    val endTime: Timestamp = Timestamp.now(),
    val durationSeconds: Int = 0,
    val overallScore: Float = 0f,
    val pacingScore: Float = 0f,
    val clarityScore: Float = 0f,
    val gestureScore: Float = 0f,
    val stressScore: Float = 0f,
    val fillerWordCount: Int = 0,
    val aiFinalSummary: String = ""
)

data class Project(
    val id: String = "",
    val title: String = "",
    val language: String = "",
    val targetAge: String = "",
    val domain: String = "",
    val transcriptText: String = "",
    val transcriptUrl: String = "",
    val audioUrl: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val averageGrade: Float = 0f
)
