package com.example.healthmonitor.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmonitor.models.User
import com.example.healthmonitor.models.Food
import com.example.healthmonitor.models.HealthData
import com.example.healthmonitor.models.NutritionData
import com.example.healthmonitor.repository.HealthRepository
import com.example.healthmonitor.utils.HealthCalculations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Calendar

class HealthViewModel(private val repository: HealthRepository) : ViewModel() {

    // User State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Foods State
    private val _foods = MutableStateFlow<List<Food>>(emptyList())
    val foods: StateFlow<List<Food>> = _foods.asStateFlow()

    // Health Data State
    private val _healthDataList = MutableStateFlow<List<HealthData>>(emptyList())
    val healthDataList: StateFlow<List<HealthData>> = _healthDataList.asStateFlow()

    // Nutrition Data State
    private val _nutritionDataList = MutableStateFlow<List<NutritionData>>(emptyList())
    val nutritionDataList: StateFlow<List<NutritionData>> = _nutritionDataList.asStateFlow()

    // Today's calories
    private val _todayCalories = MutableStateFlow(0)
    val todayCalories: StateFlow<Int> = _todayCalories.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        // Создаем тестового пользователя если его нет
        viewModelScope.launch {
            val testUser = User(
                id = "user_1",
                name = "Иван",
                age = 30,
                gender = "male",
                heightCm = 180f,
                targetWeight = 75f,
                activityLevel = "moderate"
            )
            repository.insertUser(testUser)
            _currentUser.value = testUser

            loadFoods()
            loadHealthData("user_1")
            loadNutritionData("user_1")
        }
    }

    private fun loadFoods() {
        viewModelScope.launch {
            repository.getAllFoods().collect { foods ->
                _foods.value = foods

                // Если продуктов нет - добавляем предзаполненные
                if (foods.isEmpty()) {
                    insertDefaultFoods()
                }
            }
        }
    }

    private fun insertDefaultFoods() {
        viewModelScope.launch {
            val defaultFoods = listOf(
                // Мясо
                Food("f1", "Курица (грудка)", 165, 31f, 0f, 3.6f, 0f, "meat"),
                Food("f2", "Говядина (нежирная)", 250, 26f, 0f, 17f, 0f, "meat"),
                Food("f3", "Рыба (лосось)", 208, 20f, 0f, 13f, 0f, "meat"),

                // Молочные продукты
                Food("f4", "Молоко (обезжиренное)", 30, 3.2f, 4.8f, 0.1f, 0f, "dairy"),
                Food("f5", "Йогурт (греческий)", 59, 10f, 3.3f, 0.5f, 0f, "dairy"),
                Food("f6", "Сыр (твердый)", 402, 25f, 1.3f, 33f, 0f, "dairy"),

                // Овощи
                Food("f7", "Брокколи", 34, 2.8f, 7f, 0.4f, 2.4f, "vegetables"),
                Food("f8", "Морковь", 41, 0.9f, 10f, 0.2f, 2.8f, "vegetables"),
                Food("f9", "Помидоры", 18, 0.9f, 3.9f, 0.2f, 1.2f, "vegetables"),

                // Фрукты
                Food("f10", "Банан", 89, 1.1f, 23f, 0.3f, 2.6f, "fruits"),
                Food("f11", "Яблоко", 52, 0.3f, 14f, 0.2f, 2.4f, "fruits"),
                Food("f12", "Апельсин", 47, 0.9f, 12f, 0.1f, 2.4f, "fruits"),

                // Зерна
                Food("f13", "Рис (белый)", 130, 2.7f, 28f, 0.3f, 0.4f, "grains"),
                Food("f14", "Хлеб (ржаной)", 259, 8.5f, 49f, 3.3f, 7f, "grains"),
                Food("f15", "Овсянка", 389, 17f, 67f, 7f, 10.6f, "grains"),
            )
            repository.insertFoods(defaultFoods)
        }
    }

    private fun loadHealthData(userId: String) {
        viewModelScope.launch {
            repository.getHealthDataByUser(userId).collect { data ->
                _healthDataList.value = data
            }
        }
    }

    private fun loadNutritionData(userId: String) {
        viewModelScope.launch {
            repository.getNutritionDataByDate(userId, getTodayTimestamp()).collect { data ->
                _nutritionDataList.value = data
                val totalCalories = data.sumOf { it.calories }
                _todayCalories.value = totalCalories
            }
        }
    }

    // Add health data
    fun addHealthData(weight: Float, heartRate: Int, sys: Int, dia: Int, steps: Int, sleep: Float, water: Float) {
        viewModelScope.launch {
            val healthData = HealthData(
                userId = _currentUser.value?.id ?: "user_1",
                date = getTodayTimestamp(),
                weight = weight,
                heartRate = heartRate,
                bloodPressureSystolic = sys,
                bloodPressureDiastolic = dia,
                steps = steps,
                sleepHours = sleep,
                waterIntakeL = water
            )
            repository.insertHealthData(healthData)
        }
    }

    // Add nutrition data
    fun addNutritionData(food: Food, portionGrams: Float, mealType: String) {
        viewModelScope.launch {
            val caloriesForPortion = (food.calories * portionGrams) / 100

            val nutritionData = NutritionData(
                userId = _currentUser.value?.id ?: "user_1",
                date = getTodayTimestamp(),
                mealType = mealType,
                foodName = food.name,
                calories = caloriesForPortion.toInt(),
                protein = (food.protein * portionGrams) / 100,
                carbs = (food.carbs * portionGrams) / 100,
                fat = (food.fat * portionGrams) / 100,
                fiber = (food.fiber * portionGrams) / 100
            )
            repository.insertNutritionData(nutritionData)
        }
    }

    // Add new food
    fun addFood(name: String, calories: Int, protein: Float, carbs: Float, fat: Float, category: String) {
        viewModelScope.launch {
            val newFood = Food(
                name = name,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                category = category
            )
            repository.insertFood(newFood)
        }
    }

    // Вспомогательные функции
    private fun getTodayTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun calculateBMI(): Float {
        val user = _currentUser.value ?: return 0f
        val lastHealth = _healthDataList.value.firstOrNull() ?: return 0f
        return HealthCalculations.calculateBMI(lastHealth.weight, user.heightCm)
    }

    fun calculateDailyCalories(): Int {
        val user = _currentUser.value ?: return 0
        val lastHealth = _healthDataList.value.firstOrNull()
        val weight = lastHealth?.weight ?: user.targetWeight

        val bmr = HealthCalculations.calculateBMR(
            user.age * 365,
            weight,
            user.heightCm,
            user.gender == "male"
        )

        val multiplier = when(user.activityLevel) {
            "sedentary" -> 1.2f
            "light" -> 1.375f
            "moderate" -> 1.55f
            "active" -> 1.725f
            "very_active" -> 1.9f
            else -> 1.5f
        }

        return (bmr * multiplier).toInt()
    }
}
