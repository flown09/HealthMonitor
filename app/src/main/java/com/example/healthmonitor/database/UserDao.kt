package com.example.healthmonitor.database

import androidx.room.*
import com.example.healthmonitor.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUser(userId: String): Flow<User?>

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Update
    fun updateUser(user: User)

    @Delete
    fun deleteUser(user: User)
}
