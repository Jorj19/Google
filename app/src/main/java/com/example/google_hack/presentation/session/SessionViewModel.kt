package com.example.google_hack.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.google_hack.data.models.AiFeedbackMessage
import com.example.google_hack.data.models.SessionSummary
import com.example.google_hack.data.models.WatchSnapshot
import com.example.google_hack.data.repository.SessionRepository
import com.example.google_hack.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SessionViewModel(
    private val repository: SessionRepository = SessionRepository()
) : ViewModel() {

    private val _sessionCreationState = MutableStateFlow<Resource<String>>(Resource.Loading)
    val sessionCreationState: StateFlow<Resource<String>> = _sessionCreationState

    private val _operationState = MutableStateFlow<Resource<Unit>>(Resource.Success(Unit))
    val operationState: StateFlow<Resource<Unit>> = _operationState

    fun createSession(userId: String, projectId: String, summary: SessionSummary) {
        viewModelScope.launch {
            _sessionCreationState.value = Resource.Loading
            try {
                val sessionId = repository.createSession(userId, projectId, summary)
                _sessionCreationState.value = Resource.Success(sessionId)
            } catch (e: Exception) {
                _sessionCreationState.value = Resource.Error(e.message ?: "Unknown error", e)
            }
        }
    }

    fun saveWatchSnapshot(userId: String, projectId: String, sessionId: String, snapshot: WatchSnapshot) {
        viewModelScope.launch {
            _operationState.value = Resource.Loading
            try {
                repository.saveWatchSnapshot(userId, projectId, sessionId, snapshot)
                _operationState.value = Resource.Success(Unit)
            } catch (e: Exception) {
                _operationState.value = Resource.Error(e.message ?: "Unknown error", e)
            }
        }
    }

    fun saveAiFeedbackMessage(userId: String, projectId: String, sessionId: String, message: AiFeedbackMessage) {
        viewModelScope.launch {
            _operationState.value = Resource.Loading
            try {
                repository.saveAiFeedbackMessage(userId, projectId, sessionId, message)
                _operationState.value = Resource.Success(Unit)
            } catch (e: Exception) {
                _operationState.value = Resource.Error(e.message ?: "Unknown error", e)
            }
        }
    }

    fun updateSessionSummary(userId: String, projectId: String, sessionId: String, summary: SessionSummary) {
        viewModelScope.launch {
            _operationState.value = Resource.Loading
            try {
                repository.updateSessionSummary(userId, projectId, sessionId, summary)
                _operationState.value = Resource.Success(Unit)
            } catch (e: Exception) {
                _operationState.value = Resource.Error(e.message ?: "Unknown error", e)
            }
        }
    }
}
