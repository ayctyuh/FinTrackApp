package com.fintrack.project.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.project.R
import com.fintrack.project.data.database.FinTrackDatabase
import com.fintrack.project.data.repository.AuthResult
import com.fintrack.project.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val Blue = Color(0xFF1D4ED8)
val LightBlue = Color(0xFF3B6FEA)
val BgGray = Color(0xFFF0F4FF)
val TextGray = Color(0xFF8A94A6)
val BorderColor = Color(0xFFE2E8F0)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onSignupClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember {
        UserRepository(FinTrackDatabase.getInstance(context).userDao())
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    // ── Thêm state cho lỗi ────────────────────────────────
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Phần header xanh ──────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF1A3FBF), Blue, LightBlue)
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-40).dp)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-30).dp, y = 20.dp)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 70.dp)
            ) {
                Text(text = "Chào mừng!", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Đăng nhập để tiếp tục", fontSize = 14.sp, color = Color.White.copy(alpha = 0.85f))
            }
        }

        // ── Card trắng phía dưới ──────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = 180.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "Đăng Nhập", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1F36))
                        Text(text = "Nhập thông tin tài khoản của bạn", fontSize = 12.sp, color = TextGray)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Banner lỗi (thêm mới, không đổi giao diện cũ) ──
                AnimatedVisibility(
                    visible = showError,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4E4))
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(errorMessage, fontSize = 13.sp, color = Color(0xFF991B1B), fontWeight = FontWeight.Medium)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Username hoặc Email
                Text(
                    text = "USERNAME HOẶC EMAIL",
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; showError = false },
                    placeholder = { Text("example@email.com", color = TextGray) },
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = TextGray, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = BorderColor,
                        focusedBorderColor = Blue,
                        unfocusedContainerColor = BgGray,
                        focusedContainerColor = BgGray
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mật khẩu
                Text(
                    text = "MẬT KHẨU",
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; showError = false },
                    placeholder = { Text("••••••••", color = TextGray) },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextGray, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                null, tint = TextGray, modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = BorderColor,
                        focusedBorderColor = Blue,
                        unfocusedContainerColor = BgGray,
                        focusedContainerColor = BgGray
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Ghi nhớ + Quên mật khẩu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(checkedColor = Blue),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ghi nhớ đăng nhập", fontSize = 13.sp, color = Color(0xFF1A1F36))
                    }
                    TextButton(onClick = onForgotPasswordClick) {
                        Text("Quên mật khẩu?", fontSize = 13.sp, color = Blue, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Nút Đăng Nhập — thêm logic DB ────────────
                Button(
                    onClick = {
                        if (!isLoading) {
                            isLoading = true
                            showError = false
                            CoroutineScope(Dispatchers.IO).launch {
                                val result = repository.login(
                                    emailOrUsername = email,
                                    password = password
                                )
                                withContext(Dispatchers.Main) {
                                    isLoading = false
                                    when (result) {
                                        // --- ĐOẠN ĐƯỢC THAY ĐỔI LÀ Ở ĐÂY ---
                                        is AuthResult.Success -> {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                // Tìm user trong DB bằng email hoặc username mà họ vừa nhập
                                                val userDao = FinTrackDatabase.getInstance(context).userDao()
                                                val user = userDao.getUserByEmail(email) ?: userDao.getUserByUsername(email)

                                                user?.let {
                                                    // Lưu ID của user vào SharedPreferences
                                                    val prefs = context.getSharedPreferences("FinTrackPrefs", android.content.Context.MODE_PRIVATE)
                                                    prefs.edit().putInt("LOGGED_IN_USER_ID", it.id).apply()
                                                }

                                                // Chuyển luồng về Main để thực hiện việc chuyển màn hình
                                                withContext(Dispatchers.Main) {
                                                    onLoginSuccess()
                                                }
                                            }
                                        }
                                        // -----------------------------------
                                        is AuthResult.Error -> {
                                            errorMessage = result.message
                                            showError = true
                                        }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Đăng Nhập", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor)
                    Text("  hoặc đăng nhập với  ", fontSize = 12.sp, color = TextGray)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Icon(painterResource(id = R.drawable.ic_google), "Google", tint = Color.Unspecified, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Google", fontSize = 14.sp, color = Color(0xFF1A1F36), fontWeight = FontWeight.Medium)
                    }
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Icon(painterResource(id = R.drawable.ic_facebook), "Facebook", tint = Color.Unspecified, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Facebook", fontSize = 14.sp, color = Color(0xFF1A1F36), fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text("Chưa có tài khoản? ", fontSize = 14.sp, color = TextGray)
                    TextButton(onClick = onSignupClick, contentPadding = PaddingValues(0.dp)) {
                        Text("Đăng ký ngay", fontSize = 14.sp, color = Blue, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)
                    }
                }
            }
        }
    }
}