package com.example.healthmonitor.database

import androidx.room.*
import com.example.healthmonitor.models.NutritionData
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNutritionData(data: NutritionData)

    @Query("SELECT * FROM nutrition_data WHERE userId = :userId ORDER BY date DESC")
    fun getNutritionDataByUser(userId: String): Flow<List<NutritionData>>

    @Query("SELECT * FROM nutrition_data WHERE userId = :userId AND date = :date")
    fun getNutritionDataByDate(userId: String, date: Long): Flow<List<NutritionData>>

    @Query("SELECT SUM(calories) FROM nutrition_data WHERE userId = :userId AND date = :date")
    fun getTotalCaloriesByDate(userId: String, date: Long): Flow<Int>

    @Update
    fun updateNutritionData(data: NutritionData)

    @Delete
    fun deleteNutritionData(data: NutritionData)
}