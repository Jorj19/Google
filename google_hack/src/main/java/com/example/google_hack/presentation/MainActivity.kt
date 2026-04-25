package com.example.google_hack.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.example.google_hack.data.CustomSensorManager
import com.example.google_hack.data.GestureProcessor
import com.example.google_hack.data.HeartRateManager
import com.example.google_hack.data.MicManager
import com.example.google_hack.data.TinyMLDataProcessor
import com.example.google_hack.presentation.theme.Google_HackTheme
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var heartRateManager: HeartRateManager
    private lateinit var micManager: MicManager
    private lateinit var sensorManager: CustomSensorManager
    private val gestureProcessor = GestureProcessor()
    private val tinyMLProcessor = TinyMLDataProcessor()

    private var lastGesture by mutableStateOf("None")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate started")
        heartRateManager = HeartRateManager(this)
        micManager = MicManager()
        sensorManager = CustomSensorManager(this)

        setContent {
            WearApp(
                heartRateManager = heartRateManager,
                micManager = micManager,
                sensorManager = sensorManager,
                tinyMLProcessor = tinyMLProcessor,
                initialGesture = lastGesture,
                onGestureDetected = { lastGesture = it }
            )
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("MainActivity", "onKeyDown: $keyCode")
        return when (keyCode) {
            KeyEvent.KEYCODE_NAVIGATE_NEXT -> {
                Log.d("MainActivity", "Gesture: Flick Out")
                lastGesture = "Flick Out"
                true
            }
            KeyEvent.KEYCODE_NAVIGATE_PREVIOUS -> {
                Log.d("MainActivity", "Gesture: Flick In")
                lastGesture = "Flick In"
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}

@Composable
fun WearApp(
    heartRateManager: HeartRateManager,
    micManager: MicManager,
    sensorManager: CustomSensorManager,
    tinyMLProcessor: TinyMLDataProcessor,
    initialGesture: String,
    onGestureDetected: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var heartRate by remember { mutableStateOf(0.0) }
    var micAmplitude by remember { mutableStateOf(0) }
    var micRMS by remember { mutableStateOf(0.0) }
    var accelData by remember { mutableStateOf(floatArrayOf(0f, 0f, 0f)) }
    var lastGesture by remember { mutableStateOf(initialGesture) }
    
    var currentLabel by remember { mutableIntStateOf(0) } // 0 = Idle, 1 = Action
    var isStreaming by remember { mutableStateOf(false) }

    val gestureProcessor = remember { GestureProcessor() }

    var bodySensorsGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED)
    }
    var recordAudioGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        Log.d("MainActivity", "Permission result: $result")
        bodySensorsGranted = result[Manifest.permission.BODY_SENSORS] ?: bodySensorsGranted
        recordAudioGranted = result[Manifest.permission.RECORD_AUDIO] ?: recordAudioGranted
    }

    LaunchedEffect(initialGesture) {
        lastGesture = initialGesture
    }

    LaunchedEffect(bodySensorsGranted, recordAudioGranted) {
        if (!bodySensorsGranted || !recordAudioGranted) {
            Log.d("MainActivity", "Requesting missing permissions individually...")
            if (!bodySensorsGranted) {
                permissionLauncher.launch(arrayOf(Manifest.permission.BODY_SENSORS))
            } else if (!recordAudioGranted) {
                permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
            }
        }
    }

    LaunchedEffect(recordAudioGranted, isStreaming, currentLabel) {
        if (recordAudioGranted) {
            Log.d("MainActivity", "Starting Mic collection")
            launch {
                micManager.micAmplitudeFlow().collect { 
                    micAmplitude = it 
                    micRMS = tinyMLProcessor.processAudioSample(it)
                    if (isStreaming) {
                        tinyMLProcessor.formatAudioCsv(micRMS, currentLabel)
                    }
                }
            }
        }
    }

    LaunchedEffect(bodySensorsGranted, isStreaming, currentLabel) {
        if (bodySensorsGranted) {
            Log.d("MainActivity", "Starting HeartRate and Accel collection")
            launch {
                heartRateManager.heartRateFlow().collect { 
                    Log.d("MainActivity", "HR update: $it")
                    heartRate = it 
                }
            }
            launch {
                Log.d("MainActivity", "Subscribing to Accelerometer")
                sensorManager.accelerometerFlow().collect { data ->
                    accelData = data 
                    
                    if (isStreaming) {
                        tinyMLProcessor.formatAccelerometerCsv(data, currentLabel)
                    }

                    val gesture = gestureProcessor.processMotion(data)
                    if (gesture != null) {
                        Log.d("MainActivity", "Gesture detected in UI: $gesture")
                        lastGesture = gesture
                        onGestureDetected(gesture)
                    }
                }
            }
        }
    }

    Google_HackTheme {
        AppScaffold {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(
                scrollState = listState
            ) { contentPadding ->
                TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
                    item {
                        ListHeader(
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(text = "Real-time Data")
                        }
                    }
                    if (!bodySensorsGranted || !recordAudioGranted) {
                        item {
                            Button(
                                onClick = { 
                                    Log.d("MainActivity", "Grant Permissions button clicked")
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.BODY_SENSORS,
                                            Manifest.permission.RECORD_AUDIO
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                                transformation = SurfaceTransformation(transformationSpec),
                            ) {
                                Text("Grant Missing Permissions")
                            }
                        }
                    }
                    
                    item {
                        Button(
                            onClick = { isStreaming = !isStreaming },
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                            colors = if (isStreaming) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary) else ButtonDefaults.buttonColors()
                        ) {
                            Text(if (isStreaming) "Streaming CSV..." else "Start Streaming CSV")
                        }
                    }

                    item {
                        Button(
                            onClick = { currentLabel = if (currentLabel == 0) 1 else 0 },
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(if (currentLabel == 0) "Label 0: Idle" else "Label 1: Action")
                        }
                    }

                    item {
                        Card(
                            onClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec)
                                .padding(vertical = 4.dp),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Column {
                                Text(text = "Mic RMS (Lab 6)", style = MaterialTheme.typography.labelSmall)
                                Text(text = "%.2f".format(micRMS), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    item {
                        Card(
                            onClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec)
                                .padding(vertical = 4.dp),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Column {
                                Text(text = "Heart Rate", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = if (bodySensorsGranted) "${heartRate.toInt()} BPM" else "Permission Denied",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (bodySensorsGranted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    item {
                        Card(
                            onClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec)
                                .padding(vertical = 4.dp),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Column {
                                Text(text = "Mic Amplitude", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = if (recordAudioGranted) "$micAmplitude" else "Permission Denied",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (recordAudioGranted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    item {
                        Card(
                            onClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec)
                                .padding(vertical = 4.dp),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Column {
                                Text(text = "Last Gesture", style = MaterialTheme.typography.labelSmall)
                                Text(text = lastGesture, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    item {
                        Card(
                            onClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec)
                                .padding(vertical = 4.dp),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Column {
                                Text(text = "Accelerometer", style = MaterialTheme.typography.labelSmall)
                                Text(text = "X: %.2f".format(accelData[0]), style = MaterialTheme.typography.bodyMedium)
                                Text(text = "Y: %.2f".format(accelData[1]), style = MaterialTheme.typography.bodyMedium)
                                Text(text = "Z: %.2f".format(accelData[2]), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
