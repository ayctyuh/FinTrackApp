package com.fintrack.project.utils

import java.security.MessageDigest

object SecurityUtils {
    /**
     * Hash mật khẩu bằng SHA-256
     */
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Kiểm tra xem mật khẩu có khớp với hash không
     */
    fun verifyPassword(password: String, hash: String): Boolean {
        return hashPassword(password) == hash
    }

    /**
     * Kiểm tra độ mạnh của mật khẩu
     */
    fun isStrongPassword(password: String): Boolean {
        if (password.length < 8) return false
        if (!password.any { it.isUpperCase() }) return false
        if (!password.any { it.isLowerCase() }) return false
        if (!password.any { it.isDigit() }) return false
        return true
    }

    /**
     * Kiểm tra email hợp lệ
     */
    fun isValidEmail(email: String): Boolean {
        return Regex("^[A-Za-z0-9+_.-]+@(.+)$").matches(email)
    }

    /**
     * Kiểm tra username hợp lệ
     */
    fun isValidUsername(username: String): Boolean {
        if (username.length < 3 || username.length > 20) return false
        return Regex("^[a-zA-Z0-9_.-]+$").matches(username)
    }
}

