package com.example.healthmonitor.database

import androidx.room.*
import com.example.healthmonitor.models.HealthData
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertHealthData(data: HealthData)

    @Query("SELECT * FROM health_data WHERE userId = :userId ORDER BY date DESC")
    fun getHealthDataByUser(userId: String): Flow<List<HealthData>>

    @Query("SELECT * FROM health_data WHERE userId = :userId AND date = :date")
    fun getHealthDataByDate(userId: String, date: Long): Flow<HealthData?>

    @Update
    fun updateHealthData(data: HealthData)

    @Delete
    fun deleteHealthData(data: HealthData)
}
