package com.example.google_hack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.google_hack.data.models.Project
import com.example.google_hack.presentation.session.PracticeState
import com.example.google_hack.presentation.session.PracticeViewModel
import com.example.google_hack.ui.theme.DarkBlue
import com.example.google_hack.ui.theme.TempoBlue
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    project: Project,
    onBackClick: () -> Unit,
    viewModel: PracticeViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val feedback by viewModel.feedback.collectAsState()
    val timer by viewModel.timer.collectAsState()

    val minutes = timer / 60
    val seconds = timer % 60
    val timerText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project.title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = timerText,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )

            when (val s = state) {
                is PracticeState.Idle -> {
                    Button(
                        onClick = { viewModel.startPractice("test_user_id", project) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
                    ) {
                        Text("Start Practice")
                    }
                }
                is PracticeState.Practicing -> {
                    Card(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = feedback,
                                textAlign = TextAlign.Center,
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.endPractice() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Finish Session")
                    }
                }
                is PracticeState.Finished -> {
                    Card(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Performance Report",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = DarkBlue
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = s.report, color = Color.Black)
                        }
                    }

                    Button(
                        onClick = onBackClick,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
                    ) {
                        Text("Done")
                    }
                }
                is PracticeState.Error -> {
                    Text(text = s.message, color = Color.Red)
                    Button(onClick = onBackClick) { Text("Go Back") }
                }
            }
        }
    }
}
