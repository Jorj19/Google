package com.example.google_hack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.google_hack.ui.theme.DarkBlue
import com.example.google_hack.ui.theme.Google_HackTheme
import com.example.google_hack.ui.theme.TempoBlue

data class SpeechSession(
    val title: String,
    val target: String,
    val grade: Float
)

@Composable
fun MainScreen(onAddSpeechClick: () -> Unit) {
    val speeches = emptyList<SpeechSession>() // No speeches for now

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSpeechClick,
                containerColor = DarkBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Speech")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "My Speeches",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 24.dp)
            )

            if (speeches.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter // Moved lower
                ) {
                    Text(
                        text = "No speeches yet.\nTap + to create your first one!",
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        color = Color.Black, // Changed to black
                        modifier = Modifier.padding(bottom = 100.dp) // Offset from bottom
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(speeches) { speech ->
                        SpeechCard(speech)
                    }
                }
            }
        }
    }
}

@Composable
fun SpeechCard(speech: SpeechSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)), // Slight transparency
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = speech.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
            Text(
                text = "Target: ${speech.target}",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(speech.grade / 10f)
                                .height(8.dp)
                                .background(TempoBlue, RoundedCornerShape(4.dp))
                        )
                    }
                    Text(
                        text = "Grade: ${speech.grade}/10",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkBlue,
                        modifier = Modifier.padding(top = 8.dp)
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
        MainScreen(onAddSpeechClick = {})
    }
}
