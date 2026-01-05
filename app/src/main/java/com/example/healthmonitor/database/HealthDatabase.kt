package com.example.healthmonitor.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.healthmonitor.models.User
import com.example.healthmonitor.models.HealthData
import com.example.healthmonitor.models.NutritionData
import com.example.healthmonitor.models.Food

@Database(
    entities = [User::class, HealthData::class, NutritionData::class, Food::class],
    version = 1,
    exportSchema = false
)
abstract class HealthDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun healthDataDao(): HealthDataDao
    abstract fun nutritionDataDao(): NutritionDataDao
    abstract fun foodDao(): FoodDao

    companion object {
        @Volatile
        private var INSTANCE: HealthDatabase? = null

        fun getDatabase(context: Context): HealthDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HealthDatabase::class.java,
                    "health_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
