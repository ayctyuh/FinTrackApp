package com.fintrack.project.data.repository

import android.util.Patterns
import com.fintrack.project.data.dao.UserDao
import com.fintrack.project.data.model.User

/**
 * Ket qua thao tac xac thuc.
 * Duoc su dung boi `UserRepository` va cac man hinh dang nhap/dang ky.
 */
sealed class AuthResult {
    /**
     * Thanh cong va tra ve user.
     * @param user Thong tin nguoi dung.
     */
    data class Success(val user: User) : AuthResult()

    /**
     * That bai va tra ve thong bao loi.
     * @param message Mo ta loi.
     */
    data class Error(val message: String) : AuthResult()
}

/**
 * Repository quan ly du lieu nguoi dung.
 * Phu thuoc: `UserDao` (Room).
 * Duoc su dung boi `AuthViewModel` va cac man hinh dang nhap/dang ky.
 */
class UserRepository(private val userDao: UserDao) {

    /**
     * Them nguoi dung moi.
     * @param user Doi tuong nguoi dung.
     */
    suspend fun insertUser(user: User): Long = userDao.insertUser(user)

    /**
     * Cap nhat thong tin nguoi dung.
     * @param user Doi tuong nguoi dung can cap nhat.
     */
    suspend fun updateUser(user: User) = userDao.updateUser(
        id = user.id,
        username = user.username,
        email = user.email,
        phoneNumber = user.phoneNumber,
        passwordHash = user.passwordHash,
        updatedAt = System.currentTimeMillis()
    )

    /**
     * Xoa nguoi dung theo ID.
     * @param id ID nguoi dung.
     */
    suspend fun deleteUser(id: Int) = userDao.deleteUser(id)

    /**
     * Lay nguoi dung theo ID.
     * @param id ID nguoi dung.
     */
    suspend fun getUserById(id: Int): User? = userDao.getUserById(id)

    /**
     * Lay nguoi dung theo username.
     * @param username Ten dang nhap.
     */
    suspend fun getUserByUsername(username: String): User? = userDao.getUserByUsername(username)

    /**
     * Lay nguoi dung theo email.
     * @param email Email.
     */
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)

    /**
     * Xac thuc nguoi dung theo username va hash.
     * @param username Ten dang nhap.
     * @param passwordHash Hash mat khau.
     */
    suspend fun authenticateUser(username: String, passwordHash: String): User? = userDao.authenticateUser(username, passwordHash)

    /**
     * Lay tat ca nguoi dung.
     */
    suspend fun getAllUsers(): List<User> = userDao.getAllUsers()

    /**
     * Dang ky nguoi dung tu form dang ky nhanh.
     * @param fullName Ho va ten.
     * @param email Email.
     * @param phone So dien thoai.
     * @param password Mat khau.
     * @param confirmPassword Xac nhan mat khau.
     */
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

    /**
     * Dang nhap bang email hoac username.
     * @param emailOrUsername Email hoac username.
     * @param password Mat khau.
     */
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