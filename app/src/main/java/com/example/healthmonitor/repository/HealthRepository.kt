package com.example.healthmonitor.repository

import android.util.Log
import com.example.healthmonitor.database.HealthDatabase
import com.example.healthmonitor.models.User
import com.example.healthmonitor.models.HealthData
import com.example.healthmonitor.models.NutritionData
import com.example.healthmonitor.models.Food
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HealthRepository(private val database: HealthDatabase) {

    // User операции
    fun getCurrentUser(userId: String) = database.userDao().getUser(userId)

    fun getAllUsers() = database.userDao().getAllUsers()

    suspend fun insertUser(user: User) = withContext(Dispatchers.IO) {
        database.userDao().insertUser(user)
    }

    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        database.userDao().updateUser(user)
    }

    // Health Data операции
    fun getHealthDataByUser(userId: String) =
        database.healthDataDao().getHealthDataByUser(userId)

    fun getHealthDataByDate(userId: String, date: Long) =
        database.healthDataDao().getHealthDataByDate(userId, date)

    suspend fun insertHealthData(data: HealthData) = withContext(Dispatchers.IO) {
        database.healthDataDao().insertHealthData(data)
    }

    suspend fun deleteHealthData(healthData: HealthData) {
        database.healthDataDao().deleteHealthData(healthData)
    }


    suspend fun updateHealthData(data: HealthData) = withContext(Dispatchers.IO) {
        database.healthDataDao().updateHealthData(data)
    }

    // Nutrition Data операции
    fun getNutritionDataByUser(userId: String) =
        database.nutritionDataDao().getNutritionDataByUser(userId)

    fun getNutritionDataByDate(userId: String, date: Long) =
        database.nutritionDataDao().getNutritionDataByDate(userId, date)

    suspend fun insertNutritionData(data: NutritionData) = withContext(Dispatchers.IO) {
        database.nutritionDataDao().insertNutritionData(data)
    }

    // Food операции
    fun getAllFoods() = database.foodDao().getAllFoods()

    suspend fun insertFood(food: Food) = withContext(Dispatchers.IO) {
        database.foodDao().insertFood(food)
    }

    suspend fun insertFoods(foods: List<Food>) = withContext(Dispatchers.IO) {
        Log.d("HealthRepository", "Inserting ${foods.size} foods...")
        database.foodDao().insertFoods(foods)
        Log.d("HealthRepository", "All foods inserted")
    }

    suspend fun deleteNutritionData(nutrition: NutritionData) = withContext(Dispatchers.IO) {
        database.nutritionDataDao().deleteNutritionData(nutrition)
    }
}
