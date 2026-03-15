package com.fintrack.project.data.dao

import androidx.room.*
import com.fintrack.project.data.model.User

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE username = :username AND passwordHash = :passwordHash")
    suspend fun authenticateUser(username: String, passwordHash: String): User?

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>
}

