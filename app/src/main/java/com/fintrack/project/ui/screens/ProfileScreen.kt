package com.fintrack.project.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.project.data.database.FinTrackDatabase
import com.fintrack.project.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onAddClick: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToCategory: () -> Unit, // BỔ SUNG THAM SỐ NÀY
    onStatisticsClick: () -> Unit // BỔ SUNG THAM SỐ NÀY ĐỂ NAVBAR CÓ THỂ CHUYỂN TRANG
) {
    val context = LocalContext.current
    var user by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val sharedPreferences = context.getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getInt("LOGGED_IN_USER_ID", -1)
            user = FinTrackDatabase.getInstance(context).userDao().getUserById(userId)
        }
    }

    Scaffold(
        // GỌI NAVBAR VỚI ĐẦY ĐỦ THAM SỐ ĐỂ FIX LỖI "AMBIGUITY"
        bottomBar = {
            ProfileBottomNavigationBar(
                onHomeClick = onNavigateToHome,
                onAddClick = onAddClick,
                onProfileClick = {}, // Đang ở Profile rồi
                onStatisticsClick = onStatisticsClick, // TRUYỀN HÀM CHUYỂN TRANG VÀO ĐÂY
                currentScreen = "Cá nhân"
            )
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
            // --- HEADER XANH ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF1A3FBF), Color(0xFF3B82F6))))
            ) {
                Box(modifier = Modifier.size(160.dp).align(Alignment.TopEnd).offset(x = 40.dp, y = (-40).dp).background(Color.White.copy(alpha = 0.08f), CircleShape))
                Box(modifier = Modifier.size(100.dp).align(Alignment.BottomStart).offset(x = (-30).dp, y = 20.dp).background(Color.White.copy(alpha = 0.08f), CircleShape))

                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp, bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.Center) {
                        Text("Cá nhân", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier.size(90.dp).clip(CircleShape).border(3.dp, Color.White, CircleShape).background(Color(0xFF2E5BFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Person, null, tint = Color.White, modifier = Modifier.size(48.dp))
                        }
                        Box(
                            modifier = Modifier.size(28.dp).offset(x = (-4).dp, y = (-4).dp).background(Color.White, CircleShape).padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF2E5BFF), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(user?.username ?: "Đang tải...", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("ID: ${user?.id ?: "---"}", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                }
            }

            // --- DANH SÁCH MENU ---
            Column(modifier = Modifier.padding(24.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        ProfileMenuItemColor(Icons.Outlined.Person, Color(0xFF2E5BFF), "Chỉnh sửa hồ sơ", "Cập nhật thông tin cá nhân", onNavigateToEdit)
                        HorizontalDivider(modifier = Modifier.padding(start = 76.dp), color = Color(0xFFF1F5F9))
                        // GẮN SỰ KIỆN CHUYỂN SANG MÀN DANH MỤC
                        ProfileMenuItemColor(Icons.Outlined.Category, Color(0xFFEF4444), "Danh mục", "Thêm danh mục chi tiêu", onNavigateToCategory)
                        HorizontalDivider(modifier = Modifier.padding(start = 76.dp), color = Color(0xFFF1F5F9))
                        ProfileMenuItemColor(Icons.Outlined.Security, Color(0xFF10B981), "Bảo mật", "PIN, điều khoản", onNavigateToSecurity)
                        HorizontalDivider(modifier = Modifier.padding(start = 76.dp), color = Color(0xFFF1F5F9))
                        ProfileMenuItemColor(Icons.Outlined.Settings, Color(0xFFF59E0B), "Cài đặt", "Ngôn ngữ, thông báo, giao diện") { }
                        HorizontalDivider(modifier = Modifier.padding(start = 76.dp), color = Color(0xFFF1F5F9))
                        ProfileMenuItemColor(Icons.Outlined.HelpOutline, Color(0xFF8B5CF6), "Trợ giúp", "FAQ, liên hệ hỗ trợ") { }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onLogout() },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).background(Color(0xFFEF4444), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Logout, null, tint = Color.White)
                        }
                        Text("  Đăng xuất", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFEF4444))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text("FinTrack v2.4.1 - © 2026", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ProfileMenuItemColor(icon: ImageVector, iconBgColor: Color, title: String, sub: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(48.dp).background(iconBgColor, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White)
        }
        Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 15.sp)
            Text(sub, fontSize = 12.sp, color = Color(0xFF94A3B8))
        }
        Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFCBD5E1))
    }
}

// 3. ĐÃ XÓA ProfileBottomNavigationBar TẠI ĐÂY ĐỂ TRÁNH TRÙNG LẶP (CONFLICT) VÀ SỬ DỤNG BẢN CHUNG