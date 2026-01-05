package com.example.healthmonitor.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "health_data")
data class HealthData(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val date: Long,
    val weight: Float,
    val heartRate: Int,
    val bloodPressureSystolic: Int,
    val bloodPressureDiastolic: Int,
    val steps: Int,
    val sleepHours: Float,
    val waterIntakeL: Float
)
