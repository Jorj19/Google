package com.example.google_hack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.google_hack.data.models.Project
import com.example.google_hack.presentation.home.HomeViewModel
import com.example.google_hack.ui.theme.DarkBlue
import com.example.google_hack.ui.theme.Google_HackTheme
import com.example.google_hack.ui.theme.TempoBlue
import com.example.google_hack.util.Resource

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    onAddSpeechClick: () -> Unit,
    onSpeechClick: (Project) -> Unit
) {
    val projectsState by homeViewModel.projectsState.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSpeechClick,
                containerColor = DarkBlue,
                contentColor = Color.White,
                shape = CircleShape,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Speech")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = "My Speeches",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 24.dp),
            )

            when (val resource = projectsState) {
                is Resource.Loading -> {
                    // Show a subtle progress bar at the top instead of full screen spinner
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = DarkBlue
                    )
                }
                is Resource.Success -> {
                    val projects = resource.data
                    if (projects.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            Text(
                                text = "No speeches yet.\nTap + to create your first one!",
                                textAlign = TextAlign.Center,
                                fontSize = 18.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 100.dp),
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 80.dp),
                        ) {
                            items(projects) { project ->
                                SpeechCard(project, onClick = { onSpeechClick(project) })
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = resource.message, color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun SpeechCard(project: Project, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = project.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue,
            )
            Text(
                text = "Target: ${project.targetAge} | Domain: ${project.domain}",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(4.dp)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(project.averageGrade / 10f)
                                .height(8.dp)
                                .background(TempoBlue, RoundedCornerShape(4.dp)),
                        )
                    }
                    Text(
                        text = "Grade: ${project.averageGrade}/10",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkBlue,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    Google_HackTheme {
        MainScreen(homeViewModel = viewModel(), onAddSpeechClick = {}, onSpeechClick = {})
    }
}
