package com.fintrack.project.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isIncome by remember { mutableStateOf(true) }
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    val formatter = SimpleDateFormat("dd 'Tháng' MM, yyyy", Locale.getDefault())

    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedCategoryName by remember { mutableStateOf("") }

    var showSuccessScreen by remember { mutableStateOf(false) }
    var newBalance by remember { mutableDoubleStateOf(0.0) }

    val mainColor = if (isIncome) Color(0xFF10B981) else Color(0xFFEF4444)

    var incomeCategories by remember { mutableStateOf<List<com.fintrack.project.data.model.Category>>(emptyList()) }
    var expenseCategories by remember { mutableStateOf<List<com.fintrack.project.data.model.Category>>(emptyList()) }

    var userPin by remember { mutableStateOf<String?>(null) }
    var showPinDialog by remember { mutableStateOf(false) }

    // --- STATE CHO OTP ---
    var showOtpDialog by remember { mutableStateOf(false) }
    var generatedOtp by remember { mutableStateOf("") }
    var userGoalKey by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val prefs = context.getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)
            val userId = prefs.getInt("LOGGED_IN_USER_ID", -1)
            if (userId != -1) {
                val db = FinTrackDatabase.getInstance(context)
                incomeCategories = db.categoryDao().getCategoriesByType(userId, com.fintrack.project.data.model.CategoryType.INCOME)
                expenseCategories = db.categoryDao().getCategoriesByType(userId, com.fintrack.project.data.model.CategoryType.EXPENSE)

                val user = db.userDao().getUserById(userId)
                userPin = user?.pinCode
                userGoalKey = "${userId}_${user?.username}"
            }
        }
    }
    val currentCategories = if (isIncome) incomeCategories else expenseCategories

    fun performSaveTransaction() {
        val amount = amountText.toDoubleOrNull() ?: 0.0
        if (amount <= 0 || selectedCategoryId == null) return

        scope.launch {
            withContext(Dispatchers.IO) {
                val prefs = context.getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)
                val userId = prefs.getInt("LOGGED_IN_USER_ID", -1)

                if (userId != -1) {
                    val db = FinTrackDatabase.getInstance(context)
                    val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE

                    val totalIncOld = db.transactionDao().getTotalAmount(userId, TransactionType.INCOME) ?: 0.0
                    val totalExpOld = db.transactionDao().getTotalAmount(userId, TransactionType.EXPENSE) ?: 0.0
                    val oldBalance = totalIncOld - totalExpOld

                    val newTransactionId = db.transactionDao().insertTransaction(
                        Transaction(userId = userId, amount = amount, type = type, categoryId = selectedCategoryId!!, transactionDate = selectedDateMillis, description = description.ifEmpty { selectedCategoryName })
                    )

                    val totalIncNew = db.transactionDao().getTotalAmount(userId, TransactionType.INCOME) ?: 0.0
                    val totalExpNew = db.transactionDao().getTotalAmount(userId, TransactionType.EXPENSE) ?: 0.0
                    newBalance = totalIncNew - totalExpNew

                    val typeStr = if (isIncome) "Khoản thu" else "Khoản chi"
                    db.notificationDao().insertNotification(
                        Notification(
                            userId = userId,
                            transactionId = newTransactionId.toInt(),
                            title = selectedCategoryName,
                            description = if (isIncome) "Bạn vừa có thêm 1 giao dịch" else description.ifEmpty { "Ghi nhận khoản chi mới" },
                            message = "Bạn vừa thêm $typeStr ${com.fintrack.project.utils.CurrencyUtils.formatMoney(amount)} vào danh mục $selectedCategoryName.",
                            type = NotificationType.TRANSACTION,
                            createdAt = System.currentTimeMillis(),
                            isRead = false
                        )
                    )

                    val goalAmount = prefs.getFloat("GOAL_AMOUNT_$userGoalKey", 0f).toDouble()
                    val goalName = prefs.getString("GOAL_NAME_$userGoalKey", "") ?: ""

                    var reached100Percent = false

                    if (goalAmount > 0) {
                        if (newBalance >= goalAmount && oldBalance < goalAmount) {
                            reached100Percent = true
                        }
                    }

                    // --- KIỂM TRA NGÂN SÁCH ---
                    if (!isIncome && selectedCategoryId != null) {
                        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                        val txnMonth = cal.get(Calendar.MONTH) + 1
                        val txnYear = cal.get(Calendar.YEAR)

                        val budgets = db.budgetDao().getBudgetsByMonth(userId, txnMonth, txnYear)
                        val budget = budgets.find { it.categoryId == selectedCategoryId }

                        if (budget != null && budget.limitAmount > 0) {
                            val limit = budget.limitAmount
                            val allTxns = db.transactionDao().getAllTransactionsByUser(userId)

                            val spentThisMonth = allTxns.filter {
                                it.type == TransactionType.EXPENSE &&
                                        it.categoryId == selectedCategoryId &&
                                        Calendar.getInstance().apply { timeInMillis = it.transactionDate }.get(Calendar.MONTH) + 1 == txnMonth &&
                                        Calendar.getInstance().apply { timeInMillis = it.transactionDate }.get(Calendar.YEAR) == txnYear
                            }.sumOf { it.amount }

                            val oldSpent = spentThisMonth - amount
                            val oldPct = oldSpent / limit
                            val newPct = spentThisMonth / limit

                            var alertTitle = ""
                            var alertMsg = ""

                            if (oldPct < 1.0 && newPct >= 1.0) {
                                alertTitle = "Vượt 100% ngân sách!"
                                alertMsg = "Danh mục '$selectedCategoryName' đã VƯỢT giới hạn chi tiêu của tháng $txnMonth/$txnYear."
                            } else if (oldPct < 0.75 && newPct >= 0.75) {
                                alertTitle = "Cảnh báo chi tiêu (75%)"
                                alertMsg = "Danh mục '$selectedCategoryName' đã tiêu ĐẠT mức 75% ngân sách tháng $txnMonth/$txnYear."
                            }

                            if (alertTitle.isNotEmpty()) {
                                db.notificationDao().insertNotification(
                                    Notification(
                                        userId = userId,
                                        title = alertTitle,
                                        description = "Ngân sách: ${com.fintrack.project.utils.CurrencyUtils.formatMoney(limit)} | Đã tiêu: ${com.fintrack.project.utils.CurrencyUtils.formatMoney(spentThisMonth)}",
                                        message = alertMsg,
                                        type = NotificationType.BUDGET_ALERT,
                                        createdAt = System.currentTimeMillis() + 500,
                                        isRead = false
                                    )
                                )
                            }
                        }
                    }

                    if (reached100Percent) {
                        db.notificationDao().insertNotification(
                            Notification(
                                userId = userId, title = "Mục tiêu hoàn thành! 🎉", description = "Bạn đã đạt 100% mục tiêu",
                                message = "Chúc mừng bạn đã tích lũy đủ ${com.fintrack.project.utils.CurrencyUtils.formatMoney(goalAmount)} cho mục tiêu '$goalName'. Hãy tự thưởng cho bản thân một tràng pháo tay nhé! \uD83C\uDF8A",
                                type = NotificationType.UPDATE, createdAt = System.currentTimeMillis() + 1000, isRead = false
                            )
                        )
                    }
                }
            }
            showSuccessScreen = true
        }
    }

    // Gửi SMS thật
    fun sendOtpSms() {
        generatedOtp = (100000..999999).random().toString()
        val phoneToSend = "5554" // Sửa thành số thật nếu chạy trên máy thật
        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }
            smsManager.sendTextMessage(phoneToSend, null, "FinTrack: Ma OTP xac nhan giao dich cua ban la $generatedOtp. Khong chia se ma nay voi bat ky ai.", null, null)
            showOtpDialog = true
        } catch (e: Exception) {
            Toast.makeText(context, "Lỗi gửi SMS: Không thể gửi tin nhắn.", Toast.LENGTH_SHORT).show()
            showOtpDialog = true
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            sendOtpSms()
        } else {
            Toast.makeText(context, "Cần cấp quyền SMS để nhận mã bảo mật!", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkAndSendOtp() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            sendOtpSms()
        } else {
            permissionLauncher.launch(Manifest.permission.SEND_SMS)
        }
    }

    fun onSaveClick() {
        val amount = amountText.toDoubleOrNull() ?: 0.0
        if (amount <= 0 || selectedCategoryId == null) return

        if (!userPin.isNullOrEmpty()) {
            showPinDialog = true
        } else {
            if (amount >= 10000000) checkAndSendOtp() else performSaveTransaction()
        }
    }

    if (showSuccessScreen) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.45f).background(mainColor))
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(80.dp))
                Box(modifier = Modifier.size(90.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Check, null, tint = mainColor, modifier = Modifier.size(48.dp)) }
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
                        DetailRow("Số tiền", (if (isIncome) "+" else "-") + com.fintrack.project.utils.CurrencyUtils.formatMoney(amountText.toDoubleOrNull()?:0.0), mainColor, true)
                        DetailRow("Danh mục", selectedCategoryName, Color.Black)
                        DetailRow("Ngày giao dịch", formatter.format(Date(selectedDateMillis)), Color.Black)
                        DetailRow("Mô tả", description.ifEmpty { selectedCategoryName }, Color.Black)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))
                        DetailRow("Số dư mới", com.fintrack.project.utils.CurrencyUtils.formatMoney(newBalance), Color(0xFF2E5BFF), true)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onHomeClick, modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = mainColor), shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Home, null, tint = Color.White, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Về trang chủ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = { amountText = ""; description = ""; selectedCategoryId = null; showSuccessScreen = false }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Icon(Icons.Default.Add, null, tint = Color.Gray, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Thêm giao dịch khác", color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        Scaffold(containerColor = Color(0xFFF8FAFC)) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(bottom = paddingValues.calculateBottomPadding()).verticalScroll(rememberScrollState())) {

                // HEADER CÓ BÓNG MỜ VÀ GRADIENT
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(Brush.verticalGradient(listOf(Color(0xFF1A3FBF), Color(0xFF3B82F6))))
                ) {
                    Box(modifier = Modifier.size(160.dp).align(Alignment.TopEnd).offset(x = 40.dp, y = (-40).dp).background(Color.White.copy(alpha = 0.08f), CircleShape))
                    Box(modifier = Modifier.size(100.dp).align(Alignment.BottomStart).offset(x = (-30).dp, y = 20.dp).background(Color.White.copy(alpha = 0.08f), CircleShape))

                    Column(modifier = Modifier.fillMaxWidth().padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart).size(36.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)) { Icon(Icons.Default.ChevronLeft, null, tint = Color.White) }
                            Text("Thêm Giao Dịch", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)).padding(4.dp)) {
                            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (isIncome) Color(0xFF10B981) else Color.Transparent).clickable { isIncome = true; selectedCategoryId = null }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) { Text("Khoản Thu", color = if (isIncome) Color.White else Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold) }
                            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (!isIncome) Color(0xFFEF4444) else Color.Transparent).clickable { isIncome = false; selectedCategoryId = null }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) { Text("Khoản Chi", color = if (!isIncome) Color.White else Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(if (isIncome) "SỐ TIỀN NHẬN ĐƯỢC" else "SỐ TIỀN CHI TIÊU", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (isIncome) "" else "- ", color = mainColor, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                                TextField(
                                    value = amountText, onValueChange = { amountText = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = mainColor),
                                    textStyle = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = mainColor), placeholder = { Text("0", fontSize = 32.sp, color = Color.LightGray) }, singleLine = true, modifier = Modifier.weight(1f)
                                )
                                Text("đ", color = mainColor, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }, verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(40.dp).background(Color(0xFFEFF6FF), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.CalendarToday, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp)) }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) { Text("NGÀY GIAO DỊCH", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold); Text(formatter.format(Date(selectedDateMillis)), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) }
                                Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(40.dp).background(Color(0xFFF3E8FF), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Edit, null, tint = Color(0xFFA855F7), modifier = Modifier.size(20.dp)) }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("MÔ TẢ", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    TextField(value = description, onValueChange = { description = it }, placeholder = { Text("Nhập mô tả giao dịch...", fontSize = 14.sp, color = Color.Gray) }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Danh mục", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(12.dp))

                    CategoryFlowLayout(modifier = Modifier.fillMaxWidth(), spacing = 8.dp) {
                        currentCategories.forEach { category ->
                            val isSelected = selectedCategoryId == category.id
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(16.dp)).border(1.dp, if (isSelected) mainColor else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                    .background(if (isSelected) mainColor.copy(alpha = 0.1f) else Color.White).clickable { selectedCategoryId = category.id; selectedCategoryName = category.name }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // SỬA Ở ĐÂY: Sử dụng hàm resolveLocalIcon siêu xịn
                                    Icon(
                                        resolveLocalIcon(category.icon ?: category.name),
                                        contentDescription = null,
                                        tint = if (isSelected) mainColor else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(category.name, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) mainColor else Color.DarkGray)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { onSaveClick() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = mainColor),
                        shape = RoundedCornerShape(16.dp),
                        enabled = amountText.isNotEmpty() && selectedCategoryId != null
                    ) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text(if (isIncome) "Lưu Khoản Thu" else "Lưu Khoản Chi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { selectedDateMillis = it }; showDatePicker = false }) { Text("OK") } },
                    dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Hủy") } }
                ) { DatePicker(state = datePickerState) }
            }

            // --- POPUP NHẬP MÃ PIN ---
            if (showPinDialog) {
                var inputPin by remember { mutableStateOf("") }
                fun onPinNumberClick(number: Int) { if (inputPin.length < 4) inputPin += number.toString() }
                fun onPinBackspaceClick() { if (inputPin.isNotEmpty()) inputPin = inputPin.dropLast(1) }

                Dialog(onDismissRequest = { showPinDialog = false }) {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Xác thực PIN", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                repeat(4) { i -> Box(modifier = Modifier.size(16.dp).background(if (i < inputPin.length) Color(0xFF2E5BFF) else Color(0xFFE2E8F0), CircleShape)) }
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                            PinPad(onNumberClick = ::onPinNumberClick, onBackspaceClick = ::onPinBackspaceClick)
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(onClick = { showPinDialog = false }, modifier = Modifier.weight(1f).height(48.dp)) { Text("Hủy") }
                                Spacer(modifier = Modifier.width(12.dp))
                                Button(
                                    onClick = {
                                        if (inputPin == userPin) {
                                            showPinDialog = false
                                            val amount = amountText.toDoubleOrNull() ?: 0.0
                                            if (amount >= 10000000) checkAndSendOtp() else performSaveTransaction()
                                        } else {
                                            Toast.makeText(context, "PIN không đúng!", Toast.LENGTH_SHORT).show()
                                            inputPin = ""
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp), enabled = inputPin.length == 4
                                ) { Text("Xác nhận") }
                            }
                        }
                    }
                }
            }

            // --- POPUP NHẬP OTP ---
            if (showOtpDialog) {
                var inputOtp by remember { mutableStateOf("") }
                fun onOtpNumberClick(number: Int) { if (inputOtp.length < 6) inputOtp += number.toString() }
                fun onOtpBackspaceClick() { if (inputOtp.isNotEmpty()) inputOtp = inputOtp.dropLast(1) }

                Dialog(onDismissRequest = { showOtpDialog = false }) {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Shield, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Xác thực giao dịch lớn", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Text("Kiểm tra tin nhắn SMS để lấy mã OTP bảo mật.", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)

                            Spacer(modifier = Modifier.height(24.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                repeat(6) { i ->
                                    val isFilled = i < inputOtp.length
                                    Box(
                                        modifier = Modifier.size(40.dp).border(1.dp, if(isFilled) Color(0xFF2E5BFF) else Color(0xFFE2E8F0), RoundedCornerShape(8.dp)).background(if(isFilled) Color(0xFFEFF6FF) else Color.Transparent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(if(isFilled) inputOtp[i].toString() else "", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                            PinPad(onNumberClick = ::onOtpNumberClick, onBackspaceClick = ::onOtpBackspaceClick, isOtp = true)

                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    if (inputOtp == generatedOtp) {
                                        showOtpDialog = false
                                        performSaveTransaction()
                                    } else {
                                        Toast.makeText(context, "Mã OTP không đúng!", Toast.LENGTH_SHORT).show()
                                        inputOtp = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E5BFF)),
                                enabled = inputOtp.length == 6
                            ) {
                                Text("Xác thực & Lưu", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// BỘ TỪ ĐIỂN DỊCH ICON LOCAL CỰC MẠNH
fun resolveLocalIcon(iconKey: String?): ImageVector {
    return when (iconKey) {
        "ic_food", "Ăn uống" -> Icons.Default.Fastfood
        "ic_school", "Giáo dục" -> Icons.Default.School
        "ic_hospital", "Y tế", "Sức khỏe" -> Icons.Default.LocalHospital
        "ic_movie", "Giải trí" -> Icons.Default.Movie
        "ic_car", "Giao thông" -> Icons.Default.DirectionsCar
        "ic_home", "Nhà ở" -> Icons.Default.Home
        "ic_shopping", "Mua sắm" -> Icons.Default.ShoppingCart
        "ic_money", "Lương" -> Icons.Default.AttachMoney
        "ic_gift", "Thưởng" -> Icons.Default.CardGiftcard
        "ic_store", "Kinh doanh" -> Icons.Default.Store
        "ic_trending_up", "Đầu tư" -> Icons.AutoMirrored.Filled.TrendingUp
        "ic_flight" -> Icons.Default.Flight
        "ic_cafe", "tien an trua" -> Icons.Default.LocalCafe
        "ic_pets" -> Icons.Default.Pets
        "ic_child" -> Icons.Default.ChildFriendly
        "ic_build" -> Icons.Default.Build
        "ic_computer" -> Icons.Default.Computer
        "ic_checkroom" -> Icons.Default.Checkroom
        "ic_spa" -> Icons.Default.Spa
        else -> Icons.Default.MoreHoriz
    }
}

@Composable
fun PinPad(onNumberClick: (Int) -> Unit, onBackspaceClick: () -> Unit, isOtp: Boolean = false) {
    val padModifier = Modifier.size(if(isOtp) 48.dp else 56.dp).clip(CircleShape)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9)).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { num -> PinNumberButton(num.toString(), padModifier) { onNumberClick(num) } }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = padModifier)
            PinNumberButton("0", padModifier) { onNumberClick(0) }
            Box(modifier = padModifier.clickable { onBackspaceClick() }.background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
                Text("⌫", fontSize = 24.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun PinNumberButton(text: String, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier = modifier.clickable(onClick = onClick).background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 24.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
    }
}

@Composable
fun DetailRow(label: String, value: String, valueColor: Color, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 13.sp); Text(value, color = valueColor, fontSize = 14.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
    }
}