package com.fintrack.project.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.project.data.database.FinTrackDatabase
import com.fintrack.project.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DashboardScreen(
    onLogout: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val context = LocalContext.current

    // States lưu trữ thông tin từ Database
    var username by remember { mutableStateOf("Đang tải...") }
    var currentBalance by remember { mutableDoubleStateOf(0.0) }
    var monthlyExpense by remember { mutableDoubleStateOf(0.0) }
    var weeklyIncome by remember { mutableDoubleStateOf(0.0) }
    var weeklyExpense by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val sharedPreferences = context.getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)
            val loggedInUserId = sharedPreferences.getInt("LOGGED_IN_USER_ID", -1)

            if (loggedInUserId != -1) {
                val db = FinTrackDatabase.getInstance(context)
                val userDao = db.userDao()
                val transactionDao = db.transactionDao()

                val user = userDao.getUserById(loggedInUserId)
                username = user?.username ?: "Người dùng mới"

                val calendar = Calendar.getInstance()
                val monthStart = calendar.clone() as Calendar
                monthStart.set(Calendar.DAY_OF_MONTH, 1)
                monthStart.set(Calendar.HOUR_OF_DAY, 0)
                monthStart.set(Calendar.MINUTE, 0)
                monthStart.set(Calendar.SECOND, 0)

                val monthEnd = calendar.clone() as Calendar
                monthEnd.add(Calendar.MONTH, 1)
                monthEnd.set(Calendar.DAY_OF_MONTH, 1)
                monthEnd.add(Calendar.MILLISECOND, -1)

                val weekStart = calendar.clone() as Calendar
                weekStart.firstDayOfWeek = Calendar.MONDAY
                weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                weekStart.set(Calendar.HOUR_OF_DAY, 0)

                val weekEnd = calendar.clone() as Calendar
                weekEnd.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                weekEnd.set(Calendar.HOUR_OF_DAY, 23)

                val totalInc = transactionDao.getTotalAmount(loggedInUserId, TransactionType.INCOME) ?: 0.0
                val totalExp = transactionDao.getTotalAmount(loggedInUserId, TransactionType.EXPENSE) ?: 0.0
                currentBalance = totalInc - totalExp

                monthlyExpense = transactionDao.getTotalAmountByDateRange(
                    loggedInUserId, TransactionType.EXPENSE, monthStart.timeInMillis, monthEnd.timeInMillis
                ) ?: 0.0

                weeklyIncome = transactionDao.getTotalAmountByDateRange(
                    loggedInUserId, TransactionType.INCOME, weekStart.timeInMillis, weekEnd.timeInMillis
                ) ?: 0.0

                weeklyExpense = transactionDao.getTotalAmountByDateRange(
                    loggedInUserId, TransactionType.EXPENSE, weekStart.timeInMillis, weekEnd.timeInMillis
                ) ?: 0.0
            } else {
                username = "Khách"
            }
        }
    }

    Scaffold(
        bottomBar = {
            // Đã sửa: Truyền tham số vào đây
            BottomNavigationBar(onProfileClick = onProfileClick)
        },
        containerColor = Color(0xFFF8FAFC),
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            HeaderSection(
                username = username,
                balance = currentBalance,
                monthlyExpense = monthlyExpense,
                onNotificationClick = onNotificationClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                SavingGoalCard(
                    weeklyIncome = weeklyIncome,
                    weeklyExpense = weeklyExpense
                )
                Spacer(modifier = Modifier.height(24.dp))
                TimeFilterTabs()
                Spacer(modifier = Modifier.height(24.dp))
                RecentTransactionsEmptyState()
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    return format.format(amount)
}

@Composable
fun HeaderSection(username: String, balance: Double, monthlyExpense: Double, onNotificationClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Brush.verticalGradient(colors = listOf(Color(0xFF1A3FBF), Color(0xFF3B82F6))))
    ) {
        Box(modifier = Modifier.size(160.dp).align(Alignment.TopEnd).offset(x = 40.dp, y = (-40).dp).background(Color.White.copy(alpha = 0.08f), CircleShape))
        Box(modifier = Modifier.size(100.dp).align(Alignment.BottomStart).offset(x = (-30).dp, y = 20.dp).background(Color.White.copy(alpha = 0.08f), CircleShape))

        Column(
            modifier = Modifier.padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
                bottom = 32.dp, start = 24.dp, end = 24.dp
            )
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "Xin chào \uD83D\uDC4B", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Text(text = username, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onNotificationClick, modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Thông báo", tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalanceWallet, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Số dư hiện tại", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                    Text(formatCurrency(balance), color = Color(0xFF4ADE80), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.3f)))
                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MoneyOff, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chi tiêu tháng", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                    Text(formatCurrency(monthlyExpense), color = Color(0xFFF87171), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SavingGoalCard(weeklyIncome: Double, weeklyExpense: Double) {
    val isNewUser = weeklyIncome == 0.0 && weeklyExpense == 0.0
    val goalName = if (isNewUser) "Chưa thiết lập" else "Xe hơi"
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFF2548C6))) {
        Box(modifier = Modifier.size(120.dp).align(Alignment.TopEnd).offset(x = 30.dp, y = (-30).dp).background(Color.White.copy(alpha = 0.05f), CircleShape))
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Icon(if (isNewUser) Icons.Default.Flag else Icons.Default.Schedule, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("MỤC TIÊU TIẾT KIỆM", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(goalName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(24.dp).background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AttachMoney, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Thu tuần này", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        Text(formatCurrency(weeklyIncome), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(24.dp).background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CalendarToday, null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Chi tuần này", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        val expenseText = if (weeklyExpense > 0) "-${formatCurrency(weeklyExpense)}" else formatCurrency(0.0)
                        Text(expenseText, color = Color(0xFFF87171), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TimeFilterTabs() {
    var selectedTab by remember { mutableStateOf(2) }
    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFE2E8F0), RoundedCornerShape(12.dp)).padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        listOf("Ngày", "Tuần", "Tháng").forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSelected) Color(0xFF2E5BFF) else Color.Transparent).padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                Text(text = title, color = if (isSelected) Color.White else Color(0xFF64748B), fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable
fun RecentTransactionsEmptyState() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Giao dịch gần đây", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text("Xem tất cả →", fontSize = 12.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ReceiptLong, null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Chưa có giao dịch nào", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
            Text("Hãy thêm khoản thu hoặc chi đầu tiên\nđể bắt đầu quản lý tài chính nhé!", fontSize = 14.sp, color = Color(0xFF94A3B8), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun BottomNavigationBar(onProfileClick: () -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Trang chủ", fontSize = 10.sp) },
            selected = true,
            onClick = { },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF2E5BFF), selectedTextColor = Color(0xFF2E5BFF), indicatorColor = Color(0xFFE0E7FF))
        )
        NavigationBarItem(icon = { Icon(Icons.Default.BarChart, null) }, label = { Text("Thống kê", fontSize = 10.sp) }, selected = false, onClick = { })
        NavigationBarItem(
            icon = {
                Box(modifier = Modifier.size(40.dp).background(Color(0xFF2E5BFF), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                }
            },
            label = { Text("Thêm", fontSize = 10.sp) },
            selected = false,
            onClick = { }
        )
        NavigationBarItem(icon = { Icon(Icons.Default.PieChart, null) }, label = { Text("Ngân sách", fontSize = 10.sp) }, selected = false, onClick = { })
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("Cá nhân", fontSize = 10.sp) },
            selected = false,
            onClick = onProfileClick
        )
    }
}