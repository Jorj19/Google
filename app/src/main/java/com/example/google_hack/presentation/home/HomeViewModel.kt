package com.example.google_hack.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.google_hack.data.models.Project
import com.example.google_hack.data.repository.ProjectRepository
import com.example.google_hack.util.Resource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: ProjectRepository = ProjectRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _projectsState = MutableStateFlow<Resource<List<Project>>>(Resource.Success(emptyList()))
    val projectsState: StateFlow<Resource<List<Project>>> = _projectsState.asStateFlow()

    private val _createProjectState = MutableStateFlow<Resource<String>>(Resource.Success(""))
    val createProjectState: StateFlow<Resource<String>> = _createProjectState.asStateFlow()

    init {
        fetchProjects()
    }

    fun fetchProjects() {
        val userId = auth.currentUser?.uid ?: "test_user_id"
        viewModelScope.launch {
            // Start loading in background but don't clear current list
            repository.getProjects(userId)
                .catch { e ->
                    _projectsState.value = Resource.Error(e.message ?: "Failed to fetch speeches", e)
                }
                .collect { projects ->
                    _projectsState.value = Resource.Success(projects)
                }
        }
    }

    fun createProject(project: Project, onComplete: () -> Unit) {
        val userId = auth.currentUser?.uid ?: "test_user_id"
        
        // Optimistic Update: Add to list immediately
        val currentList = (_projectsState.value as? Resource.Success)?.data ?: emptyList()
        _projectsState.value = Resource.Success(listOf(project) + currentList)
        
        // Close screen immediately
        onComplete()

        viewModelScope.launch {
            try {
                repository.createProject(userId, project)
                _createProjectState.value = Resource.Success("Success")
            } catch (e: Exception) {
                // In case of error, we could revert or show a toast
                _createProjectState.value = Resource.Error(e.message ?: "Failed to create speech", e)
            }
        }
    }
}
