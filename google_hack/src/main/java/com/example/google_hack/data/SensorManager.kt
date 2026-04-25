package com.example.google_hack.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class CustomSensorManager(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    // Try using TYPE_LINEAR_ACCELERATION (no gravity) or raw TYPE_ACCELEROMETER
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    fun accelerometerFlow(): Flow<FloatArray> = callbackFlow {
        Log.d("SensorManager", "Starting accelerometer flow. Sensor: $accelerometer")
        
        val sensor = accelerometer ?: sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        
        if (sensor == null) {
            Log.e("SensorManager", "No accelerometer or linear acceleration sensor found")
            close()
            return@callbackFlow
        }
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    // Log.d("SensorManager", "Raw: X=${it.values[0]}, Y=${it.values[1]}, Z=${it.values[2]}")
                    trySend(it.values.copyOf())
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        val registered = sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        Log.d("SensorManager", "Accelerometer registered ($sensor): $registered")

        awaitClose {
            Log.d("SensorManager", "Unregistering Accelerometer")
            sensorManager.unregisterListener(listener)
        }
    }

    fun gyroscopeFlow(): Flow<FloatArray> = callbackFlow {
        Log.d("SensorManager", "Starting gyroscope flow. Sensor: $gyroscope")
        if (gyroscope == null) {
            Log.e("SensorManager", "Gyroscope not found")
            close()
            return@callbackFlow
        }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    trySend(it.values.copyOf())
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, gyroscope, SensorManager.SENSOR_DELAY_UI)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
