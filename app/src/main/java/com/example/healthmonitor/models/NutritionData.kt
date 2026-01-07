package com.example.healthmonitor.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "nutrition_data")
data class NutritionData(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val date: Long,
    val mealType: String,
    val foodName: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val fiber: Float,
    val portionGrams: Float = 100f
)
