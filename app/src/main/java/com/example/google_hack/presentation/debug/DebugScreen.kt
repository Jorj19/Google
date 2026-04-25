package com.example.google_hack.presentation.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.google_hack.data.models.SessionSummary
import com.example.google_hack.presentation.session.SessionViewModel
import com.example.google_hack.presentation.storage.StorageViewModel
import com.example.google_hack.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    onBack: () -> Unit,
    sessionViewModel: SessionViewModel = viewModel(),
    storageViewModel: StorageViewModel = viewModel()
) {
    val user = FirebaseAuth.getInstance().currentUser
    val sessionState by sessionViewModel.sessionCreationState.collectAsState()
    val storageState by storageViewModel.uploadState.collectAsState()
    val operationState by sessionViewModel.operationState.collectAsState()

    var lastCreatedSessionId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sessionState) {
        if (sessionState is Resource.Success) {
            lastCreatedSessionId = (sessionState as Resource.Success<String>).data
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Database Debug") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("User ID: ${user?.uid ?: "Not Logged In"}", style = MaterialTheme.typography.bodySmall)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Firestore Operations", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            user?.let {
                                sessionViewModel.createSession(
                                    it.uid,
                                    "debug_project",
                                    SessionSummary(
                                        sessionType = "Debug Practice",
                                        overallScore = 90f,
                                        aiFinalSummary = "This is a debug session"
                                    )
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create Test Session")
                    }

                    if (lastCreatedSessionId != null) {
                        Text("Last Session ID: $lastCreatedSessionId", style = MaterialTheme.typography.bodySmall)
                        
                        Button(
                            onClick = {
                                // Logic for saving snapshot or feedback could go here
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false // Placeholder
                        ) {
                            Text("Save Test Snapshot (N/A)")
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Storage Operations", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Note: These require a valid file Uri. This is a placeholder UI.", style = MaterialTheme.typography.bodySmall)
                    
                    Button(
                        onClick = { /* In a real app, use an intent to pick a file */ },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    ) {
                        Text("Upload Test Transcript")
                    }
                }
            }

            // Status Section
            Text("Status:", style = MaterialTheme.typography.titleSmall)
            when (val state = sessionState) {
                is Resource.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                is Resource.Error -> Text("Session Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                is Resource.Success -> Text("Session OK: ${state.data}", color = MaterialTheme.colorScheme.primary)
                else -> {}
            }
            
            when (val state = storageState) {
                is Resource.Error -> Text("Storage Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                is Resource.Success -> if (state.data.isNotEmpty()) Text("Upload OK: ${state.data}", color = MaterialTheme.colorScheme.primary)
                else -> {}
            }
        }
    }
}
