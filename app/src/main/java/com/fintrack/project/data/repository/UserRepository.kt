package com.fintrack.project.data.repository

import com.fintrack.project.data.dao.UserDao
import com.fintrack.project.data.model.User

class UserRepository(
    private val userDao: UserDao
) {
    suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }

    suspend fun getUserById(id: Int): User? {
        return userDao.getUserById(id)
    }

    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    suspend fun authenticateUser(username: String, passwordHash: String): User? {
        return userDao.authenticateUser(username, passwordHash)
    }

    suspend fun getAllUsers(): List<User> {
        return userDao.getAllUsers()
    }
}

