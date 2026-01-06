package com.example.healthmonitor.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StepCounter(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount

    private var isListening = false

    init {
        // Логируем наличие датчика
        if (stepSensor != null) {
            Log.d("StepCounter", "Step sensor found: ${stepSensor.name}")
        } else {
            Log.w("StepCounter", "Step sensor NOT found on this device")
        }
    }

    fun startListening() {
        if (stepSensor == null) {
            Log.w("StepCounter", "Cannot start listening - sensor not available")
            return
        }

        if (!isListening) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
            isListening = true
            Log.d("StepCounter", "Started listening to step sensor")
        }
    }

    fun stopListening() {
        if (isListening) {
            sensorManager.unregisterListener(this)
            isListening = false
            Log.d("StepCounter", "Stopped listening to step sensor")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val steps = event.values[0].toInt()
            _stepCount.value = steps
            Log.d("StepCounter", "Steps: $steps")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Ничего не делаем
    }
}
