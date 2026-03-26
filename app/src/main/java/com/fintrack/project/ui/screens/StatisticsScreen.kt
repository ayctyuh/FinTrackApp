package com.fintrack.project.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.project.data.database.FinTrackDatabase
import com.fintrack.project.data.model.Transaction
import com.fintrack.project.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

enum class ChartType(val title: String, val subtitle: String, val icon: ImageVector, val color: Color) {
    OVERVIEW("Tổng hợp", "Thu & chi tổng quan", Icons.Default.BarChart, Color(0xFF2E5BFF)),
    TREND("Xu hướng", "Thu & chi theo xu hướng thời gian", Icons.Default.ShowChart, Color(0xFF0EA5E9)),
    CATEGORY("Danh mục", "Chi tiêu theo từng danh mục", Icons.Default.Hexagon, Color(0xFFA855F7))
}

enum class TimeFilter(val label: String) {
    WEEK("Tuần"), MONTH("Tháng"), YEAR("Năm")
}

data class ChartDataPoint(val label: String, val income: Float, val expense: Float)
data class CategoryDataPoint(val categoryName: String, val expense: Float)

@Composable
fun StatisticsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onAddClick: () -> Unit
) {
    val context = LocalContext.current
    var currentUserId by remember { mutableIntStateOf(-1) }

    var selectedChart by remember { mutableStateOf(ChartType.OVERVIEW) }
    var selectedTime by remember { mutableStateOf(TimeFilter.MONTH) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    var totalBalance by remember { mutableDoubleStateOf(0.0) }
    var periodIncome by remember { mutableDoubleStateOf(0.0) }
    var periodExpense by remember { mutableDoubleStateOf(0.0) }
    var chartData by remember { mutableStateOf<List<ChartDataPoint>>(emptyList()) }
    var categoryData by remember { mutableStateOf<List<CategoryDataPoint>>(emptyList()) }

    var dateLabel by remember { mutableStateOf("") }
    var categoriesMap by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }

    suspend fun loadStatisticsData(userId: Int, timeFilter: TimeFilter) {
        val db = FinTrackDatabase.getInstance(context)
        val transactionDao = db.transactionDao()

        categoriesMap = db.categoryDao().getUserCategories(userId).associate { it.id to it.name }

        val allInc = transactionDao.getTotalAmount(userId, TransactionType.INCOME) ?: 0.0
        val allExp = transactionDao.getTotalAmount(userId, TransactionType.EXPENSE) ?: 0.0
        totalBalance = allInc - allExp

        val cal = Calendar.getInstance(Locale("vi", "VN"))
        cal.firstDayOfWeek = Calendar.MONDAY

        val startTime: Long
        val endTime: Long

        when (timeFilter) {
            TimeFilter.WEEK -> {
                val today = cal.get(Calendar.DAY_OF_WEEK)
                val diffToMonday = if (today == Calendar.SUNDAY) -6 else Calendar.MONDAY - today
                cal.add(Calendar.DAY_OF_MONTH, diffToMonday)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
                startTime = cal.timeInMillis

                cal.add(Calendar.DAY_OF_MONTH, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
                endTime = cal.timeInMillis

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
                dateLabel = "Tuần này: ${sdf.format(Date(startTime))} - ${sdf.format(Date(endTime))}"
            }
            TimeFilter.MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
                startTime = cal.timeInMillis

                cal.add(Calendar.MONTH, 1); cal.add(Calendar.MILLISECOND, -1)
                endTime = cal.timeInMillis

                val sdf = SimpleDateFormat("'Tháng' MM - yyyy", Locale("vi", "VN"))
                dateLabel = sdf.format(Date(startTime))
            }
            TimeFilter.YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
                startTime = cal.timeInMillis

                cal.add(Calendar.YEAR, 1); cal.add(Calendar.MILLISECOND, -1)
                endTime = cal.timeInMillis

                val sdf = SimpleDateFormat("'Năm' yyyy", Locale("vi", "VN"))
                dateLabel = sdf.format(Date(startTime))
            }
        }

        periodIncome = transactionDao.getTotalAmountByDateRange(userId, TransactionType.INCOME, startTime, endTime) ?: 0.0
        periodExpense = transactionDao.getTotalAmountByDateRange(userId, TransactionType.EXPENSE, startTime, endTime) ?: 0.0

        val transactions = transactionDao.getTransactionsByDateRange(userId, startTime, endTime)

        val groupedData = mutableListOf<ChartDataPoint>()
        val tempCal = Calendar.getInstance(Locale("vi", "VN"))
        tempCal.firstDayOfWeek = Calendar.MONDAY

        when (timeFilter) {
            TimeFilter.WEEK -> {
                val days = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                val dayValues = Array(7) { floatArrayOf(0f, 0f) }

                transactions.forEach { txn ->
                    tempCal.timeInMillis = txn.transactionDate
                    val dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)
                    val index = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
                    if (index in 0..6) {
                        if (txn.type == TransactionType.INCOME) dayValues[index][0] += txn.amount.toFloat()
                        else dayValues[index][1] += txn.amount.toFloat()
                    }
                }
                days.forEachIndexed { i, label -> groupedData.add(ChartDataPoint(label, dayValues[i][0], dayValues[i][1])) }
            }
            TimeFilter.MONTH -> {
                val weeks = listOf("Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4", "Tuần 5")
                val weekValues = Array(5) { floatArrayOf(0f, 0f) }

                transactions.forEach { txn ->
                    tempCal.timeInMillis = txn.transactionDate
                    val weekOfMonth = tempCal.get(Calendar.WEEK_OF_MONTH) - 1
                    val index = weekOfMonth.coerceIn(0, 4)
                    if (txn.type == TransactionType.INCOME) weekValues[index][0] += txn.amount.toFloat()
                    else weekValues[index][1] += txn.amount.toFloat()
                }
                weeks.forEachIndexed { i, label -> groupedData.add(ChartDataPoint(label, weekValues[i][0], weekValues[i][1])) }
            }
            TimeFilter.YEAR -> {
                val months = listOf("Th 1", "Th 2", "Th 3", "Th 4", "Th 5", "Th 6", "Th 7", "Th 8", "Th 9", "Th 10", "Th 11", "Th 12")
                val monthValues = Array(12) { floatArrayOf(0f, 0f) }

                transactions.forEach { txn ->
                    tempCal.timeInMillis = txn.transactionDate
                    val monthOfYear = tempCal.get(Calendar.MONTH)
                    if (monthOfYear in 0..11) {
                        if (txn.type == TransactionType.INCOME) monthValues[monthOfYear][0] += txn.amount.toFloat()
                        else monthValues[monthOfYear][1] += txn.amount.toFloat()
                    }
                }
                months.forEachIndexed { i, label -> groupedData.add(ChartDataPoint(label, monthValues[i][0], monthValues[i][1])) }
            }
        }
        chartData = groupedData

        val categoryGroups = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId }
            .map { (catId, txns) ->
                val catName = categoriesMap[catId ?: -1] ?: "Khác"
                val sum = txns.sumOf { it.amount }.toFloat()
                CategoryDataPoint(catName, sum)
            }
            .sortedByDescending { it.expense }

        categoryData = categoryGroups
    }

    LaunchedEffect(selectedTime) {
        withContext(Dispatchers.IO) {
            val prefs = context.getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)
            currentUserId = prefs.getInt("LOGGED_IN_USER_ID", -1)
            if (currentUserId != -1) {
                loadStatisticsData(currentUserId, selectedTime)
            }
        }
    }

    val difference = periodIncome - periodExpense

    Scaffold(
        bottomBar = {
            ProfileBottomNavigationBar(
                onHomeClick = onNavigateToHome,
                onAddClick = onAddClick,
                currentScreen = "Thống kê",
                onProfileClick = onNavigateToProfile,
                onStatisticsClick = {}
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF1A3FBF), Color(0xFF3B82F6))))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Thống kê", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountBalanceWallet, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Số dư hiện tại", color = Color.Gray, fontSize = 12.sp)
                                }
                                Text(formatCurrency(totalBalance), color = Color(0xFF10B981), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color(0xFFE2E8F0)))
                            Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MoneyOff, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Chi tiêu ${selectedTime.label.lowercase()}", color = Color.Gray, fontSize = 12.sp)
                                }
                                Text(formatCurrency(periodExpense), color = Color(0xFFEF4444), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { isDropdownExpanded = !isDropdownExpanded },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).background(selectedChart.color.copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                            Icon(selectedChart.icon, null, tint = selectedChart.color)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(selectedChart.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                            Text(selectedChart.subtitle, fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                        Icon(if (isDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = Color.Gray)
                    }
                }

                AnimatedVisibility(
                    visible = isDropdownExpanded,
                    enter = expandVertically(animationSpec = tween(300)),
                    exit = shrinkVertically(animationSpec = tween(300))
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("CHỌN LOẠI BIỂU ĐỒ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp))
                            ChartType.values().forEach { chart ->
                                val isSelected = chart == selectedChart
                                Row(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { selectedChart = chart; isDropdownExpanded = false }.background(if (isSelected) Color(0xFFEFF6FF) else Color.Transparent).padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(40.dp).background(if (isSelected) chart.color else chart.color.copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                                        Icon(chart.icon, null, tint = if (isSelected) Color.White else chart.color)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(chart.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                                        Text(chart.subtitle, fontSize = 12.sp, color = Color(0xFF94A3B8))
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E5BFF))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFE2E8F0), RoundedCornerShape(12.dp)).padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TimeFilter.values().forEach { filter ->
                        val isSelected = selectedTime == filter
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSelected) Color(0xFF2E5BFF) else Color.Transparent).clickable { selectedTime = filter }.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(filter.label, color = if (isSelected) Color.White else Color(0xFF64748B), fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(
                                    when (selectedChart) {
                                        ChartType.OVERVIEW -> "Thu nhập & Chi tiêu"
                                        ChartType.TREND -> "Thu nhập & Chi tiêu"
                                        ChartType.CATEGORY -> "Chi tiêu theo danh mục"
                                    },
                                    fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B)
                                )
                                Text(dateLabel, fontSize = 12.sp, color = Color.Gray)
                            }

                            // Chú thích (Legend)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (selectedChart != ChartType.CATEGORY) {
                                    // Legend cho Tổng hợp / Xu hướng (Thu/Chi)
                                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF10B981), CircleShape)); Spacer(modifier = Modifier.width(4.dp))
                                    Text("Thu", fontSize = 10.sp, color = Color.Gray); Spacer(modifier = Modifier.width(12.dp))
                                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFEF4444), CircleShape)); Spacer(modifier = Modifier.width(4.dp))
                                    Text("Chi", fontSize = 10.sp, color = Color.Gray)
                                } else if (categoryData.isNotEmpty()) {
                                    // Chú thích màu sắc cho biểu đồ danh mục (Dựa theo mức độ cao thấp tự động)
                                    // Chú thích này nằm góc phải trên biểu đồ
                                    @Composable
                                    fun LegendDot(color: Color, label: String) {
                                        Box(modifier = Modifier.size(7.dp).background(color, CircleShape))
                                        Spacer(Modifier.width(3.dp))
                                        Text(label, fontSize = 9.sp, color = Color.Gray)
                                        Spacer(Modifier.width(8.dp))
                                    }
                                    LegendDot(Color(0xFFEF4444), ">80%")
                                    LegendDot(Color(0xFFF97316), ">50%")
                                    LegendDot(Color(0xFFEAB308), ">30%")
                                    LegendDot(Color(0xFF22C55E), "<30%")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                            when (selectedChart) {
                                ChartType.OVERVIEW -> OverviewBarChart(chartData)
                                ChartType.TREND -> TrendLineChart(chartData)
                                ChartType.CATEGORY -> CategoryBarChart(categoryData)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SummaryCard(title = "Thu nhập", amount = periodIncome, icon = Icons.Default.TrendingUp, color = Color(0xFF10B981), modifier = Modifier.weight(1f))
                    SummaryCard(title = "Chi tiêu", amount = periodExpense, icon = Icons.Default.TrendingDown, color = Color(0xFFEF4444), modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (difference >= 0) Color(0xFF2E5BFF) else Color(0xFFEF4444)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Chênh lệch ${selectedTime.label.lowercase()} này", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Text((if (difference >= 0) "+" else "-") + formatCurrency(Math.abs(difference)), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ==========================================
// CÁC HÀM VẼ BIỂU ĐỒ VỚI DỮ LIỆU THẬT
// ==========================================

@Composable
fun OverviewBarChart(data: List<ChartDataPoint>) {
    if (data.isEmpty() || data.all { it.income == 0f && it.expense == 0f }) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Chưa có dữ liệu", color = Color.Gray, fontSize = 12.sp)
        }
        return
    }

    val maxVal = data.maxOf { max(it.income, it.expense) }.coerceAtLeast(1f)
    val barWidth = if (data.size > 7) 8.dp else 12.dp

    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        data.forEach { point ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                    val incHeight = (point.income / maxVal)
                    Box(modifier = Modifier.width(barWidth).fillMaxHeight(incHeight).background(Color(0xFF10B981), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))

                    val expHeight = (point.expense / maxVal)
                    Box(modifier = Modifier.width(barWidth).fillMaxHeight(expHeight).background(Color(0xFFEF4444), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(point.label, fontSize = 9.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun TrendLineChart(data: List<ChartDataPoint>) {
    if (data.isEmpty() || data.all { it.income == 0f && it.expense == 0f }) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Chưa có dữ liệu", color = Color.Gray, fontSize = 12.sp)
        }
        return
    }

    val maxVal = data.maxOf { max(it.income, it.expense) }.coerceAtLeast(1f)

    Column(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 10.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)) {
            val width = size.width
            val height = size.height
            val stepX = if (data.size > 1) width / (data.size - 1) else width

            fun drawLineChart(points: List<Float>, color: Color) {
                val path = Path()
                points.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = height - (value / maxVal * height)
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(path = path, color = color, style = Stroke(width = 4f))

                points.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = height - (value / maxVal * height)
                    drawCircle(color = Color.White, radius = 8f, center = Offset(x, y))
                    drawCircle(color = color, radius = 5f, center = Offset(x, y), style = Stroke(width = 3f))
                }
            }

            drawLineChart(data.map { it.income }, Color(0xFF10B981))
            drawLineChart(data.map { it.expense }, Color(0xFFEF4444))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            data.forEach { point ->
                Text(point.label, fontSize = 9.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun CategoryBarChart(data: List<CategoryDataPoint>) {
    if (data.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Chưa có dữ liệu chi tiêu", color = Color.Gray, fontSize = 12.sp)
        }
        return
    }

    // Xác định giá trị cao nhất để tính phần trăm
    val maxVal = data.maxOf { it.expense }.coerceAtLeast(1f)
    // Chỉ lấy Top 6 danh mục cao nhất
    val displayData = data.take(6)

    // SỬA LỖI LAYOUT BẰNG COLUMN BỌC NGOÀI
    Column(modifier = Modifier.fillMaxSize()) {
        // PHẦN 1: ROW CHỨA CÁC CỘT (Chiếm phần lớn chiều cao)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Chiếm không gian còn lại (khoảng 80%)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom // Cột mọc từ dưới lên
        ) {
            displayData.forEach { point ->
                val value = point.expense
                // Logic màu sắc cảnh báo tự động
                val barColor = when {
                    value >= maxVal * 0.8f -> Color(0xFFEF4444) // Đỏ (Cao nhất)
                    value >= maxVal * 0.5f -> Color(0xFFF97316) // Cam
                    value >= maxVal * 0.3f -> Color(0xFFEAB308) // Vàng
                    else -> Color(0xFF22C55E) // Xanh lá
                }

                // Cột
                val barHeight = (value / maxVal).coerceIn(0.01f, 1f) // Giới hạn chiều cao tối thiểu để luôn thấy cột
                Box(
                    modifier = Modifier
                        .width(18.dp) // Tăng nhẹ độ rộng cột
                        .fillMaxHeight(barHeight) // Chiều cao dựa theo %
                        .background(barColor, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                )
            }
        }

        // KHOẢNG CÁCH CỐ ĐỊNH GIỮA CỘT VÀ TÊN
        Spacer(modifier = Modifier.height(10.dp))

        // PHẦN 2: ROW CHỨA TÊN DANH MỤC (Cố định chiều cao phía dưới)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp), // Chiều cao cố định cho phần chữ
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top // Chữ nằm đỉnh của vùng này
        ) {
            displayData.forEach { point ->
                // Rút gọn tên nếu quá dài
                val shortName = if (point.categoryName.length > 8) point.categoryName.take(6) + ".." else point.categoryName
                Text(
                    text = shortName,
                    fontSize = 9.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.width(42.dp) // Chiều cao chữ đủ rộng để ko bị lún
                )
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, amount: Double, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                }
                Text(title, fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(12.dp))
            val amountStr = if (amount % 1.0 == 0.0) String.format("%,.0f", amount) else String.format("%,.0f", amount)
            Text("${amountStr}đ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
