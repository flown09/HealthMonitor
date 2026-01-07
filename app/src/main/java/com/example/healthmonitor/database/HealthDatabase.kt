package com.example.healthmonitor.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.healthmonitor.models.User
import com.example.healthmonitor.models.HealthData
import com.example.healthmonitor.models.NutritionData
import com.example.healthmonitor.models.Food
import android.util.Log

@Database(
    entities = [User::class, HealthData::class, NutritionData::class, Food::class],
    version = 8,  // ← Увеличили версию
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
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            Log.d("HealthDatabase", "Database created, inserting initial foods...")
            insertInitialFoods(db)
        }

        private fun insertInitialFoods(db: SupportSQLiteDatabase) {
            val foods = listOf(
                "INSERT INTO foods (name, calories, protein, carbs, fat, fiber, category) VALUES ('Курица (грудка)', 165, 31.0, 0.0, 3.6, 0.0, 'meat')",
                "INSERT INTO foods (name, calories, protein, carbs, fat, fiber, category) VALUES ('Говядина (постная)', 250, 26.0, 0.0, 17.0, 0.0, 'meat')",
                "INSERT INTO foods (name, calories, protein, carbs, fat, fiber, category) VALUES ('Рыба (лосось)', 208, 20.0, 0.0, 13.0, 0.0, 'meat')",
                "INSERT INTO foods (name, calories, protein, carbs, fat, fiber, category) VALUES ('Яйцо куриное', 155, 13.0, 1.1, 11.0, 0.0, 'meat')",
                "INSERT INTO foods (name, calories, protein, carbs, fat, fiber, category) VALUES ('Молоко (2.5%)', 54, 3.3, 4.8, 2.5, 0.0, 'dairy')",
                "INSERT INTO foods (name, calories, protein, carbs, fat, fiber, category) VALUES ('Йогурт (натуральный)', 59, 3.5, 3.3, 0.4, 0.0, 'dairy')",
                "INSERT INTO foods (name, calories, protein, carbs, fat, fiber, category) VALUES ('Сыр (твёрдый)', 402, 25.0, 1.3, 33.0, 0.0, 'dairy')",
                "INSERT INTO foods (name, calories, protein, carbs, fat, fiber, category) VALUES ('Творог (5%)', 121, 17.0, 3.3, 5.0, 0.0, 'dairy')",
                "INSERT INTO foods (name, calories, protein, carbs, fat, fiber, category) VALUES ('Брокколи', 34, 2.8, 7.0, 0.4, 2.4, 'vegetables')",
                "INSERT INTO foods (name, calories, protein, carbs, fat, fiber, category) VALUES ('Морковь', 41, 0.9, 10.0, 0.2, 2.8, 'vegetables')",
                "INSERT INTO foods (name, calories, protein, carbs, fat, fiber, category) VALUES ('Помидор', 18, 0.9, 3.9, 0.2, 1.2, 'vegetables')",
                "INSERT INTO foods (name, calories, protein, carbs, fat, fiber, category) VALUES ('Огурец', 16, 0.7, 3.6, 0.1, 0.5, 'vegetables')",
                "INSERT INTO foods (name, calories, protein, carbs, fat, fiber, category) VALUES ('Салат (зелёный)', 15, 1.5, 2.9, 0.2, 1.3, 'vegetables')",
                "INSERT INTO foods (name, calories, protein, carbs, fat, fiber, category) VALUES ('Картофель (варёный)', 77, 2.0, 17.0, 0.1, 2.1, 'vegetables')",
                "INSERT INTO foods (name, calories, protein, carbs, fat, fiber, category) VALUES ('Банан', 89, 1.1, 23.0, 0.3, 2.6, 'fruits')",
                "INSERT INTO foods (name, calories, protein, carbs, fat, fiber, category) VALUES ('Яблоко', 52, 0.3, 14.0, 0.2, 2.4, 'fruits')",
                "INSERT INTO foods (name, calories, protein, carbs, fat, fiber, category) VALUES ('Апельсин', 47, 0.9, 12.0, 0.1, 2.4, 'fruits')",
                "INSERT INTO foods (name, calories, protein, carbs, fat, fiber, category) VALUES ('Ягоды (смешанные)', 52, 1.0, 12.0, 0.3, 1.7, 'fruits')",
                "INSERT INTO foods (name, calories, protein, carbs, fat, fiber, category) VALUES ('Рис (варёный)', 130, 2.7, 28.0, 0.3, 0.4, 'grains')",
                "INSERT INTO foods (name, calories, protein, carbs, fat, fiber, category) VALUES ('Гречка (варёная)', 123, 4.3, 25.0, 1.1, 2.7, 'grains')",
                "INSERT INTO foods (name, calories, protein, carbs, fat, fiber, category) VALUES ('Овсяная каша', 68, 2.4, 12.0, 1.4, 1.6, 'grains')",
                "INSERT INTO foods (name, calories, protein, carbs, fat, fiber, category) VALUES ('Хлеб (пшеничный)', 265, 8.4, 49.0, 3.3, 2.7, 'grains')",
                "INSERT INTO foods (name, calories, protein, carbs, fat, fiber, category) VALUES ('Макароны (варёные)', 131, 4.4, 25.0, 1.1, 1.8, 'grains')"
            )

            foods.forEach { sql ->
                try {
                    db.execSQL(sql)
                    Log.d("HealthDatabase", "Inserted: $sql")
                } catch (e: Exception) {
                    Log.e("HealthDatabase", "Error inserting: ${e.message}")
                }
            }
            Log.d("HealthDatabase", "Initial foods inserted: ${foods.size}")
        }
    }
}
