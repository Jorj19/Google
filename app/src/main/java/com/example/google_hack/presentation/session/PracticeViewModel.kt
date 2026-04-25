package com.example.google_hack.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.google_hack.data.models.AiFeedbackMessage
import com.example.google_hack.data.models.Project
import com.example.google_hack.data.models.SessionSummary
import com.example.google_hack.data.models.WatchSnapshot
import com.example.google_hack.data.repository.GeminiService
import com.example.google_hack.data.repository.SessionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp

sealed class PracticeState {
    object Idle : PracticeState()
    object Practicing : PracticeState()
    data class Finished(val report: String) : PracticeState()
    data class Error(val message: String) : PracticeState()
}

class PracticeViewModel(
    private val geminiService: GeminiService = GeminiService(),
    private val sessionRepository: SessionRepository = SessionRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow<PracticeState>(PracticeState.Idle)
    val state = _state.asStateFlow()

    private val _feedback = MutableStateFlow("Ready to start!")
    val feedback = _feedback.asStateFlow()

    private val _timer = MutableStateFlow(0)
    val timer = _timer.asStateFlow()

    private var sessionJob: Job? = null
    private var timerJob: Job? = null

    fun startPractice(userId: String, project: Project) {
        viewModelScope.launch {
            _state.value = PracticeState.Practicing
            geminiService.startSession(project)
            
            startTimer()
            startFeedbackLoop(userId, project.id)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _timer.value += 1
            }
        }
    }

    private fun startFeedbackLoop(userId: String, projectId: String) {
        sessionJob?.cancel()
        sessionJob = viewModelScope.launch {
            while (_state.value is PracticeState.Practicing) {
                delay(30000) // 30 seconds
                
                // Simulate audio and watch data for now
                val audioData = "[Audio recorded for the last 30s]"
                val watchData = WatchSnapshot(
                    heartRate = (70..110).random(),
                    stressEstimate = (0..100).random().toFloat() / 100f,
                    movementLevel = (0..100).random().toFloat() / 100f
                )

                val aiResponse = geminiService.getFeedback(audioData, watchData)
                if (aiResponse != null) {
                    _feedback.value = aiResponse
                    // Save to Firestore
                    sessionRepository.saveAiFeedbackMessage(
                        userId, projectId, "temp_session", 
                        AiFeedbackMessage(message = aiResponse, type = "REALTIME")
                    )
                }
            }
        }
    }

    fun endPractice() {
        viewModelScope.launch {
            sessionJob?.cancel()
            timerJob?.cancel()
            
            val report = geminiService.finishSession() ?: "Failed to generate report"
            _state.value = PracticeState.Finished(report)
        }
    }
}
