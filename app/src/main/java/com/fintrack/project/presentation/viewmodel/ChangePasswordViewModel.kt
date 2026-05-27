package com.fintrack.project.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.fintrack.project.data.dao.UserDao

/**
 * ViewModel doi mat khau.
 * Phu thuoc: `UserDao` (Room).
 * Duoc su dung boi `ChangePasswordScreen`.
 */
class ChangePasswordViewModel(private val userDao: UserDao) : ViewModel() {
    /**
     * Doi mat khau cho nguoi dung.
     * @param userId ID nguoi dung.
     * @param oldPass Mat khau cu.
     * @param newPass Mat khau moi.
     * @param onSuccess Callback khi thanh cong.
     * @param onError Callback khi loi.
     */
    fun changePassword(
        userId: Int,
        oldPass: String,
        newPass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = userDao.getUserById(userId)
            val hashedOldPass = oldPass.hashCode().toString()
            val hashedNewPass = newPass.hashCode().toString()
            if (user != null && user.passwordHash == hashedOldPass) {
                userDao.updatePassword(userId, hashedNewPass, System.currentTimeMillis())
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } else {
                withContext(Dispatchers.Main) {
                    onError("Mật khẩu cũ không chính xác")
                }
            }
        }
    }
}