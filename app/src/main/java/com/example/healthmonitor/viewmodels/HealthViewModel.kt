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
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import android.util.Log


class HealthViewModel(private val repository: HealthRepository) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _foods = MutableStateFlow<List<Food>>(emptyList())
    val foods: StateFlow<List<Food>> = _foods.asStateFlow()

    private val _healthDataList = MutableStateFlow<List<HealthData>>(emptyList())
    val healthDataList: StateFlow<List<HealthData>> = _healthDataList.asStateFlow()

    private val _nutritionDataList = MutableStateFlow<List<NutritionData>>(emptyList())
    val nutritionDataList: StateFlow<List<NutritionData>> = _nutritionDataList.asStateFlow()

    private val _todayCalories = MutableStateFlow(0)
    val todayCalories: StateFlow<Int> = _todayCalories.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Получаем пользователя
                val users = try {
                    repository.getAllUsers()
                } catch (e: Exception) {
                    Log.e("HealthViewModel", "Error getting users: ${e.message}")
                    null
                }

                if (users != null) {
                    users.collect { usersList ->
                        if (usersList.isNotEmpty()) {
                            val user = usersList.first()
                            _currentUser.value = user

                            // Загружаем данные асинхронно
                            loadHealthData(user.id)
                            loadNutritionData(user.id)
                            loadFoods()
                        } else {
                            createDefaultUser()
                        }
                    }
                } else {
                    createDefaultUser()
                }
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Error in loadInitialData: ${e.message}", e)
                createDefaultUser()
            }
        }
    }

    private fun createDefaultUser() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val testUser = User(
                    id = "user_1",
                    name = "-",
                    age = 18,
                    gender = "male",
                    heightCm = 180f,
                    targetWeight = 75f,
                    activityLevel = "moderate",
                    weightGoal = "maintain"
                )
                repository.insertUser(testUser)
                _currentUser.value = testUser
                loadHealthData("user_1")
                loadNutritionData("user_1")
                loadFoods()
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Error creating default user: ${e.message}", e)
            }
        }
    }

    private fun loadHealthData(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.getHealthDataByUser(userId).collect { data ->
                    _healthDataList.value = data
                }
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Error loading health data: ${e.message}")
                _healthDataList.value = emptyList()
            }
        }
    }

    private fun loadNutritionData(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.getNutritionDataByUser(userId).collect { data ->
                    _nutritionDataList.value = data
                }
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Error loading nutrition data: ${e.message}")
                _nutritionDataList.value = emptyList()
            }
        }
    }

    private fun loadFoods() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.getAllFoods().collect { foodsList ->
                    _foods.value = foodsList
                }
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Error loading foods: ${e.message}")
                _foods.value = emptyList()
            }
        }
    }

    fun addHealthData(weight: Float, heartRate: Int, sys: Int, dia: Int, steps: Int, sleep: Float, water: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
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

                // Перезагружаем данные
                val userId = _currentUser.value?.id ?: "user_1"
                loadHealthData(userId)
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Error adding health data: ${e.message}")
            }
        }
    }

    fun addNutritionData(food: Food, portionGrams: Float, mealType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
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

                // Перезагружаем данные
                val userId = _currentUser.value?.id ?: "user_1"
                loadNutritionData(userId)
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Error adding nutrition data: ${e.message}")
            }
        }
    }

    fun addFood(name: String, calories: Int, protein: Float, carbs: Float, fat: Float, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newFood = Food(
                    name = name,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat,
                    category = category
                )
                repository.insertFood(newFood)
                loadFoods()
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Error adding food: ${e.message}")
            }
        }
    }

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

        val dailyCalories = bmr * multiplier

        return when(user.weightGoal) {
            "lose" -> (dailyCalories * 0.85f).toInt()
            "maintain" -> dailyCalories.toInt()
            "gain" -> (dailyCalories * 1.15f).toInt()
            else -> dailyCalories.toInt()
        }
    }

    fun updateUser(name: String, age: Int, heightCm: Float, targetWeight: Float, activityLevel: String, weightGoal: String, dailyStepGoal: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updatedUser = _currentUser.value?.copy(
                    name = name,
                    age = age,
                    heightCm = heightCm,
                    targetWeight = targetWeight,
                    activityLevel = activityLevel,
                    weightGoal = weightGoal,
                    dailyStepGoal = dailyStepGoal
                )
                updatedUser?.let {
                    repository.updateUser(it)
                    _currentUser.value = it
                }
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Error updating user: ${e.message}")
            }
        }
    }
}
