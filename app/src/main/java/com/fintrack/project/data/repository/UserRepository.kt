package com.fintrack.project.data.repository

import android.util.Patterns
import com.fintrack.project.data.dao.UserDao
import com.fintrack.project.data.model.User

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class UserRepository(private val userDao: UserDao) {

    suspend fun insertUser(user: User): Long = userDao.insertUser(user)

    suspend fun updateUser(user: User) = userDao.updateUser(
        id = user.id,
        username = user.username,
        email = user.email,
        phoneNumber = user.phoneNumber,
        passwordHash = user.passwordHash,
        updatedAt = System.currentTimeMillis()
    )

    suspend fun deleteUser(id: Int) = userDao.deleteUser(id)

    suspend fun getUserById(id: Int): User? = userDao.getUserById(id)
    suspend fun getUserByUsername(username: String): User? = userDao.getUserByUsername(username)
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    suspend fun authenticateUser(username: String, passwordHash: String): User? = userDao.authenticateUser(username, passwordHash)
    suspend fun getAllUsers(): List<User> = userDao.getAllUsers()

    suspend fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String
    ): AuthResult {
        if (fullName.isBlank()) return AuthResult.Error("Vui lòng nhập họ và tên")
        if (email.isBlank()) return AuthResult.Error("Vui lòng nhập email")
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return AuthResult.Error("Email không đúng định dạng")
        if (phone.isBlank()) return AuthResult.Error("Vui lòng nhập số điện thoại")
        if (phone.replace(" ", "").replace("+", "").length < 9) return AuthResult.Error("Số điện thoại không hợp lệ")
        if (password.length < 8) return AuthResult.Error("Mật khẩu phải có ít nhất 8 ký tự")
        if (password != confirmPassword) return AuthResult.Error("Mật khẩu xác nhận không khớp")
        if (userDao.getUserByEmail(email) != null) return AuthResult.Error("Email này đã được đăng ký")
        if (userDao.getUserByUsername(fullName) != null) return AuthResult.Error("Tên này đã được sử dụng")

        val user = User(
            username = fullName,
            email = email,
            phoneNumber = phone,
            passwordHash = password.hashCode().toString()
        )
        userDao.insertUser(user)
        return AuthResult.Success(user)
    }

    suspend fun login(emailOrUsername: String, password: String): AuthResult {
        if (emailOrUsername.isBlank()) return AuthResult.Error("Vui lòng nhập email hoặc username")
        if (password.isBlank()) return AuthResult.Error("Vui lòng nhập mật khẩu")

        val passwordHash = password.hashCode().toString()

        val byUsername = userDao.authenticateUser(emailOrUsername, passwordHash)
        if (byUsername != null) return AuthResult.Success(byUsername)

        val byEmail = userDao.authenticateByEmail(emailOrUsername, passwordHash)
        if (byEmail != null) return AuthResult.Success(byEmail)

        val userExists = userDao.getUserByEmail(emailOrUsername)
            ?: userDao.getUserByUsername(emailOrUsername)

        return if (userExists != null)
            AuthResult.Error("Mật khẩu không đúng")
        else
            AuthResult.Error("Tài khoản không tồn tại")
    }
}