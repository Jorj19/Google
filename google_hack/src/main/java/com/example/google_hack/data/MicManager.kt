package com.example.google_hack.data

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

class MicManager {
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    @SuppressLint("MissingPermission")
    fun micAmplitudeFlow(): Flow<Int> = callbackFlow {
        Log.d("MicManager", "Starting mic flow, minBufferSize: $bufferSize")
        
        if (bufferSize <= 0) {
            Log.e("MicManager", "Invalid buffer size: $bufferSize")
            close(Exception("Invalid buffer size"))
            return@callbackFlow
        }

        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("MicManager", "AudioRecord initialization failed")
            close(Exception("AudioRecord initialization failed"))
            return@callbackFlow
        }

        audioRecord.startRecording()
        Log.d("MicManager", "Recording started")

        val job = launch(Dispatchers.IO) {
            val buffer = ShortArray(bufferSize)
            while (isActive && audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                val read = audioRecord.read(buffer, 0, bufferSize)
                if (read > 0) {
                    var max = 0
                    for (i in 0 until read) {
                        val absValue = abs(buffer[i].toInt())
                        if (absValue > max) max = absValue
                    }
                    // Log.v("MicManager", "Max amplitude: $max")
                    trySend(max)
                } else {
                    Log.w("MicManager", "Read error: $read")
                }
            }
        }

        awaitClose {
            job.cancel()
            audioRecord.stop()
            audioRecord.release()
        }
    }
}
