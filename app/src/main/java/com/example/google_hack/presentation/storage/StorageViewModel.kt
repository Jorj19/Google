package com.example.google_hack.presentation.storage

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.google_hack.data.repository.StorageRepository
import com.example.google_hack.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StorageViewModel(
    private val repository: StorageRepository = StorageRepository()
) : ViewModel() {

    private val _uploadState = MutableStateFlow<Resource<String>>(Resource.Success(""))
    val uploadState: StateFlow<Resource<String>> = _uploadState

    fun uploadTranscript(userId: String, projectId: String, fileUri: Uri) {
        viewModelScope.launch {
            _uploadState.value = Resource.Loading
            try {
                val url = repository.uploadTranscript(userId, projectId, fileUri)
                _uploadState.value = Resource.Success(url)
            } catch (e: Exception) {
                _uploadState.value = Resource.Error(e.message ?: "Unknown error", e)
            }
        }
    }

    fun uploadAudio(userId: String, projectId: String, fileUri: Uri) {
        viewModelScope.launch {
            _uploadState.value = Resource.Loading
            try {
                val url = repository.uploadAudio(userId, projectId, fileUri)
                _uploadState.value = Resource.Success(url)
            } catch (e: Exception) {
                _uploadState.value = Resource.Error(e.message ?: "Unknown error", e)
            }
        }
    }
}
