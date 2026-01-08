package com.example.healthmonitor.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar

class StepCounter(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val sharedPreferences = context.getSharedPreferences("step_counter", Context.MODE_PRIVATE)

    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount

    private var isListening = false
    private var baseSteps = 0  // Значение счётчика в начале дня
    private var currentTotalSteps = 0  // Полное значение с датчика

    init {
        if (stepSensor != null) {
            Log.d("StepCounter", "Step sensor found: ${stepSensor.name}")
            // Загружаем базовое значение для сегодня
            loadTodayBaseSteps()
        } else {
            Log.w("StepCounter", "Step sensor NOT found on this device")
        }
    }

    private fun loadTodayBaseSteps() {
        val today = getTodayKey()
        val savedToday = sharedPreferences.getString("last_date", "")

        baseSteps = if (savedToday == today) {
            // Это тот же день, загружаем базовое значение
            sharedPreferences.getInt("base_steps_$today", 0)
        } else {
            // Новый день! Сохраняем сегодня как "последний день"
            sharedPreferences.edit().putString("last_date", today).apply()
            // Базовое значение будет установлено при первом обновлении датчика
            0
        }

        Log.d("StepCounter", "Loaded base steps: $baseSteps for date: $today")
    }

    private fun getTodayKey(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"
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
            currentTotalSteps = event.values[0].toInt()

            // Если baseSteps ещё не установлен (первый запуск в этот день)
            if (baseSteps == 0 && currentTotalSteps > 0) {
                baseSteps = currentTotalSteps
                val today = getTodayKey()
                sharedPreferences.edit().putInt("base_steps_$today", baseSteps).apply()
                Log.d("StepCounter", "Set base steps: $baseSteps for today")
            }

            // Вычисляем шаги за текущий день
            val todaySteps = (currentTotalSteps - baseSteps).coerceAtLeast(0)
            _stepCount.value = todaySteps
            Log.d("StepCounter", "Total: $currentTotalSteps, Base: $baseSteps, Today: $todaySteps")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Ничего не делаем
    }
}
