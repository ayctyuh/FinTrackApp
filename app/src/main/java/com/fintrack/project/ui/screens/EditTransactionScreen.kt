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
import com.fintrack.project.data.model.Transaction
import com.fintrack.project.data.model.TransactionType
import com.fintrack.project.utils.CurrencyUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    transactionId: Int, // <-- Nhận ID giao dịch cần sửa
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }

    var isIncome by remember { mutableStateOf(false) }
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

    var showOtpDialog by remember { mutableStateOf(false) }
    var generatedOtp by remember { mutableStateOf("") }

    // TẢI DỮ LIỆU GIAO DỊCH CŨ LÊN MÀN HÌNH
    LaunchedEffect(transactionId) {
        withContext(Dispatchers.IO) {
            val prefs = context.getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)
            val userId = prefs.getInt("LOGGED_IN_USER_ID", -1)
            if (userId != -1) {
                val db = FinTrackDatabase.getInstance(context)

                // Load danh mục
                incomeCategories = db.categoryDao().getCategoriesByType(userId, com.fintrack.project.data.model.CategoryType.INCOME)
                expenseCategories = db.categoryDao().getCategoriesByType(userId, com.fintrack.project.data.model.CategoryType.EXPENSE)

                // Load User PIN
                val user = db.userDao().getUserById(userId)
                userPin = user?.pinCode

                // Load Giao dịch hiện tại (Lấy tất cả rồi lọc, hoặc bạn dùng hàm getTransactionById nếu có)
                val allTxns = db.transactionDao().getAllTransactionsByUser(userId)
                val txn = allTxns.find { it.id == transactionId }

                if (txn != null) {
                    transactionToEdit = txn
                    isIncome = txn.type == TransactionType.INCOME
                    amountText = txn.amount.toLong().toString() // Bỏ số thập phân để hiện đẹp
                    description = txn.description ?: ""
                    selectedDateMillis = txn.transactionDate
                    selectedCategoryId = txn.categoryId

                    // Tìm tên danh mục
                    val cats = if (isIncome) incomeCategories else expenseCategories
                    selectedCategoryName = cats.find { it.id == txn.categoryId }?.name ?: ""
                }
            }
            isLoading = false
        }
    }

    val currentCategories = if (isIncome) incomeCategories else expenseCategories

    fun performUpdateTransaction() {
        val amount = amountText.toDoubleOrNull() ?: 0.0
        if (amount <= 0 || selectedCategoryId == null || transactionToEdit == null) return

        scope.launch {
            withContext(Dispatchers.IO) {
                val db = FinTrackDatabase.getInstance(context)
                val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE

                // Cập nhật giao dịch
                val updatedTxn = transactionToEdit!!.copy(
                    amount = amount,
                    type = type,
                    categoryId = selectedCategoryId!!,
                    transactionDate = selectedDateMillis,
                    description = description.ifEmpty { selectedCategoryName }
                )
                db.transactionDao().updateTransaction(updatedTxn)

                // Tính lại số dư mới
                val userId = updatedTxn.userId
                val totalIncNew = db.transactionDao().getTotalAmount(userId, TransactionType.INCOME) ?: 0.0
                val totalExpNew = db.transactionDao().getTotalAmount(userId, TransactionType.EXPENSE) ?: 0.0
                newBalance = totalIncNew - totalExpNew
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
            smsManager.sendTextMessage(phoneToSend, null, "FinTrack: Ma OTP xac nhan sua giao dich cua ban la $generatedOtp. Khong chia se ma nay voi bat ky ai.", null, null)
            showOtpDialog = true
        } catch (e: Exception) {
            Toast.makeText(context, "Lỗi gửi SMS: Không thể gửi tin nhắn.", Toast.LENGTH_SHORT).show()
            showOtpDialog = true
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) sendOtpSms()
        else Toast.makeText(context, "Cần cấp quyền SMS để nhận mã bảo mật!", Toast.LENGTH_SHORT).show()
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
            if (amount >= 10000000) checkAndSendOtp() else performUpdateTransaction()
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF2E5BFF))
        }
    } else if (showSuccessScreen) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.45f).background(mainColor))
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(80.dp))
                Box(modifier = Modifier.size(90.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Check, null, tint = mainColor, modifier = Modifier.size(48.dp)) }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Cập nhật thành công!", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Giao dịch đã được lưu lại các thay đổi", color = Color.White.copy(alpha = 0.8f))

                Spacer(modifier = Modifier.height(40.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("CHI TIẾT GIAO DỊCH", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("#TXN-${transactionId.toString().padStart(5, '0')}", color = Color.Gray, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailRow("Số tiền", (if (isIncome) "+" else "-") + CurrencyUtils.formatMoney(amountText.toDoubleOrNull()?:0.0), mainColor, true)
                        DetailRow("Danh mục", selectedCategoryName, Color.Black)
                        DetailRow("Ngày giao dịch", formatter.format(Date(selectedDateMillis)), Color.Black)
                        DetailRow("Mô tả", description.ifEmpty { selectedCategoryName }, Color.Black)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))
                        DetailRow("Số dư mới", CurrencyUtils.formatMoney(newBalance), Color(0xFF2E5BFF), true)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onSaveSuccess, modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = mainColor), shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Quay lại", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
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
                            Text("Sửa Giao Dịch", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
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
                                    Icon(resolveLocalIcon(category.icon ?: category.name), contentDescription = null, tint = if (isSelected) mainColor else Color.Gray, modifier = Modifier.size(16.dp))
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
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text(if (isIncome) "Cập nhật Khoản Thu" else "Cập nhật Khoản Chi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                                            if (amount >= 10000000) checkAndSendOtp() else performUpdateTransaction()
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
                                        performUpdateTransaction()
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