package com.example.healthmonitor.database

import androidx.room.*
import com.example.healthmonitor.models.Food
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFood(food: Food)

    @Query("SELECT * FROM foods ORDER BY name ASC")
    fun getAllFoods(): Flow<List<Food>>

    @Query("SELECT * FROM foods WHERE category = :category ORDER BY name ASC")
    fun getFoodsByCategory(category: String): Flow<List<Food>>

    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchFoods(query: String): Flow<List<Food>>

    @Update
    fun updateFood(food: Food)

    @Delete
    fun deleteFood(food: Food)
}
