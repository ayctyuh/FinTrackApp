package com.fintrack.project.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit,
    onConfirmClick: (String, String) -> Unit // Tham số truyền về MainActivity
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current // Lấy context để hiển thị thông báo lỗi tại chỗ

    Scaffold(
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            // --- HEADER GRADIENT ĐỒNG BỘ ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF1A3FBF), Color(0xFF3B82F6))))
                    .padding(top = 16.dp, bottom = 10.dp)
            ) {
                Box(modifier = Modifier.size(160.dp).align(Alignment.TopEnd).offset(x = 40.dp, y = (-40).dp).background(Color.White.copy(alpha = 0.08f), CircleShape))
                Box(modifier = Modifier.size(100.dp).align(Alignment.BottomStart).offset(x = (-30).dp, y = 20.dp).background(Color.White.copy(alpha = 0.08f), CircleShape))

                Column(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        // Nút Back bên trái
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.align(Alignment.CenterStart).size(36.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Quay lại", tint = Color.White)
                        }
                        // Tiêu đề chính giữa
                        Text(text = "Đổi mật khẩu", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Hãy sử dụng mật khẩu mạnh để bảo vệ tài khoản",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                    )
                }
            }

            // --- NỘI DUNG FORM NHẬP ---
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .offset(y = 5.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp), // Bo góc giống trang Cá nhân
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {

                        PasswordField(
                            value = oldPassword,
                            onValueChange = { oldPassword = it },
                            label = "Mật khẩu hiện tại",
                            isVisible = oldPasswordVisible,
                            onToggleVisibility = { oldPasswordVisible = !oldPasswordVisible }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        PasswordField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = "Mật khẩu mới",
                            isVisible = newPasswordVisible,
                            onToggleVisibility = { newPasswordVisible = !newPasswordVisible }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        PasswordField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = "Xác nhận mật khẩu mới",
                            isVisible = newPasswordVisible,
                            onToggleVisibility = { newPasswordVisible = !newPasswordVisible }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // --- NÚT CẬP NHẬT KÈM LOGIC KIỂM TRA ---
                        Button(
                            onClick = {
                                // 1. Kiểm tra xem người dùng đã nhập đủ chưa
                                if (oldPassword.isBlank() || newPassword.isBlank()) {
                                    Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                // 2. Kiểm tra mật khẩu mới và xác nhận có khớp không
                                if (newPassword != confirmPassword) {
                                    Toast.makeText(context, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                // 3. Gửi lên ViewModel kiểm tra
                                onConfirmClick(oldPassword, newPassword)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E5BFF))
                        ) {
                            Text("Cập nhật mật khẩu", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Hàm hỗ trợ vẽ ô nhập mật khẩu
@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8)
                )
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF2E5BFF),
            unfocusedBorderColor = Color(0xFFE2E8F0)
        )
    )
}