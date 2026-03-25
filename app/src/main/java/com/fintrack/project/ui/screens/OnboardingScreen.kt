package com.fintrack.project.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.project.R

@Composable
fun OnboardingScreen(
    onNextClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val darkBlue = Color(0xFF1A3FBF)
    val lightGray = Color(0xFFF8FAFC)

    Box(modifier = Modifier.fillMaxSize().background(darkBlue)) {

        // --- Phần Header màu xanh ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp, start = 24.dp, end = 24.dp)
        ) {
            // Tag nhỏ xíu ở trên cùng
            Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF4ADE80)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("FinTrack • Quản lý tài chính", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Chào mừng đến\nvới FinTrack \uD83D\uDC4B", // Icon bàn tay vẫy
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 40.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Quản lý thu chi thông minh,\nlàm chủ tài chính cá nhân của bạn.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
        }

        // --- Phần Card màu trắng ở dưới ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f) // Chiếm 65% màn hình ở dưới
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Placeholder cho hình ảnh minh họa (thay R.drawable.your_image bằng ảnh của bạn)
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(Color(0xFFE0F2FE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_start),
                        contentDescription = "Hình minh họa chào mừng",
                        modifier = Modifier.fillMaxSize() // Bạn có thể thêm .padding(16.dp) vào đây nếu ảnh bị sát viền quá
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Danh sách tính năng
                FeatureItem(
                    icon = Icons.Default.BarChart,
                    iconBgColor = Color(0xFF3B82F6),
                    title = "Thống kê thu - chi",
                    description = "Biểu đồ trực quan theo tuần, tháng, năm"
                )
                Spacer(modifier = Modifier.height(12.dp))
                FeatureItem(
                    icon = Icons.Default.TrackChanges,
                    iconBgColor = Color(0xFF10B981),
                    title = "Quản lý giao dịch",
                    description = "Ghi nhận và phân loại mọi khoản thu chi"
                )
                Spacer(modifier = Modifier.height(12.dp))
                FeatureItem(
                    icon = Icons.Default.CreditCard,
                    iconBgColor = Color(0xFFF97316),
                    title = "Thiết lập ngân sách",
                    description = "Cảnh báo khi chi tiêu vượt giới hạn"
                )

                Spacer(modifier = Modifier.weight(1f))

                // --- Nút Back và Next ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onBackClick) {
                        Text("Back", color = Color.Gray, fontSize = 16.sp)
                    }
                    Button(
                        onClick = onNextClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Next →", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureItem(icon: ImageVector, iconBgColor: Color, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBgColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                Text(description, fontSize = 12.sp, color = Color(0xFF64748B))
            }
        }
    }
}