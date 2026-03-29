package com.fintrack.project.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.project.data.database.FinTrackDatabase
import com.fintrack.project.data.model.Category
import com.fintrack.project.data.model.CategoryType
import com.fintrack.project.data.model.Notification
import com.fintrack.project.data.model.NotificationType
import com.fintrack.project.data.model.Transaction
import com.fintrack.project.data.model.TransactionType
import com.fintrack.project.service.BankNotificationListenerService
import com.fintrack.project.service.ParsedBankTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────────────────────────────────────
//  Nút "Nhập từ thông báo" – đặt trong TransactionHistoryScreen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BankImportButton(
    modifier: Modifier = Modifier,
    onImported: () -> Unit = {}
) {
    val context = LocalContext.current
    var showSheet  by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    // Kiểm tra pending khi hiển thị nút
    var pendingCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(showSheet) {
        pendingCount = BankNotificationListenerService.getPendingTransactions(context).size
    }

    Box(modifier = modifier) {
        Button(
            onClick = {
                if (BankNotificationListenerService.isNotificationAccessGranted(context)) {
                    showSheet = true
                } else {
                    showDialog = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9)),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.NotificationsActive, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Từ thông báo", fontSize = 13.sp)
        }

        // Badge số lượng pending
        if (pendingCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .size(20.dp)
                    .background(Color(0xFFEF4444), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (pendingCount > 9) "9+" else "$pendingCount",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Bottom sheet xác nhận giao dịch
    if (showSheet) {
        BankNotificationImportSheet(
            onDismiss = { showSheet = false },
            onImported = {
                showSheet = false
                onImported()
            }
        )
    }

    // Dialog yêu cầu cấp quyền
    if (showDialog) {
        NotificationPermissionDialog(
            onConfirm = {
                showDialog = false
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            },
            onDismiss = { showDialog = false }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Bottom Sheet chính: danh sách giao dịch chờ xác nhận
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankNotificationImportSheet(
    onDismiss: () -> Unit,
    onImported: () -> Unit
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // State
    var pendingList by remember {
        mutableStateOf(BankNotificationListenerService.getPendingTransactions(context))
    }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var currentUserId by remember { mutableIntStateOf(-1) }
    var isLoading by remember { mutableStateOf(false) }

    // Map categoryId theo loại giao dịch
    val expenseCategories  by remember(categories) { derivedStateOf { categories.filter { it.type == CategoryType.EXPENSE } } }
    val incomeCategories   by remember(categories) { derivedStateOf { categories.filter { it.type == CategoryType.INCOME } } }

    // SỬA LỖI Ở ĐÂY: Ép kiểu rõ ràng thành MutableList<Int?>
    var selectedCategories by remember(pendingList) {
        mutableStateOf<MutableList<Int?>>(
            MutableList(pendingList.size) { null }
        )
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val prefs = context.getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)
            currentUserId = prefs.getInt("LOGGED_IN_USER_ID", -1)
            if (currentUserId != -1) {
                categories = FinTrackDatabase.getInstance(context)
                    .categoryDao().getUserCategories(currentUserId)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF8FAFC),
        dragHandle = {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.width(40.dp).height(4.dp).background(Color(0xFFCBD5E1), RoundedCornerShape(2.dp)))
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp)) {

            // Header
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Giao dịch từ thông báo", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
                    Text("Chọn danh mục và xác nhận từng giao dịch", fontSize = 12.sp, color = Color(0xFF64748B))
                }
                if (pendingList.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            BankNotificationListenerService.clearPendingTransactions(context)
                            onDismiss()
                        }
                    ) {
                        Text("Bỏ qua tất cả", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (pendingList.isEmpty()) {
                // Trạng thái trống
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(72.dp).background(Color(0xFFE0F2FE), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF0EA5E9), modifier = Modifier.size(36.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Không có giao dịch mới", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "FinTrack sẽ tự động phát hiện\nkhi có thông báo giao dịch mới từ ngân hàng.",
                        fontSize = 13.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(pendingList) { index, txn ->
                        val chosenCategoryList = if (txn.type == TransactionType.INCOME) incomeCategories else expenseCategories

                        PendingTransactionCard(
                            txn = txn,
                            categories = chosenCategoryList,
                            selectedCategoryId = selectedCategories.getOrNull(index),
                            onCategorySelected = { catId ->
                                val newList = selectedCategories.toMutableList()
                                newList[index] = catId
                                selectedCategories = newList
                            },
                            onConfirm = {
                                if (currentUserId == -1) return@PendingTransactionCard
                                isLoading = true
                                scope.launch(Dispatchers.IO) {
                                    val db = FinTrackDatabase.getInstance(context)
                                    val catId = selectedCategories.getOrNull(index)
                                        ?: chosenCategoryList.firstOrNull()?.id ?: return@launch

                                    // Lưu giao dịch
                                    val newTxn = Transaction(
                                        userId          = currentUserId,
                                        categoryId      = catId,
                                        amount          = txn.amount,
                                        type            = txn.type,
                                        description     = txn.description,
                                        transactionDate = txn.timestamp,
                                        sourceBank      = txn.bankName
                                    )
                                    val txnId = db.transactionDao().insertTransaction(newTxn)

                                    // Lưu notification lịch sử
                                    val typeLabel = if (txn.type == TransactionType.INCOME) "thu" else "chi"
                                    db.notificationDao().insertNotification(
                                        Notification(
                                            userId        = currentUserId,
                                            title         = "Giao dịch từ ${txn.bankName}",
                                            description   = "${txn.description} • ${formatCurrencyVN(txn.amount)}",
                                            type          = NotificationType.TRANSACTION,
                                            transactionId = txnId.toInt(), // SẼ KHÔNG CÒN LỖI
                                            message       = "Đã nhập khoản $typeLabel ${formatCurrencyVN(txn.amount)} từ ${txn.bankName}",
                                            createdAt     = System.currentTimeMillis()
                                        )
                                    )
                                    // GỌI HÀM KIỂM TRA NGÂN SÁCH Ở ĐÂY
                                    if (txn.type == TransactionType.EXPENSE) {
                                        val catName = chosenCategoryList.find { it.id == catId }?.name ?: "Danh mục"
                                        checkBudgetAlert(context, db, currentUserId, catId, txn.amount, txn.timestamp, catName)
                                    }
                                    // Xóa khỏi pending
                                    BankNotificationListenerService.removePendingTransaction(context, index)

                                    withContext(Dispatchers.Main) {
                                        // Refresh danh sách
                                        pendingList = BankNotificationListenerService.getPendingTransactions(context)
                                        selectedCategories = MutableList(pendingList.size) { null }
                                        isLoading = false
                                        if (pendingList.isEmpty()) onImported()
                                    }
                                }
                            },
                            onSkip = {
                                BankNotificationListenerService.removePendingTransaction(context, index)
                                pendingList = BankNotificationListenerService.getPendingTransactions(context)
                                selectedCategories = MutableList(pendingList.size) { null }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Nút "Xác nhận tất cả"
                Button(
                    onClick = {
                        if (currentUserId == -1) return@Button
                        isLoading = true
                        scope.launch(Dispatchers.IO) {
                            val db = FinTrackDatabase.getInstance(context)
                            pendingList.forEachIndexed { index, txn ->
                                val chosenCats = if (txn.type == TransactionType.INCOME) incomeCategories else expenseCategories
                                val catId = selectedCategories.getOrNull(index)
                                    ?: chosenCats.firstOrNull()?.id ?: return@forEachIndexed

                                val newTxn = Transaction(
                                    userId          = currentUserId,
                                    categoryId      = catId,
                                    amount          = txn.amount,
                                    type            = txn.type,
                                    description     = txn.description,
                                    transactionDate = txn.timestamp,
                                    sourceBank      = txn.bankName
                                )
                                val txnId = db.transactionDao().insertTransaction(newTxn)
                                val typeLabel = if (txn.type == TransactionType.INCOME) "thu" else "chi"
                                db.notificationDao().insertNotification(
                                    Notification(
                                        userId        = currentUserId,
                                        title         = "Giao dịch từ ${txn.bankName}",
                                        description   = "${txn.description} • ${formatCurrencyVN(txn.amount)}",
                                        type          = NotificationType.TRANSACTION,
                                        transactionId = txnId.toInt(), // SẼ KHÔNG CÒN LỖI
                                        message       = "Đã nhập khoản $typeLabel ${formatCurrencyVN(txn.amount)} từ ${txn.bankName}",
                                        createdAt     = System.currentTimeMillis()
                                    )
                                )
                                if (txn.type == TransactionType.EXPENSE) {
                                    val catName = chosenCats.find { it.id == catId }?.name ?: "Danh mục"
                                    checkBudgetAlert(context, db, currentUserId, catId, txn.amount, txn.timestamp, catName)
                                }
                            }
                            BankNotificationListenerService.clearPendingTransactions(context)
                            withContext(Dispatchers.Main) {
                                isLoading = false
                                onImported()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3FBF)),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.DoneAll, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Xác nhận tất cả (${pendingList.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Card một giao dịch đang chờ xác nhận
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PendingTransactionCard(
    txn: ParsedBankTransaction,
    categories: List<Category>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int) -> Unit,
    onConfirm: () -> Unit,
    onSkip: () -> Unit
) {
    val isIncome   = txn.type == TransactionType.INCOME
    val accentColor = if (isIncome) Color(0xFF10B981) else Color(0xFFEF4444)
    val bgColor     = if (isIncome) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
    val timeStr     = SimpleDateFormat("HH:mm dd/MM", Locale("vi")).format(Date(txn.timestamp))

    var expanded by remember { mutableStateOf(false) }
    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }
        ?: categories.firstOrNull()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Row: icon + info + số tiền
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(bgColor, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(txn.bankName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                    Text(txn.description, fontSize = 12.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(timeStr, fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isIncome) "+" else "-"}${formatCurrencyVN(txn.amount)}",
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (txn.balance != null) {
                        Text("SD: ${formatCurrencyVN(txn.balance)}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(Modifier.height(12.dp))

            // Dropdown chọn danh mục
            if (categories.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E293B)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Category, null, modifier = Modifier.size(16.dp), tint = Color(0xFF64748B))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = selectedCategory?.name ?: "Chọn danh mục",
                            modifier = Modifier.weight(1f),
                            fontSize = 13.sp,
                            color = if (selectedCategory != null) Color(0xFF1E293B) else Color(0xFF94A3B8)
                        )
                        Icon(Icons.Default.ArrowDropDown, null, tint = Color(0xFF64748B))
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.heightIn(max = 240.dp)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name, fontSize = 14.sp) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier.size(28.dp).background(
                                            if (isIncome) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                                            RoundedCornerShape(6.dp)
                                        ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            resolveCategoryIcon(cat.icon ?: cat.name),
                                            null, modifier = Modifier.size(16.dp), tint = accentColor
                                        )
                                    }
                                },
                                onClick = {
                                    onCategorySelected(cat.id)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Row: bỏ qua / xác nhận
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B))
                ) {
                    Text("Bỏ qua", fontSize = 13.sp)
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Nhập", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Dialog yêu cầu cấp quyền Notification Access
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NotificationPermissionDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier.size(56.dp).background(Color(0xFFE0F2FE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.NotificationsActive, null, tint = Color(0xFF0EA5E9), modifier = Modifier.size(28.dp))
            }
        },
        title = {
            Text("Cấp quyền đọc thông báo", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        },
        text = {
            Text(
                "FinTrack cần quyền đọc thông báo để tự động nhận diện giao dịch từ ngân hàng.\n\n" +
                        "Sau khi bấm \"Mở cài đặt\", tìm FinTrack trong danh sách và bật quyền truy cập thông báo.",
                fontSize = 14.sp, color = Color(0xFF475569), textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Mở cài đặt", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Để sau", color = Color(0xFF94A3B8))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun formatCurrencyVN(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return "${formatter.format(amount.toLong())} đ"
}

// --- HÀM KIỂM TRA NGÂN SÁCH ---
private suspend fun checkBudgetAlert(
    context: Context,
    db: FinTrackDatabase,
    userId: Int,
    categoryId: Int,
    amount: Double,
    transactionDate: Long,
    categoryName: String
) {
    val cal = Calendar.getInstance().apply { timeInMillis = transactionDate }
    val txnMonth = cal.get(Calendar.MONTH) + 1
    val txnYear = cal.get(Calendar.YEAR)

    val budgets = db.budgetDao().getBudgetsByMonth(userId, txnMonth, txnYear)
    val budget = budgets.find { it.categoryId == categoryId }

    if (budget != null && budget.limitAmount > 0) {
        val limit = budget.limitAmount
        val allTxns = db.transactionDao().getAllTransactionsByUser(userId)

        val spentThisMonth = allTxns.filter {
            it.type == TransactionType.EXPENSE &&
                    it.categoryId == categoryId &&
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
            alertMsg = "Danh mục '$categoryName' đã VƯỢT giới hạn chi tiêu của tháng $txnMonth."
        } else if (oldPct < 0.75 && newPct >= 0.75) {
            alertTitle = "Cảnh báo chi tiêu (75%)"
            alertMsg = "Danh mục '$categoryName' đã tiêu ĐẠT mức 75% ngân sách tháng $txnMonth."
        }

        if (alertTitle.isNotEmpty()) {
            db.notificationDao().insertNotification(
                Notification(
                    userId = userId,
                    title = alertTitle,
                    description = "Ngân sách: ${formatCurrencyVN(limit)} | Đã tiêu: ${formatCurrencyVN(spentThisMonth)}",
                    message = alertMsg,
                    type = NotificationType.BUDGET_ALERT,
                    createdAt = System.currentTimeMillis() + 500,
                    isRead = false
                )
            )
        }
    }
}