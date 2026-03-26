package com.fintrack.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfServiceScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Điều khoản sử dụng", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ChevronLeft, "Quay lại") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cập nhật lần cuối: Tháng 3, 2026", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(24.dp))

            Text("1. Thu thập dữ liệu", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Ứng dụng FinTrack chỉ thu thập các dữ liệu tài chính do bạn tự nguyện nhập vào. Dữ liệu này được lưu trữ cục bộ trên thiết bị của bạn để đảm bảo quyền riêng tư tuyệt đối.", color = Color(0xFF64748B), fontSize = 14.sp, lineHeight = 22.sp)

            Spacer(modifier = Modifier.height(24.dp))

            Text("2. Bảo mật mã PIN", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Mã PIN của bạn được mã hóa và sử dụng làm lớp bảo vệ thứ 2. Trong trường hợp bạn quên mã PIN, bạn sẽ cần xác minh bằng mật khẩu tài khoản gốc.", color = Color(0xFF64748B), fontSize = 14.sp, lineHeight = 22.sp)

            Spacer(modifier = Modifier.height(24.dp))

            Text("3. Cam kết của chúng tôi", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Chúng tôi cam kết không chia sẻ, mua bán dữ liệu tài chính của bạn cho bất kỳ bên thứ 3 nào dưới mọi hình thức.", color = Color(0xFF64748B), fontSize = 14.sp, lineHeight = 22.sp)

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}