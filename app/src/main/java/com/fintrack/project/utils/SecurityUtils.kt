package com.fintrack.project.utils

import java.security.MessageDigest

/**
 * Tien ich bao mat (hash va validate co ban).
 * Phu thuoc: `MessageDigest`.
 * Duoc su dung boi `AuthViewModel` va logic xac thuc.
 */
object SecurityUtils {
    /**
     * Hash mat khau bang SHA-256.
     * @param password Mat khau thuan.
     * @return Chuoi hash dang hex.
     */
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Kiem tra mat khau co khop hash khong.
     * @param password Mat khau thuan.
     * @param hash Chuoi hash.
     * @return true neu khop.
     */
    fun verifyPassword(password: String, hash: String): Boolean {
        return hashPassword(password) == hash
    }

    /**
     * Kiem tra do manh mat khau.
     * @param password Mat khau can kiem tra.
     * @return true neu dat tieu chi co ban.
     */
    fun isStrongPassword(password: String): Boolean {
        if (password.length < 8) return false
        if (!password.any { it.isUpperCase() }) return false
        if (!password.any { it.isLowerCase() }) return false
        if (!password.any { it.isDigit() }) return false
        return true
    }

    /**
     * Kiem tra dinh dang email.
     * @param email Email can kiem tra.
     * @return true neu hop le.
     */
    fun isValidEmail(email: String): Boolean {
        return Regex("^[A-Za-z0-9+_.-]+@(.+)$").matches(email)
    }

    /**
     * Kiem tra username hop le.
     * @param username Ten dang nhap.
     * @return true neu hop le.
     */
    fun isValidUsername(username: String): Boolean {
        if (username.length < 3 || username.length > 20) return false
        return Regex("^[a-zA-Z0-9_.-]+$").matches(username)
    }
}

