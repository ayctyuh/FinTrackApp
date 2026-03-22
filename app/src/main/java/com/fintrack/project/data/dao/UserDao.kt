package com.fintrack.project.data.dao

import androidx.room.*
import com.fintrack.project.data.model.User

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User): Long

    @Query("UPDATE users SET username = :username, email = :email, phoneNumber = :phoneNumber, passwordHash = :passwordHash, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateUser(id: Int, username: String, email: String, phoneNumber: String?, passwordHash: String, updatedAt: Long): Int

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: Int): Int

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE phoneNumber = :phone")
    suspend fun getUserByPhone(phone: String): User?

    @Query("SELECT * FROM users WHERE username = :username AND passwordHash = :passwordHash")
    suspend fun authenticateUser(username: String, passwordHash: String): User?

    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash")
    suspend fun authenticateByEmail(email: String, passwordHash: String): User?

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>
}