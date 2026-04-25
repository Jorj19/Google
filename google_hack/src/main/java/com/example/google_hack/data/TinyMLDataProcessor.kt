package com.example.google_hack.data

import android.util.Log
import kotlin.math.sqrt

class TinyMLDataProcessor {
    
    // --- Accelerometer Processing ---
    fun formatAccelerometerCsv(accel: FloatArray, label: Int): String {
        val timestamp = System.currentTimeMillis()
        // Format: timestamp, x, y, z, label
        val csv = "$timestamp,${accel[0]},${accel[1]},${accel[2]},$label"
        Log.d("TinyML_Accel", csv)
        return csv
    }

    // --- Microphone Processing ---
    // RMS = sqrt( (sum of squares of samples) / number of samples )
    private val audioBuffer = mutableListOf<Int>()
    private val maxBufferSize = 44100 // Approx 1 second at 44.1kHz (Mono)

    fun processAudioSample(amplitude: Int): Double {
        audioBuffer.add(amplitude)
        if (audioBuffer.size > 1000) { // Sliding window of last 1000 samples for real-time feel
            audioBuffer.removeAt(0)
        }
        
        if (audioBuffer.isEmpty()) return 0.0
        
        val sumOfSquares = audioBuffer.fold(0.0) { acc, sample ->
            acc + (sample.toDouble() * sample.toDouble())
        }
        val rms = sqrt(sumOfSquares / audioBuffer.size)
        
        // Log if RMS is above a "gating" threshold (e.g., 500)
        if (rms > 500) {
            // Log.d("TinyML_Audio", "RMS: $rms")
        }
        
        return rms
    }

    fun formatAudioCsv(rms: Double, label: Int): String {
        val timestamp = System.currentTimeMillis()
        val csv = "$timestamp,$rms,$label"
        Log.d("TinyML_Audio", csv)
        return csv
    }
}
