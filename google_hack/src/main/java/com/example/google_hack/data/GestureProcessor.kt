package com.example.google_hack.data

import android.util.Log
import kotlin.math.abs

class GestureProcessor {

    private var lastAccelX = 0f
    private var lastAccelY = 0f
    private var lastAccelZ = 0f

    private var stillnessStartTime = System.currentTimeMillis()

    private var movementHistory = mutableListOf<Float>()
    private val HISTORY_SIZE = 50

    // For pattern detection
    private var lastPeakTime = 0L
    private var peakIntervals = mutableListOf<Long>()

    fun processMotion(accel: FloatArray): String? {
        val x = accel[0]
        val y = accel[1]
        val z = accel[2]
        val currentTime = System.currentTimeMillis()

        val deltaX = abs(x - lastAccelX)
        val deltaY = abs(y - lastAccelY)
        val deltaZ = abs(z - lastAccelZ)

        val currentIntensity = deltaX + deltaY + deltaZ

        movementHistory.add(currentIntensity)
        if (movementHistory.size > HISTORY_SIZE) movementHistory.removeAt(0)

        val avg = movementHistory.average().toFloat()
        val variance = movementHistory.map { (it - avg) * (it - avg) }.average().toFloat()

        var result: String? = null

        // ==============================
        // 1. OVERACTIVE / CHAOTIC
        // ==============================
        if (currentIntensity > 18f) {
            result = "Too fast! Calm your gestures"
        }

        // ==============================
        // 2. STILLNESS
        // ==============================
        if (avg < 0.5f) {
            if (currentTime - stillnessStartTime > 4000) {
                result = "Use your hands more!"
                stillnessStartTime = currentTime
            }
        } else {
            stillnessStartTime = currentTime
        }

        // ==============================
        // 3. FIDGETING (NEW)
        // ==============================
        // low intensity but high variance = jitter
        if (avg in 0.5f..3f && variance > 5f) {
            result = "Stop fidgeting!"
            Log.d("SpeechCoach", "Fidgeting detected")
        }

        // ==============================
        // 4. EMPHASIS GESTURE (NEW - GOOD)
        // ==============================
        // spike followed by calm
        if (currentIntensity > 12f && avg < 3f) {
            result = "Nice emphasis!"
            Log.d("SpeechCoach", "Good gesture detected")
        }

        // ==============================
        // 5. REPETITIVE PATTERN (NEW)
        // ==============================
        if (currentIntensity > 10f) {
            val interval = currentTime - lastPeakTime
            if (lastPeakTime != 0L) {
                peakIntervals.add(interval)
                if (peakIntervals.size > 5) peakIntervals.removeAt(0)

                val avgInterval = peakIntervals.average()

                // if intervals are very similar → repetitive
                val isRepeating = peakIntervals.all {
                    abs(it - avgInterval) < 200
                }

                if (isRepeating && peakIntervals.size >= 4) {
                    result = "You're repeating the same gesture"
                }
            }
            lastPeakTime = currentTime
        }

        // ==============================
        // 6. HAND RAISE (keep)
        // ==============================
        if (z > 8.5f && lastAccelZ <= 8.5f) {
            result = "Looking at watch?"
        }

        lastAccelX = x
        lastAccelY = y
        lastAccelZ = z

        return result
    }
}