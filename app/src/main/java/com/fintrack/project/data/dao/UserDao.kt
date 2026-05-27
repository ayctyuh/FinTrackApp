package com.fintrack.project.data.dao

import androidx.room.*
import com.fintrack.project.data.model.User

@Dao
/**
 * DAO cho bang nguoi dung.
 * Phu thuoc: Room va entity `User`.
 * Duoc su dung boi `UserRepository`.
 */
interface UserDao {
    /**
     * Them nguoi dung.
     * @param user Doi tuong nguoi dung.
     */
    @Insert
    suspend fun insertUser(user: User): Long

    /**
     * Cap nhat thong tin nguoi dung theo ID.
     * @param id ID nguoi dung.
     * @param username Ten dang nhap.
     * @param email Email.
     * @param phoneNumber So dien thoai.
     * @param passwordHash Hash mat khau.
     * @param updatedAt Thoi gian cap nhat.
     */
    @Query("UPDATE users SET username = :username, email = :email, phoneNumber = :phoneNumber, passwordHash = :passwordHash, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateUser(id: Int, username: String, email: String, phoneNumber: String?, passwordHash: String, updatedAt: Long): Int

    /**
     * Cap nhat mat khau.
     * @param id ID nguoi dung.
     * @param newPasswordHash Hash moi.
     * @param updatedAt Thoi gian cap nhat.
     */
    @Query("UPDATE users SET passwordHash = :newPasswordHash, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePassword(id: Int, newPasswordHash: String, updatedAt: Long): Int

    /**
     * Xoa nguoi dung.
     * @param id ID nguoi dung.
     */
    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: Int): Int

    /**
     * Lay nguoi dung theo ID.
     * @param id ID nguoi dung.
     */
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?

    /**
     * Lay nguoi dung theo username.
     * @param username Ten dang nhap.
     */
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    /**
     * Lay nguoi dung theo email.
     * @param email Email.
     */
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    /**
     * Lay nguoi dung theo so dien thoai.
     * @param phone So dien thoai.
     */
    @Query("SELECT * FROM users WHERE phoneNumber = :phone")
    suspend fun getUserByPhone(phone: String): User?

    /**
     * Xac thuc theo username va hash.
     * @param username Ten dang nhap.
     * @param passwordHash Hash mat khau.
     */
    @Query("SELECT * FROM users WHERE username = :username AND passwordHash = :passwordHash")
    suspend fun authenticateUser(username: String, passwordHash: String): User?

    /**
     * Xac thuc theo email va hash.
     * @param email Email.
     * @param passwordHash Hash mat khau.
     */
    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash")
    suspend fun authenticateByEmail(email: String, passwordHash: String): User?

    /**
     * Lay toan bo nguoi dung.
     */
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    /**
     * Cap nhat nguoi dung bang entity.
     * @param user Doi tuong nguoi dung.
     */
    @Update
    suspend fun updateUser(user: User)
}