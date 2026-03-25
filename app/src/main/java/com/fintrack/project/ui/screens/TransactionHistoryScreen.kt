package com.fintrack.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(onBackClick: () -> Unit) {
    // Trạng thái bật/tắt Lịch
    var showDatePicker by remember { mutableStateOf(false) }

    // Quản lý trạng thái của Lịch chọn khoảng ngày
    val dateRangePickerState = rememberDateRangePickerState()

    // Format ngày hiển thị
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val startDateText = dateRangePickerState.selectedStartDateMillis?.let { formatter.format(Date(it)) } ?: "Từ ngày"
    val endDateText = dateRangePickerState.selectedEndDateMillis?.let { formatter.format(Date(it)) } ?: "Đến ngày"

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(bottom = paddingValues.calculateBottomPadding())) {

            // --- HEADER XANH ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF1A3FBF), Color(0xFF3B82F6))))
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp).padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp).size(36.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.ChevronLeft, "Quay lại", tint = Color.White)
                    }
                    Text("Lịch sử giao dịch", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            // --- NỘI DUNG CHÍNH ---
            Column(modifier = Modifier.fillMaxSize().padding(top = 110.dp, start = 20.dp, end = 20.dp)) {

                // --- THẺ TRUY VẤN ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Truy vấn giao dịch", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(16.dp))

                        // 2 Ô chọn ngày
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DateBox(text = startDateText, modifier = Modifier.weight(1f)) { showDatePicker = true }
                            DateBox(text = endDateText, modifier = Modifier.weight(1f)) { showDatePicker = true }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 2 Nút bấm
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { /* TODO: Xử lý truy vấn */ },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3FBF)),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Truy vấn", fontSize = 14.sp) }

                            Button(
                                onClick = { /* TODO: Chuyển sang màn thêm giao dịch */ },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E5BFF)),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Nhập giao dịch", fontSize = 14.sp) }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Hệ thống hỗ trợ truy vấn lịch sử giao dịch trong vòng 1 năm kể từ ngày hiện tại",
                            fontSize = 11.sp, color = Color(0xFF94A3B8), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- TRẠNG THÁI TRỐNG ---
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(modifier = Modifier.size(80.dp).background(Color(0xFFE2E8F0), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ReceiptLong, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(40.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Lịch sử giao dịch trống", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Chưa có giao dịch nào được ghi nhận\ntrong khoảng thời gian này.", fontSize = 13.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
                }
            }
        }

        // --- BỘ CHỌN LỊCH HIỂN THỊ KHI BẤM ---
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Xác nhận", fontWeight = FontWeight.Bold, color = Color(0xFF2E5BFF)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Hủy", color = Color.Gray) }
                }
            ) {
                DateRangePicker(
                    state = dateRangePickerState,
                    modifier = Modifier.weight(1f), // Tránh bị quá khổ màn hình
                    title = { Text(text = "Chọn khoảng thời gian", modifier = Modifier.padding(16.dp)) },
                    headline = {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(text = startDateText, fontWeight = FontWeight.Bold)
                            Text(text = "  -  ")
                            Text(text = endDateText, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = DatePickerDefaults.colors(
                        selectedDayContainerColor = Color(0xFF2E5BFF),
                        selectedDayContentColor = Color.White,
                        dayInSelectionRangeContainerColor = Color(0xFFE0E7FF),
                        dayInSelectionRangeContentColor = Color(0xFF1E293B)
                    )
                )
            }
        }
    }
}

// UI Ô chứa ngày tháng
@Composable
fun DateBox(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(56.dp)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(if (text.contains("/")) "Ngày" else "Chọn", fontSize = 10.sp, color = Color(0xFF94A3B8))
                Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            }
            Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF2E5BFF), modifier = Modifier.size(20.dp))
        }
    }
}