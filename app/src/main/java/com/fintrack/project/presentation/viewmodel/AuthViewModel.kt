package com.fintrack.project.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.fintrack.project.data.model.User
import com.fintrack.project.data.repository.UserRepository
import com.fintrack.project.utils.SecurityUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userRepository: UserRepository
) : BaseViewModel() {
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess = _loginSuccess.asStateFlow()

    private val _signupSuccess = MutableStateFlow(false)
    val signupSuccess = _signupSuccess.asStateFlow()

    /**
     * Đăng nhập người dùng
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                showLoading()
                clearError()

                if (username.isBlank() || password.isBlank()) {
                    setError("Vui lòng nhập tên đăng nhập và mật khẩu")
                    hideLoading()
                    return@launch
                }

                val passwordHash = SecurityUtils.hashPassword(password)
                val user = userRepository.authenticateUser(username, passwordHash)

                if (user != null) {
                    _currentUser.value = user
                    _isLoggedIn.value = true
                    _loginSuccess.value = true
                } else {
                    setError("Tên đăng nhập hoặc mật khẩu không chính xác")
                }
            } catch (e: Exception) {
                setError(e.message ?: "Lỗi không xác định")
            } finally {
                hideLoading()
            }
        }
    }

    /**
     * Đăng ký người dùng mới
     */
    fun signup(username: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            try {
                showLoading()
                clearError()

                // Validate inputs
                if (username.isBlank() || email.isBlank() || password.isBlank()) {
                    setError("Vui lòng điền đầy đủ thông tin")
                    hideLoading()
                    return@launch
                }

                if (!SecurityUtils.isValidUsername(username)) {
                    setError("Tên đăng nhập phải có 3-20 ký tự, chỉ chứa chữ, số, dấu gạch ngang")
                    hideLoading()
                    return@launch
                }

                if (!SecurityUtils.isValidEmail(email)) {
                    setError("Email không hợp lệ")
                    hideLoading()
                    return@launch
                }

                if (password != confirmPassword) {
                    setError("Mật khẩu không khớp")
                    hideLoading()
                    return@launch
                }

                if (!SecurityUtils.isStrongPassword(password)) {
                    setError("Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số")
                    hideLoading()
                    return@launch
                }

                // Check if user already exists
                if (userRepository.getUserByUsername(username) != null) {
                    setError("Tên đăng nhập đã tồn tại")
                    hideLoading()
                    return@launch
                }

                if (userRepository.getUserByEmail(email) != null) {
                    setError("Email đã được đăng ký")
                    hideLoading()
                    return@launch
                }

                // Create new user
                val passwordHash = SecurityUtils.hashPassword(password)
                val newUser = User(
                    username = username,
                    email = email,
                    passwordHash = passwordHash
                )

                val userId = userRepository.insertUser(newUser)
                if (userId > 0) {
                    _signupSuccess.value = true
                    setError(null)
                } else {
                    setError("Đăng ký thất bại, vui lòng thử lại")
                }
            } catch (e: Exception) {
                setError(e.message ?: "Lỗi không xác định")
            } finally {
                hideLoading()
            }
        }
    }

    /**
     * Đăng xuất
     */
    fun logout() {
        _currentUser.value = null
        _isLoggedIn.value = false
    }

    /**
     * Reset login/signup success states
     */
    fun resetStates() {
        _loginSuccess.value = false
        _signupSuccess.value = false
    }
}

