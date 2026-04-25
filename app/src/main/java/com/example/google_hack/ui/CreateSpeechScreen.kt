package com.example.google_hack.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.google_hack.ui.theme.DarkBlue
import com.example.google_hack.ui.theme.Google_HackTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSpeechScreen(onBackClick: () -> Unit, onSubmitClick: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var transcriptText by remember { mutableStateOf("") }
    var selectedFileName by remember { mutableStateOf("") }
    var useManualEntry by remember { mutableStateOf(value = false) }

    var selectedLanguage by remember { mutableStateOf("English") }
    var selectedAge by remember { mutableStateOf("18+") }
    var selectedDomain by remember { mutableStateOf("IT") }

    val languages = listOf("English", "Romanian")
    val ages = listOf("18-", "18+", "30+", "50+", "60+")
    val domains = listOf("IT", "School", "University", "Work", "Education", "Healthcare")

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
        focusedContainerColor = Color.White.copy(alpha = 0.8f),
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        focusedLabelColor = Color.Black,
        unfocusedLabelColor = Color.Black.copy(alpha = 0.7f),
    )

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        selectedFileName = uri?.let { "Selected: ${it.path?.substringAfterLast("/")}" } ?: ""
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Create New Speech", 
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = Color.Black,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Speech Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                textStyle = TextStyle(color = Color.Black),
            )

            // Transcript Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Transcript", fontWeight = FontWeight.Bold, color = Color.Black)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { 
                            useManualEntry = false
                            filePickerLauncher.launch(arrayOf("text/plain", "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (!useManualEntry) DarkBlue else Color.LightGray),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Upload File")
                    }
                    
                    Button(
                        onClick = { useManualEntry = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (useManualEntry) DarkBlue else Color.LightGray),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("Manual Entry")
                    }
                }

                if (useManualEntry) {
                    OutlinedTextField(
                        value = transcriptText,
                        onValueChange = { transcriptText = it },
                        label = { Text("Type transcript here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        textStyle = TextStyle(color = Color.Black),
                    )
                } else if (selectedFileName.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = selectedFileName,
                            modifier = Modifier.padding(16.dp),
                            color = Color.Black,
                        )
                    }
                }
            }

            // Dropdowns
            TempoDropdown(label = "Language", options = languages, selectedOption = selectedLanguage) { selectedLanguage = it }
            TempoDropdown(label = "Target Audience Age", options = ages, selectedOption = selectedAge) { selectedAge = it }
            TempoDropdown(label = "Domain", options = domains, selectedOption = selectedDomain) { selectedDomain = it }

            Button(
                onClick = onSubmitClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
            ) {
                Text("Submit", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TempoDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(value = false) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, fontWeight = FontWeight.Medium, color = Color.Black, fontSize = 14.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = selectedOption, color = Color.Black)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Black)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .background(Color.White),
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = Color.Black) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateSpeechScreenPreview() {
    Google_HackTheme {
        CreateSpeechScreen(onBackClick = {}, onSubmitClick = {})
    }
}
