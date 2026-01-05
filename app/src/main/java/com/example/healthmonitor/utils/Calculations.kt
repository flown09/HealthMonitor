package com.example.healthmonitor.utils

object HealthCalculations {
    // BMI (Индекс массы тела)
    fun calculateBMI(weightKg: Float, heightCm: Float): Float {
        val heightM = heightCm / 100
        return weightKg / (heightM * heightM)
    }

    // BMI категория
    fun getBMICategory(bmi: Float): String = when {
        bmi < 18.5 -> "Недостаток веса"
        bmi < 25 -> "Нормальный вес"
        bmi < 30 -> "Избыточный вес"
        else -> "Ожирение"
    }

    // Базовый метаболизм (Harris-Benedict)
    fun calculateBMR(ageDays: Int, weightKg: Float, heightCm: Float, isMale: Boolean): Float {
        val age = ageDays / 365
        return if (isMale) {
            88.362f + (13.397f * weightKg) + (4.799f * heightCm) - (5.677f * age)
        } else {
            447.593f + (9.247f * weightKg) + (3.098f * heightCm) - (4.330f * age)
        }
    }

    // Суточная норма калорий
    fun calculateDailyCalories(bmr: Float, activityMultiplier: Float): Int {
        return (bmr * activityMultiplier).toInt()
    }

    // Макронутриенты (соотношение 40-30-30)
    fun calculateMacronutrients(calorieGoal: Int): MacroNutrients {
        return MacroNutrients(
            proteinG = (calorieGoal * 0.30 / 4).toInt(), // 4 cal/g
            carbsG = (calorieGoal * 0.40 / 4).toInt(),   // 4 cal/g
            fatG = (calorieGoal * 0.30 / 9).toInt()      // 9 cal/g
        )
    }

    // Норма воды (30-35 мл на кг веса)
    fun calculateWaterIntake(weightKg: Float): Float {
        return (weightKg * 35) / 1000
    }
}

data class MacroNutrients(
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int
)

// Активность уровень множитель
fun getActivityMultiplier(level: String): Float = when (level) {
    "sedentary" -> 1.2f
    "light" -> 1.375f
    "moderate" -> 1.55f
    "active" -> 1.725f
    "very_active" -> 1.9f
    else -> 1.5f
}
