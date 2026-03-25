package com.fintrack.project.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.project.data.database.FinTrackDatabase
import com.fintrack.project.data.model.Notification
import com.fintrack.project.data.model.Transaction
import com.fintrack.project.data.model.TransactionType
import com.fintrack.project.data.model.NotificationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import kotlin.math.max

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // States
    var isIncome by remember { mutableStateOf(true) } // true = Thu, false = Chi
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Ngày tháng
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    val formatter = SimpleDateFormat("dd 'Tháng' MM, yyyy", Locale.getDefault())

    // Danh mục
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedCategoryName by remember { mutableStateOf("") }

    // Màn hình thành công
    var showSuccessScreen by remember { mutableStateOf(false) }
    var newBalance by remember { mutableDoubleStateOf(0.0) }

    val mainColor = if (isIncome) Color(0xFF10B981) else Color(0xFFEF4444)

    // Tạm thời hardcode danh mục
    var incomeCategories by remember { mutableStateOf<List<com.fintrack.project.data.model.Category>>(emptyList()) }
    var expenseCategories by remember { mutableStateOf<List<com.fintrack.project.data.model.Category>>(emptyList()) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val prefs = context.getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)
            val userId = prefs.getInt("LOGGED_IN_USER_ID", -1)
            if (userId != -1) {
                val db = FinTrackDatabase.getInstance(context)
                incomeCategories = db.categoryDao().getCategoriesByType(userId, com.fintrack.project.data.model.CategoryType.INCOME)
                expenseCategories = db.categoryDao().getCategoriesByType(userId, com.fintrack.project.data.model.CategoryType.EXPENSE)
            }
        }
    }
    val currentCategories = if (isIncome) incomeCategories else expenseCategories

    // HÀM LƯU DỮ LIỆU
    fun saveTransaction() {
        val amount = amountText.toDoubleOrNull() ?: 0.0
        if (amount <= 0 || selectedCategoryId == null) return

        scope.launch {
            withContext(Dispatchers.IO) {
                val prefs = context.getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)
                val userId = prefs.getInt("LOGGED_IN_USER_ID", -1)

                if (userId != -1) {
                    val db = FinTrackDatabase.getInstance(context)
                    val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE

                    // Lưu vào bảng transactions
                    db.transactionDao().insertTransaction(
                        Transaction(
                            userId = userId,
                            amount = amount,
                            type = type,
                            categoryId = selectedCategoryId!!,
                            transactionDate = selectedDateMillis,
                            description = description.ifEmpty { selectedCategoryName }
                        )
                    )

                    // Lưu vào bảng notifications
                    val typeStr = if (isIncome) "khoản thu" else "khoản chi"
                    db.notificationDao().insertNotification(
                        Notification(
                            userId = userId,
                            title = "Giao dịch mới \uD83D\uDCB0",
                            description = "Ghi nhận $typeStr mới", // Truyền thêm description
                            message = "Bạn vừa thêm $typeStr ${formatCurrency(amount)} vào danh mục $selectedCategoryName.",
                            type = NotificationType.TRANSACTION,   // Truyền thêm type
                            createdAt = System.currentTimeMillis(),
                            isRead = false
                        )
                    )

                    // Tính lại số dư mới nhất
                    val totalInc = db.transactionDao().getTotalAmount(userId, TransactionType.INCOME) ?: 0.0
                    val totalExp = db.transactionDao().getTotalAmount(userId, TransactionType.EXPENSE) ?: 0.0
                    newBalance = totalInc - totalExp
                }
            }
            showSuccessScreen = true
        }
    }

    if (showSuccessScreen) {
        // --- MÀN HÌNH THÀNH CÔNG ---
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.45f).background(mainColor))
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(80.dp))
                Box(modifier = Modifier.size(90.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Check, null, tint = mainColor, modifier = Modifier.size(48.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Lưu thành công!", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(if (isIncome) "Khoản thu đã được ghi lại" else "Khoản chi đã được ghi lại", color = Color.White.copy(alpha = 0.8f))

                Spacer(modifier = Modifier.height(40.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("CHI TIẾT GIAO DỊCH", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("#TXN-${System.currentTimeMillis().toString().takeLast(6)}", color = Color.Gray, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailRow("Số tiền", (if (isIncome) "+" else "-") + formatCurrency(amountText.toDoubleOrNull()?:0.0), mainColor, true)
                        DetailRow("Danh mục", selectedCategoryName, Color.Black)
                        DetailRow("Ngày giao dịch", formatter.format(Date(selectedDateMillis)), Color.Black)
                        DetailRow("Mô tả", description.ifEmpty { selectedCategoryName }, Color.Black)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))
                        DetailRow("Số dư mới", formatCurrency(newBalance), Color(0xFF2E5BFF), true)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onHomeClick, modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = mainColor), shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Home, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Về trang chủ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = {
                    amountText = ""; description = ""; selectedCategoryId = null; showSuccessScreen = false
                }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Icon(Icons.Default.Add, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Thêm giao dịch khác", color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        // --- MÀN HÌNH NHẬP GIAO DỊCH ---
        Scaffold(
            bottomBar = { AddTransactionBottomBar(onHomeClick = onHomeClick) },
            containerColor = Color(0xFFF8FAFC)
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(bottom = paddingValues.calculateBottomPadding()).verticalScroll(rememberScrollState())) {
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)).background(Color(0xFF2E5BFF))) {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart).size(36.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)) {
                                Icon(Icons.Default.ChevronLeft, null, tint = Color.White)
                            }
                            Text("Thêm Giao Dịch", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)).padding(4.dp)) {
                            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (isIncome) Color(0xFF10B981) else Color.Transparent).clickable { isIncome = true; selectedCategoryId = null }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                Text("Khoản Thu", color = if (isIncome) Color.White else Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                            }
                            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (!isIncome) Color(0xFFEF4444) else Color.Transparent).clickable { isIncome = false; selectedCategoryId = null }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                Text("Khoản Chi", color = if (!isIncome) Color.White else Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(if (isIncome) "SỐ TIỀN NHẬN ĐƯỢC" else "SỐ TIỀN CHI TIÊU", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (isIncome) "$" else "- $", color = mainColor, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                                TextField(
                                    value = amountText,
                                    onValueChange = { amountText = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = mainColor),
                                    textStyle = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = mainColor),
                                    placeholder = { Text("0", fontSize = 32.sp, color = Color.LightGray) },
                                    singleLine = true
                                )
                            }
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }, verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(40.dp).background(Color(0xFFEFF6FF), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.CalendarToday, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("NGÀY GIAO DỊCH", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(formatter.format(Date(selectedDateMillis)), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(40.dp).background(Color(0xFFF3E8FF), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.Edit, null, tint = Color(0xFFA855F7), modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("MÔ TẢ", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    TextField(
                                        value = description,
                                        onValueChange = { description = it },
                                        placeholder = { Text("Nhập mô tả giao dịch...", fontSize = 14.sp, color = Color.Gray) },
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent
                                        )
                                        // Đã xóa dòng contentPadding đi
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Danh mục", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(12.dp))

                    CustomFlowRow(modifier = Modifier.fillMaxWidth(), spacing = 8.dp) {
                        currentCategories.forEach { category ->
                            val isSelected = selectedCategoryId == category.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, if (isSelected) mainColor else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                    .background(if (isSelected) mainColor.copy(alpha = 0.1f) else Color.White)
                                    .clickable { selectedCategoryId = category.id; selectedCategoryName = category.name }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(getIconForCategory(category.name), null, tint = if (isSelected) mainColor else Color.Gray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(category.name, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) mainColor else Color.DarkGray)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { saveTransaction() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = mainColor),
                        shape = RoundedCornerShape(16.dp),
                        enabled = amountText.isNotEmpty() && selectedCategoryId != null
                    ) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isIncome) "Lưu Khoản Thu" else "Lưu Khoản Chi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Hủy") } }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }
    }
}

data class CategoryItem(val id: Int, val name: String, val icon: ImageVector)

@Composable
fun DetailRow(label: String, value: String, valueColor: Color, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 13.sp)
        Text(value, color = valueColor, fontSize = 14.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun AddTransactionBottomBar(onHomeClick: () -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        NavigationBarItem(icon = { Icon(Icons.Default.Home, null) }, label = { Text("Trang chủ", fontSize = 10.sp) }, selected = false, onClick = onHomeClick)
        NavigationBarItem(icon = { Icon(Icons.Default.BarChart, null) }, label = { Text("Thống kê", fontSize = 10.sp) }, selected = false, onClick = { })
        NavigationBarItem(
            icon = { Box(modifier = Modifier.size(40.dp).background(Color(0xFF2E5BFF), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, null, tint = Color.White) } },
            label = { Text("Thêm", fontSize = 10.sp) }, selected = true, onClick = { }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF2E5BFF), selectedTextColor = Color(0xFF2E5BFF), indicatorColor = Color(0xFFE0E7FF))
        )
        NavigationBarItem(icon = { Icon(Icons.Default.PieChart, null) }, label = { Text("Ngân sách", fontSize = 10.sp) }, selected = false, onClick = { })
        NavigationBarItem(icon = { Icon(Icons.Default.Person, null) }, label = { Text("Cá nhân", fontSize = 10.sp) }, selected = false, onClick = { })
    }
}
@Composable
fun CustomFlowRow(
    modifier: Modifier = Modifier,
    spacing: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val horizontalSpacing = spacing.roundToPx()
        val verticalSpacing = spacing.roundToPx()

        var currentRowWidth = 0
        var currentRowHeight = 0
        var totalHeight = 0

        val placeables = measurables.map { measurable ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))

            if (currentRowWidth + placeable.width > constraints.maxWidth) {
                // Xuống dòng mới
                totalHeight += currentRowHeight + verticalSpacing
                currentRowWidth = 0
                currentRowHeight = 0
            }

            currentRowWidth += placeable.width + horizontalSpacing
            currentRowHeight = max(currentRowHeight, placeable.height)

            placeable
        }

        totalHeight += currentRowHeight

        layout(constraints.maxWidth, totalHeight) {
            var xPosition = 0
            var yPosition = 0
            var rowHeight = 0

            placeables.forEach { placeable ->
                if (xPosition + placeable.width > constraints.maxWidth) {
                    xPosition = 0
                    yPosition += rowHeight + verticalSpacing
                    rowHeight = 0
                }

                placeable.placeRelative(xPosition, yPosition)

                xPosition += placeable.width + horizontalSpacing
                rowHeight = max(rowHeight, placeable.height)
            }
        }
    }
}
fun getIconForCategory(name: String): ImageVector {
    return when (name) {
        "Ăn uống" -> Icons.Default.Fastfood
        "Giáo dục" -> Icons.Default.School
        "Y tế" -> Icons.Default.LocalHospital // Cần import Icons.Default.LocalHospital
        "Giải trí" -> Icons.Default.Movie
        "Giao thông" -> Icons.Default.DirectionsCar
        "Utilities", "Nhà ở" -> Icons.Default.Home
        "Mua sắm" -> Icons.Default.ShoppingCart
        "Lương" -> Icons.Default.AttachMoney
        "Thưởng" -> Icons.Default.CardGiftcard
        "Kinh doanh" -> Icons.Default.Store
        "Đầu tư" -> Icons.Default.TrendingUp
        else -> Icons.Default.MoreHoriz
    }
}