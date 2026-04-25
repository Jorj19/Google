package com.example.google_hack.data

import android.content.Context
import android.util.Log
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DeltaDataType
import androidx.health.services.client.data.SampleDataPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.guava.await

class HeartRateManager(context: Context) {
    private val healthClient = HealthServices.getClient(context)
    private val measureClient = healthClient.measureClient

    fun heartRateFlow(): Flow<Double> = callbackFlow {
        Log.d("HeartRateManager", "Starting heart rate flow")
        val callback = object : MeasureCallback {
            override fun onAvailabilityChanged(dataType: DeltaDataType<*, *>, availability: Availability) {
                Log.d("HeartRateManager", "Availability changed: $availability")
            }

            override fun onDataReceived(data: DataPointContainer) {
                val heartRatePoints = data.getData(DataType.HEART_RATE_BPM)
                Log.d("HeartRateManager", "Data received: ${heartRatePoints.size} points")
                heartRatePoints.lastOrNull()?.let {
                    Log.d("HeartRateManager", "HR: ${it.value}")
                    trySend(it.value)
                }
            }
        }

        try {
            measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, callback)
            Log.d("HeartRateManager", "Callback registered")
        } catch (e: Exception) {
            Log.e("HeartRateManager", "Failed to register callback", e)
            close(e)
        }

        awaitClose {
            Log.d("HeartRateManager", "Unregistering callback")
            measureClient.unregisterMeasureCallbackAsync(DataType.HEART_RATE_BPM, callback)
        }
    }

    suspend fun hasHeartRateCapability(): Boolean {
        return try {
            val capabilities = measureClient.getCapabilitiesAsync().await()
            val supported = DataType.HEART_RATE_BPM in capabilities.supportedDataTypesMeasure
            Log.d("HeartRateManager", "Supports HR: $supported")
            supported
        } catch (e: Exception) {
            Log.e("HeartRateManager", "Error checking HR capability", e)
            false
        }
    }
}
