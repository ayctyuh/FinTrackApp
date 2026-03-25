package com.fintrack.project.ui.screens

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.project.data.database.FinTrackDatabase
import com.fintrack.project.data.model.Notification
import com.fintrack.project.data.model.NotificationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    // State chứa danh sách thông báo lấy từ DB
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Gọi dữ liệu từ Database khi màn hình được tạo
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val sharedPreferences = context.getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)
            val loggedInUserId = sharedPreferences.getInt("LOGGED_IN_USER_ID", -1)

            if (loggedInUserId != -1) {
                val notificationDao = FinTrackDatabase.getInstance(context).notificationDao()
                // Lấy danh sách thông báo từ DB, Room đã sắp xếp mới nhất lên đầu (ORDER BY createdAt DESC)
                notifications = notificationDao.getUserNotifications(loggedInUserId)
            }
            isLoading = false
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // Phần Header màu xanh
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2E5BFF))
                    .padding(
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                        bottom = 16.dp, start = 16.dp, end = 16.dp
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.ChevronLeft, "Quay lại", tint = Color.White)
                    }

                    Text(
                        text = "Thông báo",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f).offset(x = (-18).dp)
                    )
                }
            }

            if (isLoading) {
                // Hiển thị vòng tròn loading trong lúc chờ lấy dữ liệu
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2E5BFF))
                }
            } else if (notifications.isEmpty()) {
                // TRẠNG THÁI TRỐNG (Khi chưa có thông báo)
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Chưa có thông báo nào",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Các nhắc nhở và cập nhật mới nhất sẽ được hiển thị tại đây.",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // DANH SÁCH THÔNG BÁO TỪ DATABASE
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
                ) {
                    // Để đơn giản, hiện tại ta hiển thị danh sách thẳng.
                    // Nếu bạn muốn chia nhóm theo ngày (Hôm nay, Hôm qua), cần thêm logic group by ngày ở ViewModel.

                    items(notifications) { notif ->
                        // Xác định icon và màu sắc dựa trên NotificationType
                        val iconData = getIconDataForType(notif.type)

                        NotificationItem(
                            icon = iconData.icon,
                            iconBgColor = iconData.bgColor,
                            title = notif.title,
                            description = notif.description,
                            time = formatDate(notif.createdAt),
                            hasDot = !notif.isRead, // Nếu isRead = false thì hiện chấm đỏ
                            isTransaction = notif.type == NotificationType.TRANSACTION // Nếu là transaction thì có thể hiện tag sau này
                        )
                    }
                }
            }
        }
    }
}

// Data class nhỏ để gộp icon và màu sắc trả về
data class NotificationIconData(val icon: ImageVector, val bgColor: Color)

// Hàm xác định icon và màu tùy theo loại thông báo
fun getIconDataForType(type: NotificationType): NotificationIconData {
    return when (type) {
        NotificationType.REMINDER -> NotificationIconData(Icons.Default.Notifications, Color(0xFF2E5BFF)) // Xanh dương
        NotificationType.UPDATE -> NotificationIconData(Icons.Default.Check, Color(0xFF10B981)) // Xanh lá
        NotificationType.TRANSACTION -> NotificationIconData(Icons.Default.AttachMoney, Color(0xFF10B981)) // Xanh lá
        NotificationType.BUDGET_ALERT -> NotificationIconData(Icons.Default.Warning, Color(0xFFEF4444)) // Đỏ
    }
}

// Hàm format thời gian (Long -> String: VD: 17:00 • 20 Tháng 6)
fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm • dd 'Tháng' MM", Locale("vi", "VN"))
    return sdf.format(Date(timestamp))
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF94A3B8),
        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
    )
}

@Composable
fun NotificationItem(
    icon: ImageVector, iconBgColor: Color,
    title: String, description: String, time: String,
    hasDot: Boolean, isTransaction: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            // Icon
            Box(
                modifier = Modifier.size(40.dp).background(iconBgColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, fontSize = 12.sp, color = Color(0xFF64748B), maxLines = 2) // Cho description tối đa 2 dòng

                if (isTransaction) {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Tạm thời fix cứng TagItem, sau này bạn có thể query thêm Category để điền vào đây
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TagItem("Giao dịch", Color(0xFFE0E7FF), Color(0xFF2E5BFF))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(time, fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            }

            // Dấu chấm đỏ/cam thông báo chưa đọc
            if (hasDot) {
                Box(modifier = Modifier.size(8.dp).background(Color(0xFFF59E0B), CircleShape))
            }
        }
    }
}

@Composable
fun TagItem(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier.background(bgColor, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}