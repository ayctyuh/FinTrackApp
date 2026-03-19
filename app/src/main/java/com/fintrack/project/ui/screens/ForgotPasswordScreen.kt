package com.fintrack.project.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.project.R
import kotlinx.coroutines.delay

val FPBlue = Color(0xFF1D4ED8)
val FPGreen = Color(0xFF22C55E)
val FPBgGray = Color(0xFFF0F4FF)
val FPBorderColor = Color(0xFFE2E8F0)
val FPTextGray = Color(0xFF8A94A6)

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit = {},
    onSignupClick: () -> Unit = {}   // ✅ thêm parameter này
) {
    var step by remember { mutableStateOf(1) }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Header xanh (đồng nhất với Login/Signup) ──────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF1A3FBF), FPBlue, Color(0xFF3B6FEA))
                    )
                )
        ) {
            // Vòng tròn lớn - góc phải trên
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-40).dp)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
            )
            // Vòng tròn nhỏ - góc trái dưới
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-30).dp, y = 20.dp)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
            )

            // Back button + Title
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 70.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ✅ Chỉ hiện nút back khi step > 1
                if (step > 1) {
                    IconButton(
                        onClick = { step-- },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                } else {
                    // Step 1: padding trái để chữ không bị lệch
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Column {
                    Text(
                        "Quên mật khẩu?",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Khôi phục qua email của bạn",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // ── Card trắng ────────────────────────────────────────
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
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                // Step indicator
                StepIndicator(currentStep = step)

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    },
                    label = "FPStep"
                ) { currentStep ->
                    when (currentStep) {
                        1 -> EmailStep(
                            onNext = { step = 2 },
                            onSignupClick = onSignupClick  // ✅ truyền đúng callback
                        )
                        2 -> OTPStep(onNext = { step = 3 })
                        3 -> NewPasswordStep(onNext = { step = 4 })
                        4 -> SuccessStep(onBackToLogin = onBackClick)
                        else -> EmailStep(onNext = { step = 2 }, onSignupClick = onSignupClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int) {
    val steps = listOf("Email", "Mã OTP", "Mật khẩu mới")

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        steps.forEachIndexed { index, label ->
            val stepNum = index + 1
            val isActive = stepNum == currentStep
            val isDone = stepNum < currentStep

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            when {
                                isActive -> FPBlue
                                isDone -> FPGreen
                                else -> FPBgGray
                            },
                            CircleShape
                        )
                        .border(1.dp, if (!isActive && !isDone) FPBorderColor else Color.Transparent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("$stepNum", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                            color = if (isActive) Color.White else FPTextGray)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(label, fontSize = 10.sp,
                    color = if (isActive) FPBlue else FPTextGray,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
            }

            if (index < steps.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.width(48.dp).padding(horizontal = 4.dp).padding(bottom = 18.dp),
                    color = if (stepNum < currentStep) FPGreen else FPBorderColor,
                    thickness = 1.5.dp
                )
            }
        }
    }
}

@Composable
private fun EmailStep(onNext: () -> Unit, onSignupClick: () -> Unit) {
    var email by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Đặt lại mật khẩu?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1F36))
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Nhập địa chỉ email đã đăng ký. Chúng tôi sẽ gửi mã xác nhận để đặt lại mật khẩu.",
            fontSize = 13.sp, color = FPTextGray, lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF)),
            border = BorderStroke(1.dp, Color(0xFFBFD0FF))
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Info, null, tint = FPBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Kiểm tra hộp thư đến và thư rác. Mã có hiệu lực trong 5 phút.",
                    fontSize = 12.sp, color = FPBlue, lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("ĐỊA CHỈ EMAIL", fontSize = 11.sp, fontWeight = FontWeight.Bold,
            color = FPTextGray, letterSpacing = 0.8.sp,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("example@example.com", color = FPTextGray) },
            leadingIcon = { Icon(Icons.Default.Email, null, tint = FPTextGray, modifier = Modifier.size(20.dp)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = FPBorderColor,
                focusedBorderColor = FPBlue,
                unfocusedContainerColor = FPBgGray,
                focusedContainerColor = FPBgGray
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FPBlue)
        ) {
            Text("Tiếp theo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ✅ Nút Đăng ký ngay → về trang Đăng ký
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Chưa có tài khoản? ", fontSize = 14.sp, color = FPTextGray)
            TextButton(onClick = onSignupClick, contentPadding = PaddingValues(0.dp)) {
                Text(
                    "Đăng ký ngay",
                    fontSize = 14.sp,
                    color = FPBlue,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}

@Composable
private fun OTPStep(onNext: () -> Unit) {
    var otp by remember { mutableStateOf(List(6) { "" }) }
    var timeLeft by remember { mutableStateOf(300) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Nhập mã OTP", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1F36))
        Spacer(modifier = Modifier.height(6.dp))
        Text("Đã gửi mã 6 chữ số đến email của bạn. Mã có hiệu lực trong 5 phút.",
            fontSize = 13.sp, color = FPTextGray, lineHeight = 18.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(6) { index ->
                OutlinedTextField(
                    value = otp[index],
                    onValueChange = { value ->
                        if (value.length <= 1 && value.all { it.isDigit() }) {
                            otp = otp.toMutableList().also { it[index] = value }
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = if (otp[index].isNotEmpty()) FPBlue else FPBorderColor,
                        focusedBorderColor = FPBlue,
                        unfocusedContainerColor = if (otp[index].isNotEmpty()) Color(0xFFEEF2FF) else FPBgGray,
                        focusedContainerColor = Color(0xFFEEF2FF)
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Mã hết hạn sau:", fontSize = 13.sp, color = FPTextGray)
            Text("%02d:%02d".format(minutes, seconds), fontSize = 14.sp,
                fontWeight = FontWeight.Bold, color = if (timeLeft < 60) Color.Red else FPBlue)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { timeLeft = 300 }) {
            Text("Gửi lại mã", fontSize = 13.sp, color = FPBlue, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FPBlue),
            enabled = otp.all { it.isNotEmpty() }
        ) {
            Text("Xác nhận", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun NewPasswordStep(onNext: () -> Unit) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val passwordMatch = password.isNotEmpty() && password == confirmPassword
    val hasMinLength = password.length >= 8

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Mật khẩu mới", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1F36))
        Spacer(modifier = Modifier.height(6.dp))
        Text("Tạo một mật khẩu an toàn cho bạn.", fontSize = 13.sp, color = FPTextGray)

        Spacer(modifier = Modifier.height(24.dp))

        Text("MẬT KHẨU MỚI", fontSize = 11.sp, fontWeight = FontWeight.Bold,
            color = FPTextGray, letterSpacing = 0.8.sp,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))

        OutlinedTextField(
            value = password, onValueChange = { password = it },
            placeholder = { Text("••••••••", color = FPTextGray) },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = FPTextGray, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        null, tint = FPTextGray, modifier = Modifier.size(20.dp))
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = FPBorderColor, focusedBorderColor = FPBlue,
                unfocusedContainerColor = FPBgGray, focusedContainerColor = FPBgGray
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        if (password.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (hasMinLength) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    null, tint = if (hasMinLength) FPGreen else Color.Red, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ít nhất 8 ký tự", fontSize = 12.sp, color = if (hasMinLength) FPGreen else Color.Red)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("XÁC NHẬN MẬT KHẨU", fontSize = 11.sp, fontWeight = FontWeight.Bold,
            color = FPTextGray, letterSpacing = 0.8.sp,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))

        OutlinedTextField(
            value = confirmPassword, onValueChange = { confirmPassword = it },
            placeholder = { Text("••••••••", color = FPTextGray) },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = FPTextGray, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        null, tint = FPTextGray, modifier = Modifier.size(20.dp))
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = if (passwordMatch) FPGreen else FPBorderColor,
                focusedBorderColor = if (passwordMatch) FPGreen else FPBlue,
                unfocusedContainerColor = if (passwordMatch) Color(0xFFDCFCE7) else FPBgGray,
                focusedContainerColor = if (passwordMatch) Color(0xFFDCFCE7) else FPBgGray
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        if (passwordMatch) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = FPGreen, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Mật khẩu khớp", fontSize = 12.sp, color = FPGreen)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FPBlue),
            enabled = passwordMatch && hasMinLength
        ) {
            Text("Đổi mật khẩu", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun SuccessStep(onBackToLogin: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(3000)
        onBackToLogin()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color(0xFFDCFCE7), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, null, tint = FPGreen, modifier = Modifier.size(52.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Hoàn tất!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1F36))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Mật khẩu đã được cập nhật thành công.\nBạn có thể đăng nhập với mật khẩu mới ngay bây giờ.",
            fontSize = 14.sp, color = FPTextGray, textAlign = TextAlign.Center, lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FPBlue)
        ) {
            Text("Về trang đăng nhập", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Tự động chuyển sau 3 giây...", fontSize = 12.sp, color = FPTextGray)
    }
}