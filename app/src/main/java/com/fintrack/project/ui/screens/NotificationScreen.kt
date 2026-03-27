package com.fintrack.project.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.project.data.database.FinTrackDatabase
import com.fintrack.project.data.model.Notification
import com.fintrack.project.data.model.NotificationType
import com.fintrack.project.data.model.Transaction
import com.fintrack.project.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentUserId by remember { mutableIntStateOf(-1) }

    var selectedTxn by remember { mutableStateOf<Transaction?>(null) }
    // MỚI: 2 Map để phục vụ tra cứu
    var categoryIdMap by remember { mutableStateOf<Map<Int, com.fintrack.project.data.model.Category>>(emptyMap()) }
    var categoryNameMap by remember { mutableStateOf<Map<String, com.fintrack.project.data.model.Category>>(emptyMap()) }
    var currentBalance by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val sharedPreferences = context.getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)
            currentUserId = sharedPreferences.getInt("LOGGED_IN_USER_ID", -1)

            if (currentUserId != -1) {
                val db = FinTrackDatabase.getInstance(context)
                notifications = db.notificationDao().getUserNotifications(currentUserId)

                val cats = db.categoryDao().getUserCategories(currentUserId)
                categoryIdMap = cats.associateBy { it.id }
                categoryNameMap = cats.associateBy { it.name } // Tra cứu ngược để tìm Icon

                val totalInc = db.transactionDao().getTotalAmount(currentUserId, TransactionType.INCOME) ?: 0.0
                val totalExp = db.transactionDao().getTotalAmount(currentUserId, TransactionType.EXPENSE) ?: 0.0
                currentBalance = totalInc - totalExp
            }
            isLoading = false
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = { Text("Thông báo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ChevronLeft, "Quay lại") }
                },
                actions = {
                    TextButton(onClick = {
                        scope.launch(Dispatchers.IO) {
                            if (currentUserId != -1) {
                                val db = FinTrackDatabase.getInstance(context)
                                db.notificationDao().markAllAsRead(currentUserId)
                                notifications = db.notificationDao().getUserNotifications(currentUserId)
                            }
                        }
                    }) {
                        Text("Đánh dấu đã đọc", color = Color(0xFF2E5BFF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2E5BFF))
                }
            } else if (notifications.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.NotificationsOff, null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Chưa có thông báo nào", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Các nhắc nhở và cập nhật mới nhất sẽ được hiển thị tại đây.", fontSize = 14.sp, color = Color(0xFF94A3B8), textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                ) {
                    items(notifications) { notif ->
                        val isTransaction = notif.type == NotificationType.TRANSACTION
                        val isIncome = notif.message.contains("Khoản thu")

                        val amountMatch = Regex("""([\d.,]+\s*[đ₫])""").find(notif.message)
                        val amountStr = amountMatch?.value ?: ""

                        // SỬA: Lấy icon dựa vào name map
                        val category = categoryNameMap[notif.title]
                        val displayIcon = if (isTransaction) resolveCategoryIcon(category?.icon ?: category?.name) else getIconDataForType(notif.type).icon

                        val iconBgColor = if (isTransaction) {
                            if (isIncome) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
                        } else {
                            getIconDataForType(notif.type).bgColor
                        }
                        val iconTintColor = if (isTransaction) {
                            if (isIncome) Color(0xFF10B981) else Color(0xFFEF4444)
                        } else {
                            Color.White
                        }

                        val displayDesc = if (isTransaction && notif.description.contains("Ghi nhận Khoản")) {
                            "Bạn vừa có thêm 1 giao dịch"
                        } else {
                            notif.description
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable {
                                    scope.launch(Dispatchers.IO) {
                                        val db = FinTrackDatabase.getInstance(context)
                                        db.notificationDao().markAsRead(notif.id)
                                        notifications = db.notificationDao().getUserNotifications(currentUserId)

                                        if (notif.transactionId != null) {
                                            selectedTxn = db.transactionDao().getTransactionById(notif.transactionId)
                                        }
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(48.dp).background(iconBgColor, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Icon(displayIcon, contentDescription = null, tint = iconTintColor, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                                        if (!isTransaction) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("🔔", fontSize = 14.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(displayDesc, fontSize = 13.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Schedule, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(formatDate(notif.createdAt), fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    }
                                }

                                if (isTransaction && amountStr.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = (if (isIncome) "+" else "-") + amountStr,
                                        color = iconTintColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }

                                if (!notif.isRead) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFF59E0B), CircleShape))
                                }
                            }
                        }
                    }
                }
            }
        }

        selectedTxn?.let { txn ->
            TransactionDetailDialog(
                transaction = txn,
                categoryName = categoryIdMap[txn.categoryId]?.name ?: "Khác", // SỬA NỐT CHỖ NÀY
                currentBalance = currentBalance,
                onDismiss = { selectedTxn = null }
            )
        }
    }
}

data class NotificationIconData(val icon: ImageVector, val bgColor: Color)

fun getIconDataForType(type: NotificationType): NotificationIconData {
    return when (type) {
        NotificationType.REMINDER -> NotificationIconData(Icons.Default.Notifications, Color(0xFF2E5BFF))
        NotificationType.UPDATE -> NotificationIconData(Icons.Default.Check, Color(0xFF10B981))
        NotificationType.TRANSACTION -> NotificationIconData(Icons.Default.AttachMoney, Color(0xFF10B981))
        NotificationType.BUDGET_ALERT -> NotificationIconData(Icons.Default.Warning, Color(0xFFEF4444))
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale("vi", "VN"))
    return sdf.format(Date(timestamp))
}