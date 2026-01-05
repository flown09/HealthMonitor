package com.example.healthmonitor.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "foods")
data class Food(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val calories: Int,      // на 100г
    val protein: Float,     // на 100г
    val carbs: Float,       // на 100г
    val fat: Float,         // на 100г
    val fiber: Float = 0f,  // на 100г
    val category: String    // "meat", "dairy", "vegetables", "fruits", "grains"
)
