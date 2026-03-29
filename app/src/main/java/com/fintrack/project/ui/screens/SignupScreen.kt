package com.fintrack.project.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fintrack.project.R
import com.fintrack.project.data.database.FinTrackDatabase
import com.fintrack.project.data.repository.AuthResult
import com.fintrack.project.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val SignupBlue = Color(0xFF1D4ED8)
val SignupGreen = Color(0xFF22C55E)
val SignupBgGray = Color(0xFFF0F4FF)
val SignupBorderColor = Color(0xFFE2E8F0)
val SignupTextGray = Color(0xFF8A94A6)

@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit = {},
    onLoginClick: () -> Unit = {} // <-- SỬA: onLoginClick thay vì onBackClick
) {
    val context = LocalContext.current
    val repository = remember { UserRepository(FinTrackDatabase.getInstance(context).userDao()) }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var agreeTerms by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessBanner by remember { mutableStateOf(false) }
    var showErrorBanner by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val passwordMatch = password.isNotEmpty() && confirmPassword.isNotEmpty() && password == confirmPassword

    if (isSuccess) {
        LaunchedEffect(Unit) { delay(2000); onSignupSuccess() }
    }

    if (showTermsDialog) {
        Dialog(onDismissRequest = { showTermsDialog = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Điều khoản dịch vụ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1F36))
                            IconButton(onClick = { showTermsDialog = false }) { Icon(Icons.Default.Close, null, tint = SignupTextGray) }
                        }
                        HorizontalDivider(color = SignupBorderColor)
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                            TermSection("1. Chấp nhận điều khoản", "Bằng cách sử dụng FinTrack, bạn đồng ý tuân thủ các điều khoản này.")
                            TermSection("2. Tài khoản người dùng", "Bạn chịu trách nhiệm bảo mật thông tin đăng nhập của mình.")
                            TermSection("3. Sử dụng hợp lệ", "Bạn cam kết không sử dụng ứng dụng cho các mục đích bất hợp pháp.")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { agreeTerms = true; showTermsDialog = false }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = SignupBlue)) {
                            Text("Đã hiểu", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showPrivacyDialog) {
        Dialog(onDismissRequest = { showPrivacyDialog = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Chính sách bảo mật", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1F36))
                            IconButton(onClick = { showPrivacyDialog = false }) { Icon(Icons.Default.Close, null, tint = SignupTextGray) }
                        }
                        HorizontalDivider(color = SignupBorderColor)
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                            TermSection("1. Thu thập thông tin", "Chúng tôi thu thập thông tin để cung cấp dịch vụ tốt hơn.")
                            TermSection("2. Bảo mật dữ liệu", "Chúng tôi không bán hoặc chia sẻ thông tin cá nhân của bạn với bên thứ ba.")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { agreeTerms = true; showPrivacyDialog = false }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = SignupBlue)) {
                            Text("Đã hiểu", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(Brush.linearGradient(colors = listOf(Color(0xFF1A3FBF), SignupBlue, Color(0xFF3B6FEA))))) {
            Box(modifier = Modifier.size(160.dp).align(Alignment.TopEnd).offset(x = 40.dp, y = (-40).dp).background(Color.White.copy(alpha = 0.08f), CircleShape))
            Box(modifier = Modifier.size(100.dp).align(Alignment.BottomStart).offset(x = (-30).dp, y = 20.dp).background(Color.White.copy(alpha = 0.08f), CircleShape))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(start = 24.dp, bottom = 70.dp)) {
                Text("Tạo tài khoản", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Bắt đầu quản lý tài chính", fontSize = 14.sp, color = Color.White.copy(alpha = 0.85f))
            }
        }

        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(top = 180.dp).clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(Color.White)) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Đăng Ký", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1F36))
                    Text("Điền đầy đủ thông tin để tạo tài khoản", fontSize = 12.sp, color = SignupTextGray)
                }

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(visible = showSuccessBanner, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    Column {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7))) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = SignupGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Đăng ký thành công!", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                                    Text("Tài khoản đã được tạo. Chuyển đến đăng nhập...", fontSize = 11.sp, color = Color(0xFF166534))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                AnimatedVisibility(visible = showErrorBanner, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    Column {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4E4))) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(errorMessage, fontSize = 13.sp, color = Color(0xFF991B1B), fontWeight = FontWeight.Medium)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                SLabel("HỌ VÀ TÊN")
                SField(value = fullName, onValueChange = { fullName = it; showErrorBanner = false }, placeholder = "Nguyễn Văn A", leadingIcon = { Icon(Icons.Default.Person, null, tint = if (isSuccess) SignupGreen else SignupTextGray, modifier = Modifier.size(20.dp)) }, trailingIcon = if (isSuccess) {{ Icon(Icons.Default.Check, null, tint = SignupGreen, modifier = Modifier.size(20.dp)) }} else null, isSuccess = isSuccess)

                Spacer(modifier = Modifier.height(8.dp))

                SLabel("EMAIL")
                SField(value = email, onValueChange = { email = it; showErrorBanner = false }, placeholder = "example@example.com", leadingIcon = { Icon(Icons.Default.Email, null, tint = if (isSuccess) SignupGreen else SignupTextGray, modifier = Modifier.size(20.dp)) }, trailingIcon = if (isSuccess) {{ Icon(Icons.Default.Check, null, tint = SignupGreen, modifier = Modifier.size(20.dp)) }} else null, keyboardType = KeyboardType.Email, isSuccess = isSuccess)

                Spacer(modifier = Modifier.height(8.dp))

                SLabel("SỐ ĐIỆN THOẠI")
                SField(value = phone, onValueChange = { phone = it; showErrorBanner = false }, placeholder = "+ 123 456 789", leadingIcon = { Icon(Icons.Default.Phone, null, tint = if (isSuccess) SignupGreen else SignupTextGray, modifier = Modifier.size(20.dp)) }, trailingIcon = if (isSuccess) {{ Icon(Icons.Default.Check, null, tint = SignupGreen, modifier = Modifier.size(20.dp)) }} else null, keyboardType = KeyboardType.Phone, isSuccess = isSuccess)

                Spacer(modifier = Modifier.height(8.dp))

                SLabel("MẬT KHẨU")
                SField(value = password, onValueChange = { password = it; showErrorBanner = false }, placeholder = "••••••••", leadingIcon = { Icon(Icons.Default.Lock, null, tint = if (isSuccess) SignupGreen else SignupTextGray, modifier = Modifier.size(20.dp)) }, trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = SignupTextGray, modifier = Modifier.size(20.dp)) } }, keyboardType = KeyboardType.Password, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), isSuccess = isSuccess)

                Spacer(modifier = Modifier.height(8.dp))

                SLabel("XÁC NHẬN MẬT KHẨU")
                SField(value = confirmPassword, onValueChange = { confirmPassword = it; showErrorBanner = false }, placeholder = "••••••••", leadingIcon = { Icon(Icons.Default.Lock, null, tint = if (isSuccess) SignupGreen else SignupTextGray, modifier = Modifier.size(20.dp)) }, trailingIcon = { if (isSuccess) { Icon(Icons.Default.Check, null, tint = SignupGreen, modifier = Modifier.size(20.dp)) } else { IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) { Icon(if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = SignupTextGray, modifier = Modifier.size(20.dp)) } } }, keyboardType = KeyboardType.Password, visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(), isSuccess = isSuccess)

                if (passwordMatch && !isSuccess) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = SignupGreen, modifier = Modifier.size(14.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("Mật khẩu khớp", fontSize = 12.sp, color = SignupGreen)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Checkbox(checked = agreeTerms, onCheckedChange = { agreeTerms = it }, colors = CheckboxDefaults.colors(checkedColor = SignupBlue), modifier = Modifier.size(24.dp).padding(top = 2.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(buildAnnotatedString { withStyle(SpanStyle(color = SignupTextGray, fontSize = 12.sp)) { append("Bằng cách tiếp tục, bạn đồng ý với ") } }, lineHeight = 18.sp)
                        Row {
                            TextButton(onClick = { showTermsDialog = true }, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(20.dp)) { Text("Điều khoản dịch vụ", fontSize = 12.sp, color = SignupBlue, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline) }
                            Text(" và ", fontSize = 12.sp, color = SignupTextGray)
                            TextButton(onClick = { showPrivacyDialog = true }, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(20.dp)) { Text("Chính sách bảo mật", fontSize = 12.sp, color = SignupBlue, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (!isLoading && !isSuccess) {
                            isLoading = true; showErrorBanner = false
                            CoroutineScope(Dispatchers.IO).launch {
                                val result = repository.register(fullName = fullName, email = email, phone = phone, password = password, confirmPassword = confirmPassword)
                                withContext(Dispatchers.Main) {
                                    isLoading = false
                                    when (result) {
                                        is AuthResult.Success -> { isSuccess = true; showSuccessBanner = true; showErrorBanner = false }
                                        is AuthResult.Error -> { errorMessage = result.message; showErrorBanner = true }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isSuccess) SignupGreen else SignupBlue, disabledContainerColor = SignupBorderColor), enabled = agreeTerms && !isSuccess && !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp) else Text(if (isSuccess) "Đăng ký thành công ✓" else "Đăng Ký", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = SignupBorderColor)
                    Text("  hoặc đăng ký với  ", fontSize = 12.sp, color = SignupTextGray)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = SignupBorderColor)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = {}, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, SignupBorderColor), colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)) {
                        Icon(painterResource(id = R.drawable.ic_google), null, tint = Color.Unspecified, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Google", fontSize = 14.sp, color = Color(0xFF1A1F36), fontWeight = FontWeight.Medium)
                    }
                    OutlinedButton(onClick = {}, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, SignupBorderColor), colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)) {
                        Icon(painterResource(id = R.drawable.ic_facebook), null, tint = Color.Unspecified, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Facebook", fontSize = 14.sp, color = Color(0xFF1A1F36), fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text("Đã có tài khoản? ", fontSize = 14.sp, color = SignupTextGray)
                    TextButton(onClick = onLoginClick, contentPadding = PaddingValues(0.dp)) { // <-- Đã gọi đúng hàm onLoginClick
                        Text("Đăng nhập", fontSize = 14.sp, color = SignupBlue, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun TermSection(title: String, content: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1F36))
        Spacer(modifier = Modifier.height(4.dp))
        Text(content, fontSize = 12.sp, color = SignupTextGray, lineHeight = 18.sp)
    }
}

@Composable
fun SLabel(text: String) {
    Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SignupTextGray, letterSpacing = 0.8.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))
}

@Composable
fun SField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable () -> Unit,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isSuccess: Boolean = false
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = SignupTextGray) },
        leadingIcon = leadingIcon, trailingIcon = trailingIcon,
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = if (isSuccess) SignupGreen else SignupBorderColor,
            focusedBorderColor = if (isSuccess) SignupGreen else SignupBlue,
            unfocusedContainerColor = if (isSuccess) Color(0xFFDCFCE7) else SignupBgGray,
            focusedContainerColor = if (isSuccess) Color(0xFFDCFCE7) else SignupBgGray
        ),
        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = keyboardType), visualTransformation = visualTransformation
    )
}