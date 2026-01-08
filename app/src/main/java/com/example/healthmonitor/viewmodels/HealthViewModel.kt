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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first



class HealthViewModel(private val repository: HealthRepository) : ViewModel() {

    private val _todayMacros = MutableStateFlow(Triple(0, 0, 0))
    val todayMacros: StateFlow<Triple<Int, Int, Int>> = _todayMacros.asStateFlow()

    val _refreshTrigger = MutableStateFlow(0)
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

    private fun updateTodayCalories() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val today = getTodayTimestamp()
                val todayNutrition = _nutritionDataList.value.filter { it.date == today }
                val totalCalories = todayNutrition.sumOf { it.calories }
                val totalProtein = todayNutrition.sumOf { it.protein.toInt() }
                val totalFat = todayNutrition.sumOf { it.fat.toInt() }
                val totalCarbs = todayNutrition.sumOf { it.carbs.toInt() }

                _todayCalories.value = totalCalories
                _todayMacros.value = Triple(totalProtein, totalFat, totalCarbs)

                Log.d("HealthViewModel", "Today macros: P:$totalProtein F:$totalFat C:$totalCarbs")
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Error updating today calories: ${e.message}")
            }
        }
    }

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Сначала ДОЖДЕМСЯ добавления продуктов
                initializeFoodsSync()

                // ПОТОМ получаем пользователя
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

    private suspend fun initializeFoodsSync() {
        try {
            val existingFoods = repository.getAllFoods().first()
            Log.d("HealthViewModel", "Existing foods count: ${existingFoods.size}")

            if (existingFoods.size < 21) {
                Log.d("HealthViewModel", "Adding initial foods... (current: ${existingFoods.size})")

                val initialFoods = listOf(
                    Food(name = "Курица (грудка)", calories = 165, protein = 31f, carbs = 0f, fat = 3.6f, fiber = 0f, category = "meat"),
                    Food(name = "Говядина (постная)", calories = 250, protein = 26f, carbs = 0f, fat = 17f, fiber = 0f, category = "meat"),
                    Food(name = "Рыба (лосось)", calories = 208, protein = 20f, carbs = 0f, fat = 13f, fiber = 0f, category = "meat"),
                    Food(name = "Яйцо куриное", calories = 155, protein = 13f, carbs = 1.1f, fat = 11f, fiber = 0f, category = "meat"),
                    Food(name = "Молоко (2.5%)", calories = 54, protein = 3.3f, carbs = 4.8f, fat = 2.5f, fiber = 0f, category = "dairy"),
                    Food(name = "Йогурт (натуральный)", calories = 59, protein = 3.5f, carbs = 3.3f, fat = 0.4f, fiber = 0f, category = "dairy"),
                    Food(name = "Сыр (твёрдый)", calories = 402, protein = 25f, carbs = 1.3f, fat = 33f, fiber = 0f, category = "dairy"),
                    Food(name = "Творог (5%)", calories = 121, protein = 17f, carbs = 3.3f, fat = 5f, fiber = 0f, category = "dairy"),
                    Food(name = "Брокколи", calories = 34, protein = 2.8f, carbs = 7f, fat = 0.4f, fiber = 2.4f, category = "vegetables"),
                    Food(name = "Морковь", calories = 41, protein = 0.9f, carbs = 10f, fat = 0.2f, fiber = 2.8f, category = "vegetables"),
                    Food(name = "Помидор", calories = 18, protein = 0.9f, carbs = 3.9f, fat = 0.2f, fiber = 1.2f, category = "vegetables"),
                    Food(name = "Огурец", calories = 16, protein = 0.7f, carbs = 3.6f, fat = 0.1f, fiber = 0.5f, category = "vegetables"),
                    Food(name = "Салат (зелёный)", calories = 15, protein = 1.5f, carbs = 2.9f, fat = 0.2f, fiber = 1.3f, category = "vegetables"),
                    Food(name = "Картофель (варёный)", calories = 77, protein = 2f, carbs = 17f, fat = 0.1f, fiber = 2.1f, category = "vegetables"),
                    Food(name = "Банан", calories = 89, protein = 1.1f, carbs = 23f, fat = 0.3f, fiber = 2.6f, category = "fruits"),
                    Food(name = "Яблоко", calories = 52, protein = 0.3f, carbs = 14f, fat = 0.2f, fiber = 2.4f, category = "fruits"),
                    Food(name = "Апельсин", calories = 47, protein = 0.9f, carbs = 12f, fat = 0.1f, fiber = 2.4f, category = "fruits"),
                    Food(name = "Ягоды (смешанные)", calories = 52, protein = 1f, carbs = 12f, fat = 0.3f, fiber = 1.7f, category = "fruits"),
                    Food(name = "Рис (варёный)", calories = 130, protein = 2.7f, carbs = 28f, fat = 0.3f, fiber = 0.4f, category = "grains"),
                    Food(name = "Гречка (варёная)", calories = 123, protein = 4.3f, carbs = 25f, fat = 1.1f, fiber = 2.7f, category = "grains"),
                    Food(name = "Овсяная каша", calories = 68, protein = 2.4f, carbs = 12f, fat = 1.4f, fiber = 1.6f, category = "grains"),
                    Food(name = "Хлеб (пшеничный)", calories = 265, protein = 8.4f, carbs = 49f, fat = 3.3f, fiber = 2.7f, category = "grains"),
                    Food(name = "Макароны (варёные)", calories = 131, protein = 4.4f, carbs = 25f, fat = 1.1f, fiber = 1.8f, category = "grains")
                )

                Log.d("HealthViewModel", "Total foods to insert: ${initialFoods.size}")

                try {
                    repository.insertFoods(initialFoods)
                    Log.d("HealthViewModel", "All foods inserted successfully!")
                } catch (e: Exception) {
                    Log.e("HealthViewModel", "Error inserting foods batch: ${e.message}")
                }

                delay(500)

                val afterInsert = repository.getAllFoods().first()
                Log.d("HealthViewModel", "Foods in DB after insert: ${afterInsert.size}")
                afterInsert.forEach { Log.d("HealthViewModel", "  - ${it.name}") }
            } else {
                Log.d("HealthViewModel", "Foods already exist in DB: ${existingFoods.size}")
                existingFoods.forEach { Log.d("HealthViewModel", "  - ${it.name}") }
            }
        } catch (e: Exception) {
            Log.e("HealthViewModel", "Error initializing foods: ${e.message}", e)
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
                // ← НЕ ДОБАВЛЯЕМ никакие данные со значениями 0
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Error creating default user: ${e.message}", e)
            }
        }
    }

    private fun loadHealthData(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.getHealthDataByUser(userId).collect { data ->
                    _healthDataList.value = data.sortedBy { it.date }
                    _refreshTrigger.value += 1  // Триггер обновления
                }
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Error loading health data: ${e.message}")
            }
        }
    }



    private fun loadNutritionData(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.getNutritionDataByUser(userId).collect { data ->
                    _nutritionDataList.value = data
                    updateTodayCalories()
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

    fun addHealthData(
        weight: Float,
        heartRate: Int,
        sys: Int,
        dia: Int,
        steps: Int,
        sleep: Float,
        water: Float,
        dateTimestamp: Long = getTodayTimestamp()  // ← ДОБАВЬ параметр даты
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("HealthViewModel", "Adding health data with weight: $weight for date: $dateTimestamp")

                val healthData = HealthData(
                    userId = _currentUser.value?.id ?: "user_1",
                    date = dateTimestamp,  // ← ИСПОЛЬЗУЙ переданную дату
                    weight = weight,
                    heartRate = heartRate,
                    bloodPressureSystolic = sys,
                    bloodPressureDiastolic = dia,
                    steps = steps,
                    sleepHours = sleep,
                    waterIntakeL = water
                )
                repository.insertHealthData(healthData)

                val userId = _currentUser.value?.id ?: "user_1"
                val currentUser = _currentUser.value
                if (currentUser != null && dateTimestamp == getTodayTimestamp()) {
                    // Обновляем профиль только если это запись за сегодня
                    val updatedUser = currentUser.copy(targetWeight = weight)
                    repository.updateUser(updatedUser)
                    _currentUser.value = updatedUser
                    Log.d("HealthViewModel", "User weight updated to: $weight")
                }

                loadHealthData(userId)
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Error adding health data: ${e.message}")
            }
        }
    }


    fun saveTodayStepsToDatabase(steps: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val healthData = HealthData(
                    userId = _currentUser.value?.id ?: "user_1",
                    date = getTodayTimestamp(),
                    weight = 0f,
                    heartRate = 0,
                    bloodPressureSystolic = 0,
                    bloodPressureDiastolic = 0,
                    steps = steps,
                    sleepHours = 0f,
                    waterIntakeL = 0f
                )

                // Проверяем есть ли уже запись за сегодня
                val existingToday = _healthDataList.value.find { it.date == getTodayTimestamp() }

                if (existingToday != null) {
                    // Обновляем только шаги, сохраняем остальное
                    repository.updateHealthData(
                        existingToday.copy(
                            steps = steps,
                            weight = if (existingToday.weight > 0) existingToday.weight else 0f
                        )
                    )
                } else {
                    // Не добавляем новую запись со значениями 0
                    Log.d("HealthViewModel", "No health data for today, skipping save")
                }

                val userId = _currentUser.value?.id ?: "user_1"
                loadHealthData(userId)
                Log.d("HealthViewModel", "Saved $steps steps to database")
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Error saving steps: ${e.message}")
            }
        }
    }

    fun calculateWaterIntake(): Float {
        val user = _currentUser.value ?: return 0f
        val baseIntake = user.targetWeight * 30 // 30 мл на кг

        val activityMultiplier = when (user.activityLevel) {
            "sedentary" -> 1.2f
            "light" -> 1.375f
            "moderate" -> 1.55f
            "active" -> 1.725f
            "very_active" -> 1.9f
            else -> 1.2f
        }

        return baseIntake * activityMultiplier / 1000 // Конвертируем в литры
    }


    fun addNutritionData(food: Food, portionGrams: Float, mealType: String, dateTimestamp: Long = getTodayTimestamp()) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val caloriesForPortion = (food.calories * portionGrams) / 100

                val nutritionData = NutritionData(
                    userId = _currentUser.value?.id ?: "user_1",
                    date = dateTimestamp,
                    mealType = mealType,
                    foodName = food.name,
                    calories = caloriesForPortion.toInt(),
                    protein = (food.protein * portionGrams) / 100,
                    carbs = (food.carbs * portionGrams) / 100,
                    fat = (food.fat * portionGrams) / 100,
                    fiber = (food.fiber * portionGrams) / 100,
                    portionGrams = portionGrams
                )
                repository.insertNutritionData(nutritionData)

                val userId = _currentUser.value?.id ?: "user_1"
                loadNutritionData(userId)
                updateTodayCalories()
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
        val lastHealth = _healthDataList.value.filter { it.weight > 0 }.lastOrNull() ?: return 0f
        return HealthCalculations.calculateBMI(lastHealth.weight, user.heightCm)
    }

    fun calculateDailyCalories(): Int {
        val user = _currentUser.value ?: return 0
        val lastHealth = _healthDataList.value.filter { it.weight > 0 }.lastOrNull()
        val weight = lastHealth?.weight ?: user.targetWeight

        Log.d("HealthViewModel", "BMR calculation - weight: $weight, age: ${user.age}, height: ${user.heightCm}")

        val bmr = HealthCalculations.calculateBMR(
            user.age,
            weight,
            user.heightCm,
            user.gender == "male"
        )

        Log.d("HealthViewModel", "BMR: $bmr")

        val multiplier = when(user.activityLevel) {
            "sedentary" -> 1.2f
            "light" -> 1.375f
            "moderate" -> 1.55f
            "active" -> 1.725f
            "very_active" -> 1.9f
            else -> 1.5f
        }

        val dailyCalories = bmr * multiplier

        Log.d("HealthViewModel", "Daily calories: $dailyCalories (multiplier: $multiplier)")

        return when(user.weightGoal) {
            "lose" -> (dailyCalories * 0.85f).toInt()
            "maintain" -> dailyCalories.toInt()
            "gain" -> (dailyCalories * 1.15f).toInt()
            else -> dailyCalories.toInt()
        }
    }

    fun calculateDailyMacros(): Triple<Int, Int, Int> {
        val dailyCalories = calculateDailyCalories()

        val proteinCalories = dailyCalories * 0.30f
        val fatCalories = dailyCalories * 0.20f
        val carbsCalories = dailyCalories * 0.50f

        val protein = (proteinCalories / 4).toInt()
        val fat = (fatCalories / 9).toInt()
        val carbs = (carbsCalories / 4).toInt()

        return Triple(protein, fat, carbs)
    }


    fun updateHealthData(healthData: HealthData) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateHealthData(healthData)
                val userId = _currentUser.value?.id ?: "user_1"
                loadHealthData(userId)
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Error updating health data: ${e.message}")
            }
        }
    }
    fun updateUser(name: String, gender: String, age: Int, heightCm: Float, targetWeight: Float, activityLevel: String, weightGoal: String, dailyStepGoal: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updatedUser = _currentUser.value?.copy(
                    name = name,
                    gender = gender,
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


    fun deleteHealthData(healthData: HealthData) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updatedList = _healthDataList.value.filter { it.id != healthData.id }
                _healthDataList.value = updatedList

                repository.deleteHealthData(healthData)

                delay(300)
                val userId = _currentUser.value?.id ?: "user_1"
                loadHealthData(userId)

                // ← ДОБАВЬ ЭТО: если удалили запись с весом, обновляем профиль
                if (healthData.weight > 0) {
                    val allWeightData = _healthDataList.value.filter { it.weight > 0 }.sortedBy { it.date }

                    if (allWeightData.isNotEmpty()) {
                        // Есть другие записи - берём последнюю
                        val lastWeight = allWeightData.last().weight
                        val currentUser = _currentUser.value
                        if (currentUser != null) {
                            val updatedUser = currentUser.copy(targetWeight = lastWeight)
                            repository.updateUser(updatedUser)
                            _currentUser.value = updatedUser
                            Log.d("HealthViewModel", "Updated targetWeight to: $lastWeight after deletion")
                        }
                    } else {
                        // Нет других записей - оставляем старый targetWeight
                        Log.d("HealthViewModel", "No more weight records, keeping current targetWeight")
                    }
                }
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Error deleting health data: ${e.message}")
            }
        }
    }


    fun deleteNutritionData(nutrition: NutritionData) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updatedList = _nutritionDataList.value.filter { it.id != nutrition.id }
                _nutritionDataList.value = updatedList

                updateTodayCalories()

                repository.deleteNutritionData(nutrition)

                delay(300)
                val userId = _currentUser.value?.id ?: "user_1"
                loadNutritionData(userId)
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Error deleting nutrition data: ${e.message}")
            }
        }
    }
}
