package com.fintrack.project.ui.screens

import android.content.Context
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.fintrack.project.data.database.FinTrackDatabase
import com.fintrack.project.data.model.Notification
import com.fintrack.project.data.model.NotificationType
import com.fintrack.project.data.model.Transaction
import com.fintrack.project.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSeeAllClick: () -> Unit = {},
    onBudgetClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onStatisticsClick: () -> Unit = {},
    onEditTransactionClick: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var username by remember { mutableStateOf("Đang tải...") }
    var currentBalance by remember { mutableDoubleStateOf(0.0) }
    var monthlyExpense by remember { mutableDoubleStateOf(0.0) }
    var recentTransactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var unreadNotiCount by remember { mutableIntStateOf(0) }
    var currentUserId by remember { mutableIntStateOf(-1) }
    var refreshKey by remember { mutableIntStateOf(0) }

    // SỬA: Map lưu trữ toàn bộ Object Category thay vì chỉ tên
    var categoriesMap by remember { mutableStateOf<Map<Int, com.fintrack.project.data.model.Category>>(emptyMap()) }
    var selectedTxn by remember { mutableStateOf<Transaction?>(null) }

    var userGoalKey by remember { mutableStateOf("") }
    var goalName by remember { mutableStateOf("") }
    var goalAmount by remember { mutableDoubleStateOf(0.0) }
    var goalIconStr by remember { mutableStateOf("Flag") }
    var showGoalDialog by remember { mutableStateOf(false) }

    LaunchedEffect(refreshKey) {
        withContext(Dispatchers.IO) {
            val sharedPreferences = context.getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)
            val loggedInUserId = sharedPreferences.getInt("LOGGED_IN_USER_ID", -1)
            currentUserId = loggedInUserId

            if (loggedInUserId != -1) {
                val db = FinTrackDatabase.getInstance(context)
                val userDao = db.userDao()
                val transactionDao = db.transactionDao()
                val categoryDao = db.categoryDao()

                val user = userDao.getUserById(loggedInUserId)
                username = user?.username ?: "Người dùng mới"

                val generatedKey = "${loggedInUserId}_${user?.username}"
                userGoalKey = generatedKey

                goalName = sharedPreferences.getString("GOAL_NAME_$generatedKey", "") ?: ""
                goalAmount = sharedPreferences.getFloat("GOAL_AMOUNT_$generatedKey", 0f).toDouble()
                goalIconStr = sharedPreferences.getString("GOAL_ICON_$generatedKey", "Flag") ?: "Flag"

                val existingCategories = categoryDao.getUserCategories(loggedInUserId)
                if (existingCategories.isEmpty()) {
                    com.fintrack.project.data.model.DEFAULT_EXPENSE_CATEGORIES.forEach { catName ->
                        categoryDao.insertCategory(com.fintrack.project.data.model.Category(userId = loggedInUserId, name = catName, type = com.fintrack.project.data.model.CategoryType.EXPENSE, isDefault = true))
                    }
                    com.fintrack.project.data.model.DEFAULT_INCOME_CATEGORIES.forEach { catName ->
                        categoryDao.insertCategory(com.fintrack.project.data.model.Category(userId = loggedInUserId, name = catName, type = com.fintrack.project.data.model.CategoryType.INCOME, isDefault = true))
                    }
                }

                // SỬA: Lấy toàn bộ model để sau này trích xuất icon
                categoriesMap = categoryDao.getUserCategories(loggedInUserId).associateBy { it.id }

                val calendar = Calendar.getInstance()
                val monthStart = calendar.clone() as Calendar
                monthStart.set(Calendar.DAY_OF_MONTH, 1); monthStart.set(Calendar.HOUR_OF_DAY, 0); monthStart.set(Calendar.MINUTE, 0); monthStart.set(Calendar.SECOND, 0)
                val monthEnd = calendar.clone() as Calendar
                monthEnd.add(Calendar.MONTH, 1); monthEnd.set(Calendar.DAY_OF_MONTH, 1); monthEnd.add(Calendar.MILLISECOND, -1)

                val totalInc = transactionDao.getTotalAmount(loggedInUserId, TransactionType.INCOME) ?: 0.0
                val totalExp = transactionDao.getTotalAmount(loggedInUserId, TransactionType.EXPENSE) ?: 0.0
                currentBalance = totalInc - totalExp
                monthlyExpense = transactionDao.getTotalAmountByDateRange(loggedInUserId, TransactionType.EXPENSE, monthStart.timeInMillis, monthEnd.timeInMillis) ?: 0.0

                if (goalAmount > 0) {
                    val lastReminderTime = sharedPreferences.getLong("LAST_GOAL_REMINDER_$loggedInUserId", 0L)
                    val currentTime = System.currentTimeMillis()
                    val oneDayInMillis = 24 * 60 * 60 * 1000L

                    if (currentTime - lastReminderTime > oneDayInMillis) {
                        val progress = (currentBalance / goalAmount * 100).toInt().coerceIn(0, 100)
                        if (progress < 100) {
                            db.notificationDao().insertNotification(
                                Notification(userId = loggedInUserId, title = "Nhắc nhở mục tiêu \uD83D\uDCAA", description = "Tiến độ hiện tại: $progress%", message = "Đừng quên bạn đang thực hiện mục tiêu '$goalName'. Hiện bạn đã đạt được $progress%. Tiếp tục duy trì thói quen ghi chép thu chi nhé!", type = NotificationType.REMINDER, createdAt = currentTime, isRead = false)
                            )
                            sharedPreferences.edit().putLong("LAST_GOAL_REMINDER_$loggedInUserId", currentTime).apply()
                        }
                    }
                }

                recentTransactions = transactionDao.getRecentTransactions(loggedInUserId)
                unreadNotiCount = db.notificationDao().getUnreadCount(loggedInUserId)
            } else {
                username = "Khách"
            }
        }
    }

    Scaffold(
        bottomBar = {
            ProfileBottomNavigationBar(onHomeClick = {}, onAddClick = onAddClick, onProfileClick = onProfileClick, onBudgetClick = onBudgetClick, onStatisticsClick = onStatisticsClick, currentScreen = "Trang chủ")
        },
        containerColor = Color(0xFFF8FAFC),
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(bottom = paddingValues.calculateBottomPadding()).verticalScroll(rememberScrollState())) {
            HeaderSection(username = username, balance = currentBalance, monthlyExpense = monthlyExpense, unreadNotiCount = unreadNotiCount, onNotificationClick = onNotificationClick)
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {

                SavingGoalCard(goalName = goalName, goalAmount = goalAmount, goalIconStr = goalIconStr, currentBalance = currentBalance, onClick = { showGoalDialog = true })

                Spacer(modifier = Modifier.height(24.dp))
                TimeFilterTabs()
                Spacer(modifier = Modifier.height(24.dp))
                if (recentTransactions.isEmpty()) {
                    RecentTransactionsEmptyState(onSeeAllClick = onSeeAllClick)
                } else {
                    RecentTransactionsList(transactions = recentTransactions, categoriesMap = categoriesMap, onSeeAllClick = onSeeAllClick, onTransactionClick = { txn -> selectedTxn = txn })
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showGoalDialog) {
            GoalSetupDialog(
                currentName = goalName, currentAmount = goalAmount, currentIcon = goalIconStr, onDismiss = { showGoalDialog = false },
                onSave = { name, amount, iconStr ->
                    goalName = name; goalAmount = amount; goalIconStr = iconStr
                    val prefs = context.getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("GOAL_NAME_$userGoalKey", name).putFloat("GOAL_AMOUNT_$userGoalKey", amount.toFloat()).putString("GOAL_ICON_$userGoalKey", iconStr).apply()
                    showGoalDialog = false
                }
            )
        }

        selectedTxn?.let { txn ->
            TransactionDetailDialog(
                transaction = txn,
                categoryName = categoriesMap[txn.categoryId]?.name ?: "Khác",
                currentBalance = currentBalance,
                onDismiss = { selectedTxn = null },
                onEdit = { transactionToEdit ->
                    // Đóng Popup và mở màn hình Sửa giao dịch
                    selectedTxn = null
                    onEditTransactionClick(transactionToEdit.id)
                },
                onDelete = { transactionToDelete ->
                    // Đóng Popup và gọi lệnh xóa vào DB
                    selectedTxn = null
                    coroutineScope.launch(Dispatchers.IO) {
                        val db = FinTrackDatabase.getInstance(context)
                        db.transactionDao().deleteTransaction(transactionToDelete)

                        // Xóa xong thì +1 refreshKey để App tự tính lại Số dư và Chi tiêu tháng
                        withContext(Dispatchers.Main) {
                            refreshKey++
                        }
                    }
                }
            )
        }
    }
}

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return format.format(amount)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderSection(username: String, balance: Double, monthlyExpense: Double, unreadNotiCount: Int, onNotificationClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)).background(Brush.verticalGradient(colors = listOf(Color(0xFF1A3FBF), Color(0xFF3B82F6))))) {
        Box(modifier = Modifier.size(160.dp).align(Alignment.TopEnd).offset(x = 40.dp, y = (-40).dp).background(Color.White.copy(alpha = 0.08f), CircleShape))
        Box(modifier = Modifier.size(100.dp).align(Alignment.BottomStart).offset(x = (-30).dp, y = 20.dp).background(Color.White.copy(alpha = 0.08f), CircleShape))
        Column(modifier = Modifier.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text(text = "Xin chào \uD83D\uDC4B", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp); Text(text = username, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                IconButton(onClick = onNotificationClick, modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)) {
                    BadgedBox(badge = { if (unreadNotiCount > 0) { Badge(containerColor = Color.Red, modifier = Modifier.size(10.dp).offset(x = (7).dp, y = (-7).dp)) } }) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Thông báo", tint = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AccountBalanceWallet, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("Số dư hiện tại", color = Color(0xFF64748B), fontSize = 12.sp) }
                    Text(formatCurrency(balance), color = Color(0xFF10B981), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.width(1.dp).height(50.dp).background(Color(0xFFF1F5F9)))
                Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.MoneyOff, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("Chi tiêu tháng", color = Color(0xFF64748B), fontSize = 12.sp) }
                    Text(formatCurrency(monthlyExpense), color = Color(0xFFEF4444), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SavingGoalCard(goalName: String, goalAmount: Double, goalIconStr: String, currentBalance: Double, onClick: () -> Unit) {
    val isConfigured = goalAmount > 0
    val displayName = if (isConfigured) goalName.ifEmpty { "Chưa đặt tên" } else "Bấm để thiết lập"
    val displayIcon = getIconForGoal(if (isConfigured) goalIconStr else "Flag")

    val progress = if (isConfigured) (currentBalance / goalAmount).coerceIn(0.0, 1.0).toFloat() else 0f
    val remaining = if (isConfigured) maxOf(0.0, goalAmount - currentBalance) else 0.0

    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFF2548C6)).clickable { onClick() }) {
        Box(modifier = Modifier.size(120.dp).align(Alignment.TopEnd).offset(x = 30.dp, y = (-30).dp).background(Color.White.copy(alpha = 0.05f), CircleShape))
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {

            Column(modifier = Modifier.weight(1.2f)) {
                Box(modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Icon(displayIcon, null, tint = Color.White)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("MỤC TIÊU CỦA BẠN", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(displayName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }

            Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress }, modifier = Modifier.fillMaxSize(), color = Color(0xFF4ADE80), trackColor = Color.White.copy(alpha = 0.2f), strokeWidth = 6.dp
                )
                Text(text = "${(progress * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1.3f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF4ADE80), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Cần tiết kiệm", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        Text(if(isConfigured) formatCurrency(goalAmount) else "0 đ", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFF87171), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Còn thiếu", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        Text(if(isConfigured) formatCurrency(remaining) else "0 đ", color = Color(0xFFF87171), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSetupDialog(
    currentName: String, currentAmount: Double, currentIcon: String, onDismiss: () -> Unit, onSave: (String, Double, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var amountText by remember { mutableStateOf(if (currentAmount > 0) currentAmount.toLong().toString() else "") }
    var selectedIcon by remember { mutableStateOf(currentIcon.ifEmpty { "Flag" }) }
    val iconOptions = listOf("Flag", "Car", "Home", "Flight", "Laptop", "School", "Favorite")

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(12.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(56.dp).background(Color(0xFFEFF6FF), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(getIconForGoal(selectedIcon), null, tint = Color(0xFF2E5BFF), modifier = Modifier.size(28.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Thiết lập Mục tiêu", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Text("Cùng nhau chinh phục cột mốc mới!", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên mục tiêu (VD: Đóng học phí)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E5BFF), unfocusedBorderColor = Color(0xFFE2E8F0)))
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Số tiền cần tiết kiệm (đ)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E5BFF), unfocusedBorderColor = Color(0xFFE2E8F0)))

                Spacer(modifier = Modifier.height(20.dp))
                Text("Chọn biểu tượng:", fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    iconOptions.take(6).forEach { iconKey ->
                        val isSelected = selectedIcon == iconKey
                        Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(if (isSelected) Color(0xFF2E5BFF) else Color(0xFFF1F5F9)).clickable { selectedIcon = iconKey }, contentAlignment = Alignment.Center) {
                            Icon(getIconForGoal(iconKey), contentDescription = null, tint = if (isSelected) Color.White else Color(0xFF64748B), modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))) { Text("Hủy", fontWeight = FontWeight.Bold) }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(onClick = { val amount = amountText.toDoubleOrNull() ?: 0.0; if (name.isNotBlank() && amount > 0) { onSave(name, amount, selectedIcon) } }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E5BFF))) { Text("Lưu lại", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

fun getIconForGoal(name: String): ImageVector {
    return when (name) {
        "Car" -> Icons.Default.DirectionsCar
        "Home" -> Icons.Default.Home
        "Flight" -> Icons.Default.Flight
        "Laptop" -> Icons.Default.Laptop
        "School" -> Icons.Default.School
        "Favorite" -> Icons.Default.Favorite
        else -> Icons.Default.Flag
    }
}

@Composable
fun TimeFilterTabs() {
    var selectedTab by remember { mutableStateOf(2) }
    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFE2E8F0), RoundedCornerShape(12.dp)).padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        listOf("Ngày", "Tuần", "Tháng").forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSelected) Color(0xFF2E5BFF) else Color.Transparent).clickable { selectedTab = index }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                Text(text = title, color = if (isSelected) Color.White else Color(0xFF64748B), fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable
fun RecentTransactionsEmptyState(onSeeAllClick: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Giao dịch gần đây", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)); Text(text = "Xem tất cả →", fontSize = 12.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.Medium, modifier = Modifier.clip(RoundedCornerShape(4.dp)).clickable { onSeeAllClick() }.padding(4.dp)) }
        Spacer(modifier = Modifier.height(24.dp))
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ReceiptLong, null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp)); Text("Chưa có giao dịch nào", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(8.dp)); Text("Hãy thêm khoản thu hoặc chi đầu tiên\nđể bắt đầu quản lý tài chính nhé!", fontSize = 14.sp, color = Color(0xFF94A3B8), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ProfileBottomNavigationBar(
    onHomeClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onBudgetClick: () -> Unit = {},
    onStatisticsClick: () -> Unit = {},
    currentScreen: String = "Cá nhân"
) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        NavigationBarItem(icon = { Icon(Icons.Default.Home, "Trang chủ") }, label = { Text("Trang chủ", fontSize = 10.sp) }, selected = currentScreen == "Trang chủ", onClick = onHomeClick, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF2E5BFF), selectedTextColor = Color(0xFF2E5BFF), indicatorColor = Color(0xFFE0E7FF)))
        NavigationBarItem(icon = { Icon(Icons.Default.BarChart, "Thống kê") }, label = { Text("Thống kê", fontSize = 10.sp) }, selected = currentScreen == "Thống kê", onClick = onStatisticsClick, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF2E5BFF), selectedTextColor = Color(0xFF2E5BFF), indicatorColor = Color(0xFFE0E7FF)))
        NavigationBarItem(icon = { Box(modifier = Modifier.offset(y = -6.dp).size(50.dp).background(Color(0xFF2E5BFF), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, null, tint = Color.White) } }, label = null //{ Text("Thêm", fontSize = 10.sp) }//
            , selected = false, onClick = onAddClick)
        NavigationBarItem(icon = { Icon(Icons.Default.PieChart, "Ngân sách") }, label = { Text("Ngân sách", fontSize = 10.sp) }, selected = currentScreen == "Ngân sách", onClick = onBudgetClick, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF2E5BFF), selectedTextColor = Color(0xFF2E5BFF), indicatorColor = Color(0xFFE0E7FF)))
        NavigationBarItem(icon = { Icon(Icons.Default.Person, "Cá nhân") }, label = { Text("Cá nhân", fontSize = 10.sp) }, selected = currentScreen == "Cá nhân", onClick = onProfileClick, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF2E5BFF), selectedTextColor = Color(0xFF2E5BFF), indicatorColor = Color(0xFFE0E7FF)))
    }
}

@Composable
fun RecentTransactionsList(transactions: List<Transaction>, categoriesMap: Map<Int, com.fintrack.project.data.model.Category>, onSeeAllClick: () -> Unit, onTransactionClick: (Transaction) -> Unit) {
    val timeFormatter = SimpleDateFormat("HH:mm - dd/MM", Locale("vi", "VN"))
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Giao dịch gần đây", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)); Text("Xem tất cả →", fontSize = 12.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.Medium, modifier = Modifier.clip(RoundedCornerShape(4.dp)).clickable { onSeeAllClick() }.padding(4.dp)) }
        Spacer(modifier = Modifier.height(16.dp))
        transactions.forEach { txn ->
            val isInc = txn.type == TransactionType.INCOME
            // LẤY OBJECT CATEGORY RA
            val category = categoriesMap[txn.categoryId]
            val categoryName = category?.name ?: "Khác"
            val displayIcon = resolveCategoryIcon(category?.icon ?: category?.name)
            val displayDescription = if (!txn.description.isNullOrEmpty()) txn.description else categoryName

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { onTransactionClick(txn) },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).background(if (isInc) Color(0xFFD1FAE5) else Color(0xFFFEE2E2), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Icon(displayIcon, null, tint = if (isInc) Color(0xFF10B981) else Color(0xFFEF4444)) }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(displayDescription, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                        Text(timeFormatter.format(Date(txn.transactionDate)), fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                    Text(text = (if (isInc) "+" else "-") + formatCurrency(txn.amount), color = if (isInc) Color(0xFF10B981) else Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun TransactionDetailDialog(transaction: Transaction, categoryName: String, currentBalance: Double, onDismiss: () -> Unit, onEdit: (Transaction) -> Unit = {},   onDelete: (Transaction) -> Unit = {}) {
    val isInc = transaction.type == TransactionType.INCOME
    val formatter = SimpleDateFormat("dd 'Tháng' MM, yyyy", Locale("vi", "VN"))
    val mainColor = if (isInc) Color(0xFF10B981) else Color(0xFFEF4444)
    var showConfirmDelete by remember { mutableStateOf(false) }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("Xóa giao dịch", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn xóa giao dịch này? Số dư và các báo cáo sẽ được cập nhật lại.") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDelete = false
                    onDelete(transaction) // Gọi lệnh xóa
                }) { Text("Xóa", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) { Text("Hủy", color = Color.Gray) }
            },
            containerColor = Color.White
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(8.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                // ─── THÊM 2 NÚT SỬA VÀ XÓA Ở ĐÂY ───
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("CHI TIẾT GIAO DỊCH", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Nút Sửa
                        IconButton(onClick = { onEdit(transaction) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Edit, "Sửa", tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        // Nút Xóa
                        IconButton(onClick = { showConfirmDelete = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, "Xóa", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                Text("#TXN-${transaction.id.toString().padStart(5, '0')}", color = Color.Gray, fontSize = 10.sp)
                Spacer(modifier = Modifier.height(16.dp))

                DetailRow("Số tiền", (if (isInc) "+" else "-") + formatCurrency(transaction.amount), mainColor, true)
                DetailRow("Danh mục", categoryName, Color.Black)
                DetailRow("Ngày giao dịch", formatter.format(Date(transaction.transactionDate)), Color.Black)
                DetailRow("Mô tả", transaction.description ?: categoryName, Color.Black)
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF1F5F9))
                DetailRow("Số dư hiện tại", formatCurrency(currentBalance), Color(0xFF2E5BFF), true)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E5BFF)), shape = RoundedCornerShape(12.dp)) {
                    Text("Đóng", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
