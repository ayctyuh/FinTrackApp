package com.fintrack.project.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.project.data.database.FinTrackDatabase
import com.fintrack.project.data.model.Notification
import com.fintrack.project.data.model.NotificationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class PinStep { CHECK_OLD, ENTER_NEW, CONFIRM_NEW }

@Composable
fun PinSetupScreen(
    onBackClick: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToHome: () -> Unit = {},     // <-- THÊM
    onAddClick: () -> Unit = {},           // <-- THÊM
    onNavigateToProfile: () -> Unit = {},  // <-- THÊM
    onPinSaved: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var dbPin by remember { mutableStateOf<String?>(null) }
    var currentUserId by remember { mutableIntStateOf(-1) }
    var isLoading by remember { mutableStateOf(true) }

    var currentStep by remember { mutableStateOf(PinStep.ENTER_NEW) }
    var currentInput by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }

    val maxPinLength = 4

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val sharedPreferences = context.getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)
            currentUserId = sharedPreferences.getInt("LOGGED_IN_USER_ID", -1)

            if (currentUserId != -1) {
                val user = FinTrackDatabase.getInstance(context).userDao().getUserById(currentUserId)
                dbPin = user?.pinCode
                currentStep = if (dbPin.isNullOrEmpty()) PinStep.ENTER_NEW else PinStep.CHECK_OLD
            }
            isLoading = false
        }
    }

    fun onNumberClick(number: Int) {
        if (currentInput.length < maxPinLength) {
            currentInput += number.toString()
        }
    }

    fun onBackspaceClick() {
        if (currentInput.isNotEmpty()) {
            currentInput = currentInput.dropLast(1)
        }
    }

    fun verifyAndProceed() {
        if (currentInput.length == maxPinLength) {
            when (currentStep) {
                PinStep.CHECK_OLD -> {
                    if (currentInput == dbPin) {
                        currentInput = ""
                        currentStep = PinStep.ENTER_NEW
                    } else {
                        currentInput = ""
                        Toast.makeText(context, "Mã PIN cũ không chính xác!", Toast.LENGTH_SHORT).show()
                    }
                }
                PinStep.ENTER_NEW -> {
                    newPin = currentInput
                    currentInput = ""
                    currentStep = PinStep.CONFIRM_NEW
                }
                PinStep.CONFIRM_NEW -> {
                    if (currentInput == newPin) {
                        // Lưu vào DB
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                val db = FinTrackDatabase.getInstance(context)
                                val userDao = db.userDao()
                                val notificationDao = db.notificationDao()
                                val user = userDao.getUserById(currentUserId)

                                user?.let {
                                    userDao.updateUser(it.copy(pinCode = newPin))
                                }

                                // PHẦN TẠO THÔNG BÁO CHO VIỆC ĐỔI/THÊM PIN
                                val isChanging = !dbPin.isNullOrEmpty()
                                val notifTitle = if (isChanging) "Thay đổi mã PIN" else "Thiết lập mã PIN"
                                val notifDesc = if (isChanging) "Bạn đã thay đổi mã PIN thành công." else "Thiết lập mã PIN bảo mật thành công."

                                notificationDao.insertNotification(
                                    Notification(
                                        userId = currentUserId,
                                        title = notifTitle,
                                        description = notifDesc,
                                        message = "Mã PIN của bạn đã được cập nhật an toàn. Hãy ghi nhớ mã PIN này để sử dụng khi lưu các giao dịch nhé!",
                                        type = NotificationType.UPDATE, // Hiển thị màu xanh
                                        createdAt = System.currentTimeMillis(),
                                        isRead = false
                                    )
                                )
                            }
                            val msg = if (dbPin.isNullOrEmpty()) "Thiết lập mã PIN thành công!" else "Thay đổi mã PIN thành công!"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            onPinSaved()
                        }
                    } else {
                        currentInput = ""
                        newPin = ""
                        currentStep = PinStep.ENTER_NEW
                        Toast.makeText(context, "Mã PIN không khớp, vui lòng nhập lại!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Scaffold(containerColor = Color(0xFFF8FAFC),
        bottomBar = {
            ProfileBottomNavigationBar(
                onHomeClick = onNavigateToHome,
                onAddClick = onAddClick,
                onProfileClick = onNavigateToProfile,
                onStatisticsClick = onNavigateToStatistics,
                onBudgetClick = onNavigateToBudget,
                currentScreen = "Cá nhân" // Giữ sáng icon Cá nhân vì cài PIN nằm trong mục này
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) { Icon(Icons.Default.ChevronLeft, "Quay lại") }
            }

            Spacer(modifier = Modifier.height(32.dp))

            val title = when (currentStep) {
                PinStep.CHECK_OLD -> "Nhập mã PIN hiện tại"
                PinStep.ENTER_NEW -> "Thiết lập mã PIN mới"
                PinStep.CONFIRM_NEW -> "Xác nhận mã PIN mới"
            }
            Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Nhập 4 số để bảo mật", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(48.dp))

            // 4 Dấu chấm hiển thị PIN
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (i in 0 until maxPinLength) {
                    val isFilled = i < currentInput.length
                    Box(modifier = Modifier.size(20.dp).background(if (isFilled) Color(0xFF2E5BFF) else Color(0xFFE2E8F0), CircleShape))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bàn phím số
            Column(modifier = Modifier.padding(bottom = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                val padModifier = Modifier.size(72.dp).clip(CircleShape)

                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    NumberButton("1", padModifier) { onNumberClick(1) }
                    NumberButton("2", padModifier) { onNumberClick(2) }
                    NumberButton("3", padModifier) { onNumberClick(3) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    NumberButton("4", padModifier) { onNumberClick(4) }
                    NumberButton("5", padModifier) { onNumberClick(5) }
                    NumberButton("6", padModifier) { onNumberClick(6) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    NumberButton("7", padModifier) { onNumberClick(7) }
                    NumberButton("8", padModifier) { onNumberClick(8) }
                    NumberButton("9", padModifier) { onNumberClick(9) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = padModifier)
                    NumberButton("0", padModifier) { onNumberClick(0) }
                    Box(modifier = padModifier.clickable { onBackspaceClick() }.background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Backspace, null, tint = Color(0xFF1E293B))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { verifyAndProceed() },
                    modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E5BFF)),
                    shape = RoundedCornerShape(16.dp),
                    enabled = currentInput.length == 4
                ) {
                    Text("Xác nhận", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun NumberButton(text: String, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier = modifier.clickable(onClick = onClick).background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 28.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
    }
}