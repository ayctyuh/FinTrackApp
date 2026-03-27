package com.fintrack.project.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.project.data.database.FinTrackDatabase
import com.fintrack.project.data.model.Transaction
import com.fintrack.project.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    onBackClick: () -> Unit,
    onAddTransactionClick: () -> Unit = {} // Vẫn giữ tham số để tránh lỗi ở MainActivity nhưng không dùng
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showDatePicker by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()

    var displayTransactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var categoriesMap by remember { mutableStateOf<Map<Int, com.fintrack.project.data.model.Category>>(emptyMap()) }
    var currentUserId by remember { mutableIntStateOf(-1) }
    var currentBalance by remember { mutableDoubleStateOf(0.0) }
    var selectedTxn by remember { mutableStateOf<Transaction?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    val timeFormatter = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale("vi", "VN"))
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val startDateText = dateRangePickerState.selectedStartDateMillis?.let { formatter.format(Date(it)) } ?: "Từ ngày"
    val endDateText = dateRangePickerState.selectedEndDateMillis?.let { formatter.format(Date(it)) } ?: "Đến ngày"

    suspend fun loadTransactions(startMillis: Long? = null, endMillis: Long? = null) {
        if (currentUserId != -1) {
            val db = FinTrackDatabase.getInstance(context)
            if (startMillis != null && endMillis != null) {
                val adjustedEndMillis = endMillis + (24 * 60 * 60 * 1000) - 1
                displayTransactions = db.transactionDao().getTransactionsByDateRange(currentUserId, startMillis, adjustedEndMillis)
            } else {
                displayTransactions = db.transactionDao().getUserTransactions(currentUserId)
            }
        }
    }

    LaunchedEffect(refreshTrigger) {
        withContext(Dispatchers.IO) {
            val prefs = context.getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)
            currentUserId = prefs.getInt("LOGGED_IN_USER_ID", -1)

            if (currentUserId != -1) {
                val db = FinTrackDatabase.getInstance(context)
                categoriesMap = db.categoryDao().getUserCategories(currentUserId).associateBy { it.id }
                loadTransactions()
                val totalInc = db.transactionDao().getTotalAmount(currentUserId, TransactionType.INCOME) ?: 0.0
                val totalExp = db.transactionDao().getTotalAmount(currentUserId, TransactionType.EXPENSE) ?: 0.0
                currentBalance = totalInc - totalExp
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(bottom = paddingValues.calculateBottomPadding())) {

            // Header Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF1A3FBF), Color(0xFF3B82F6))))
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp).padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp).size(36.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)) {
                        Icon(Icons.Default.ChevronLeft, "Quay lại", tint = Color.White)
                    }
                    Text("Lịch sử giao dịch", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Main Content
            Column(modifier = Modifier.fillMaxSize().padding(top = 110.dp, start = 20.dp, end = 20.dp)) {

                // Box Truy Vấn
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Truy vấn giao dịch", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(16.dp))

                        // Hai ô chọn ngày
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DateBox(text = startDateText, modifier = Modifier.weight(1f)) { showDatePicker = true }
                            DateBox(text = endDateText, modifier = Modifier.weight(1f)) { showDatePicker = true }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Cụm Nút (Đã xóa nhập thủ công, đưa từ thông báo lên)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    val sMillis = dateRangePickerState.selectedStartDateMillis
                                    val eMillis = dateRangePickerState.selectedEndDateMillis
                                    if (sMillis != null && eMillis != null) {
                                        scope.launch(Dispatchers.IO) { loadTransactions(sMillis, eMillis) }
                                    } else {
                                        scope.launch(Dispatchers.IO) { loadTransactions() }
                                    }
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3FBF)),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Truy vấn", fontSize = 14.sp) }

                            // Nút Import từ thông báo (Sẽ chiếm nửa còn lại)
                            BankImportButton(
                                modifier = Modifier.weight(1f),
                                onImported = {
                                    refreshTrigger++ // Kích hoạt tải lại danh sách
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Hệ thống hỗ trợ truy vấn lịch sử giao dịch trong vòng 1 năm kể từ ngày hiện tại", fontSize = 11.sp, color = Color(0xFF94A3B8), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Danh sách Transaction
                if (displayTransactions.isEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth().weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Box(modifier = Modifier.size(80.dp).background(Color(0xFFE2E8F0), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.ReceiptLong, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(40.dp)) }
                        Spacer(modifier = Modifier.height(16.dp)); Text("Lịch sử giao dịch trống", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(8.dp)); Text("Chưa có giao dịch nào được ghi nhận\ntrong khoảng thời gian này.", fontSize = 13.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), contentPadding = PaddingValues(bottom = 24.dp)) {
                        items(displayTransactions) { txn ->
                            val isInc = txn.type == TransactionType.INCOME
                            val category = categoriesMap[txn.categoryId]
                            val categoryName = category?.name ?: "Khác"
                            val displayIcon = resolveCategoryIcon(category?.icon ?: category?.name)
                            val displayDescription = if (!txn.description.isNullOrEmpty()) txn.description else categoryName

                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { selectedTxn = txn },
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
            }
        }

        // Popup Chi Tiết
        selectedTxn?.let { txn ->
            TransactionDetailDialog(
                transaction = txn,
                categoryName = categoriesMap[txn.categoryId]?.name ?: "Khác",
                currentBalance = currentBalance,
                onDismiss = { selectedTxn = null }
            )
        }

        // Popup Date Picker
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("Xác nhận", fontWeight = FontWeight.Bold, color = Color(0xFF2E5BFF)) } },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Hủy", color = Color.Gray) } }
            ) {
                DateRangePicker(
                    state = dateRangePickerState, modifier = Modifier.weight(1f), title = { Text(text = "Chọn khoảng thời gian", modifier = Modifier.padding(16.dp)) },
                    headline = { Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) { Text(text = startDateText, fontWeight = FontWeight.Bold); Text(text = "  -  "); Text(text = endDateText, fontWeight = FontWeight.Bold) } },
                    colors = DatePickerDefaults.colors(selectedDayContainerColor = Color(0xFF2E5BFF), selectedDayContentColor = Color.White, dayInSelectionRangeContainerColor = Color(0xFFE0E7FF), dayInSelectionRangeContentColor = Color(0xFF1E293B))
                )
            }
        }
    }
}

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