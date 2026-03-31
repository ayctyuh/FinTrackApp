package com.fintrack.project.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.animation.fadeIn
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.project.data.model.Budget
import com.fintrack.project.data.model.Category
import com.fintrack.project.presentation.viewmodel.BudgetViewModel
import java.text.NumberFormat
import java.util.*

// ────────────────────────────────────────────────────────────────
// COLOR PALETTE — đồng bộ với theme xanh dương của dự án
// ────────────────────────────────────────────────────────────────
private val PrimaryBlue       = Color(0xFF2563EB)
private val PrimaryBlueDark   = Color(0xFF1D4ED8)
private val PrimaryBlueLight  = Color(0xFFEFF6FF)
private val AccentGreen       = Color(0xFF10B981)
private val AccentOrange      = Color(0xFFF59E0B)
private val AccentRed         = Color(0xFFEF4444)
private val TextPrimary       = Color(0xFF1E293B)
private val TextSecondary     = Color(0xFF64748B)
private val TextMuted         = Color(0xFF94A3B8)
private val Surface           = Color(0xFFF8FAFC)
private val CardBg            = Color(0xFFFFFFFF)
private val DividerColor      = Color(0xFFE2E8F0)

// ────────────────────────────────────────────────────────────────
// DATA CLASSES HỖ TRỢ UI
// ────────────────────────────────────────────────────────────────

/** Trạng thái từng danh mục: icon, tên, đã chi, giới hạn */
data class CategoryBudgetUiState(
    val categoryId: Int,
    val name: String,
    val iconEmoji: ImageVector,
    val iconBgColor: Color,
    val spent: Double,
    val limit: Double,
    val budget: Budget?
)

/** Bước trong wizard thiết lập ngân sách */
enum class BudgetSetupStep { TOTAL, DISTRIBUTE }

// ────────────────────────────────────────────────────────────────
// MAIN SCREEN
// ────────────────────────────────────────────────────────────────

/**
 * BudgetScreen — Màn hình Ngân sách chính.
 *
 * @param userId         ID người dùng hiện tại
 * @param viewModel      BudgetViewModel đã được inject
 * @param spentByCategory Map<categoryId, totalSpent> — lấy từ TransactionViewModel
 * @param categories     Danh sách danh mục của người dùng
 * @param onNavigateTo   Callback điều hướng bottom nav (0=Home,1=Stats,2=Add,3=Budget,4=Profile)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    userId: Int,
    viewModel: BudgetViewModel,
    spentByCategory: Map<Int, Double> = emptyMap(),
    categories: List<Category> = emptyList(),
    onNavigateTo: (Int) -> Unit = {},
    onSeeReportClick: () -> Unit = {}
) {
    val budgets by viewModel.budgets.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val isLoading by viewModel.loading.collectAsState()

    // Tháng/năm đang xem
    var currentMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1) }
    var currentYear  by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var showMonthYearPicker by remember { mutableStateOf(false) }

    // Wizard thiết lập
    var showSetupWizard by remember { mutableStateOf(false) }
    var setupStep        by remember { mutableStateOf(BudgetSetupStep.TOTAL) }
    var totalBudgetInput by remember { mutableStateOf("") }
    var editingCategory  by remember { mutableStateOf<CategoryBudgetUiState?>(null) }
    var editLimitInput   by remember { mutableStateOf("") }
    var showEditDialog   by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    // Map từ categoryId → Budget
    val budgetMap = remember(budgets) { budgets.associateBy { it.categoryId } }

    // Danh sách UI cho từng danh mục chi tiêu
    val categoryUiList = buildCategoryUiList(categories, spentByCategory, budgetMap)

    LaunchedEffect(currentMonth, currentYear) {
        viewModel.getBudgetsByMonth(userId, currentMonth, currentYear)
    }
    val displayMonthlyBudget: Budget? = monthlyBudget

    // Tổng đã chi
    val totalSpent = spentByCategory.values.sumOf { it }
    val totalLimit = monthlyBudget?.limitAmount ?: 0.0
    val handleBack = {
        when (setupStep) {
            BudgetSetupStep.DISTRIBUTE     -> setupStep = BudgetSetupStep.TOTAL
            else -> showSetupWizard = false
        }
    }

    Scaffold(
        containerColor = Surface,
        topBar = {
            BudgetTopBar(
                hasMonthlyBudget = displayMonthlyBudget != null,
                showBackButton = showSetupWizard,
                isWizard = showSetupWizard,
                onMonthYearClick = { showMonthYearPicker = true },
                onBackClick = handleBack,
                onAddClick = {
                    showSetupWizard = true
                    setupStep = BudgetSetupStep.TOTAL
                    totalBudgetInput = if (totalLimit > 0) totalLimit.toLong().toString() else ""
                },
                currentMonth = currentMonth,
                currentYear = currentYear,
                totalLimit = totalLimit,
                categoryCount = categoryUiList.count { it.limit > 0 },
                onPrevMonth = {
                    if (currentMonth == 1) { currentMonth = 12; currentYear-- }
                    else currentMonth--
                },
                onNextMonth = {
                    if (currentMonth == 12) { currentMonth = 1; currentYear++ }
                    else currentMonth++
                }
            )
            if (showMonthYearPicker) {
                MonthYearPickerDialog(
                    currentMonth = currentMonth,
                    currentYear = currentYear,
                    onDismiss = { showMonthYearPicker = false },
                    onConfirm = { month, year ->
                        currentMonth = month
                        currentYear = year
                        showMonthYearPicker = false
                    }
                )
            }
        },
        bottomBar = {
            ProfileBottomNavigationBar(
                onHomeClick = { onNavigateTo(0) },
                onStatisticsClick = { onNavigateTo(1) },
                onAddClick = { onNavigateTo(2) },
                onBudgetClick = { /* Đang ở màn hình này rồi, không làm gì cả */ },
                onProfileClick = { onNavigateTo(4) },
                currentScreen = "Ngân sách"
            )
        }
    ) { padding ->
        if (showSetupWizard) {
            // ── WIZARD FLOW ──────────────────────────────────────
            BudgetSetupWizard(
                step = setupStep,
                totalBudgetInput = totalBudgetInput,
                onTotalChange = { totalBudgetInput = it },
                currentMonth = currentMonth,
                currentYear = currentYear,
                categoryUiList = categoryUiList,
                editingCategory = editingCategory,
                editLimitInput = editLimitInput,
                onEditLimitChange = { editLimitInput = it },
                hasExistingBudget = monthlyBudget != null,
                onDeleteBudget = {
                    monthlyBudget?.let { viewModel.deleteBudget(it) }
                    budgets.forEach { budget ->
                        viewModel.deleteBudget(budget)
                    }
                    viewModel.getBudgetsByMonth(userId, currentMonth, currentYear)
                    showSetupWizard = false
                },
                onEditCategory = { cat ->
                    editingCategory = cat
                    editLimitInput = cat.limit.let { if (it > 0) it.toInt().toString() else "" }
                    showEditDialog = true
                },
                onSaveCategory = { cat, newLimit ->
                    val b = cat.budget?.copy(limitAmount = newLimit)
                        ?: Budget(
                            userId = userId,
                            categoryId = cat.categoryId,
                            limitAmount = newLimit,
                            month = currentMonth,
                            year = currentYear
                        )
                    setupStep = BudgetSetupStep.DISTRIBUTE
                    if (cat.budget != null) viewModel.updateBudget(b)
                    else viewModel.createBudget(b)
                },
                onProceedToDistribute = {
                    val amount = totalBudgetInput.toDoubleOrNull() ?: return@BudgetSetupWizard
                    val b = monthlyBudget?.copy(limitAmount = amount)
                        ?: Budget(
                            userId = userId,
                            categoryId = null,
                            limitAmount = amount,
                            month = currentMonth,
                            year = currentYear
                        )
                    if (monthlyBudget != null) viewModel.updateBudget(b)
                    else viewModel.createBudget(b)
                    setupStep = BudgetSetupStep.DISTRIBUTE
                },
                onSkipDistribute = {
                    showSetupWizard = false
                    showSuccessDialog = true
                },
                onSaveDistribute = {
                    showSetupWizard = false
                    showSuccessDialog = true
                },
                onBack = {
                    when (setupStep) {
                        BudgetSetupStep.DISTRIBUTE     -> setupStep = BudgetSetupStep.TOTAL
                        else -> showSetupWizard = false
                    }
                },
                modifier = Modifier.padding(padding)
            )
            if (showEditDialog && editingCategory != null) {
                // TÍNH TOÁN NGÂN SÁCH ĐỂ KIỂM TRA VƯỢT HẠN MỨC
                val totalMonthly = totalBudgetInput.toDoubleOrNull() ?: 0.0
                val otherCategoriesSum = categoryUiList
                    .filter { it.categoryId != editingCategory!!.categoryId } // Tổng các danh mục KHÁC danh mục đang sửa
                    .sumOf { it.limit }

                CategoryEditPopup(
                    category = editingCategory!!,
                    limitInput = editLimitInput,
                    totalMonthlyBudget = totalMonthly,
                    otherCategoriesSum = otherCategoriesSum,
                    onLimitChange = { editLimitInput = it },
                    onDismiss = { showEditDialog = false }, // Nút Đóng
                    onSave = {
                        val newLimit = editLimitInput.toDoubleOrNull() ?: 0.0
                        val cat = editingCategory!!
                        // Lưu vào Database
                        val b = cat.budget?.copy(limitAmount = newLimit)
                            ?: Budget(
                                userId = userId,
                                categoryId = cat.categoryId,
                                limitAmount = newLimit,
                                month = currentMonth,
                                year = currentYear
                            )
                        if (cat.budget != null) viewModel.updateBudget(b)
                        else viewModel.createBudget(b)

                        showEditDialog = false // Lưu xong thì tắt Popup
                    }
                )
            }
        } else {
            // ── MAIN VIEW ────────────────────────────────────────
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Surface),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else if (displayMonthlyBudget == null) {
                // 2. Load xong và KHÔNG có ngân sách -> Hiện trang Chưa thiết lập (Cái cặp xách)
                EmptyBudgetView(
                    currentMonth = currentMonth,
                    currentYear = currentYear,
                    onPrevMonth = {
                        if (currentMonth == 1) { currentMonth = 12; currentYear-- }
                        else currentMonth--
                    },
                    onNextMonth = {
                        if (currentMonth == 12) { currentMonth = 1; currentYear++ }
                        else currentMonth++
                    },
                    onSetupClick = {
                        showSetupWizard = true
                        setupStep = BudgetSetupStep.TOTAL
                    },
                    onCopyClick = {
                        viewModel.copyBudgetFromPreviousMonth(userId, currentMonth, currentYear)
                    },
                    modifier = Modifier.padding(padding)
                )
            } else {
                // 3. Load xong và CÓ ngân sách -> Hiện danh sách chi tiêu
                BudgetMainView(
                    currentMonth = currentMonth,
                    currentYear = currentYear,
                    totalLimit = totalLimit,
                    totalSpent = totalSpent,
                    categoryUiList = categoryUiList,
                    onPrevMonth = {},
                    onNextMonth = {},
                    onSeeReportClick = onSeeReportClick,
                    modifier = Modifier.padding(padding)
                )
            }
        }
        if (showSuccessDialog) {
            BudgetSuccessDialog(
                totalBudget = totalBudgetInput.toDoubleOrNull() ?: 0.0,
                categoryCount = categoryUiList.count { it.limit > 0 },
                currentMonth = currentMonth,
                currentYear = currentYear,
                onDismiss = { showSuccessDialog = false } // Đóng popup khi nhấn nút
            )
        }
    }
}

// ────────────────────────────────────────────────────────────────
// TOP BAR
// ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetTopBar(
    hasMonthlyBudget: Boolean,
    showBackButton: Boolean = false,
    isWizard: Boolean = false,
    onAddClick: () -> Unit,
    onBackClick: () -> Unit = {},
    // Thêm các tham số cho month + stats
    currentMonth: Int = 0,
    currentYear: Int = 0,
    totalLimit: Double = 0.0,
    categoryCount: Int = 0,
    onMonthYearClick: () -> Unit,
    onPrevMonth: () -> Unit = {},
    onNextMonth: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF1A3FBF), Color(0xFF3B82F6))))
    ) {
        Box(modifier = Modifier.size(160.dp).align(Alignment.TopEnd).offset(x = 40.dp, y = (-40).dp).background(Color.White.copy(alpha = 0.08f), CircleShape))
        Box(modifier = Modifier.size(100.dp).align(Alignment.BottomStart).offset(x = (-30).dp, y = 20.dp).background(Color.White.copy(alpha = 0.08f), CircleShape))

        Column {
            Spacer(modifier = Modifier.height(5.dp))
            // ── Tiêu đề + nút back/add ──────────────────────────
            CenterAlignedTopAppBar(
                title = {
                    Text("Ngân sách", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.padding(start = 12.dp).size(36.dp).padding(start = 4.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)) {
                            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Quay lại", tint = Color.White)
                        }
                    }
                },
                actions = {
                    // CHỈ HIỆN NÚT (THÊM/SỬA) KHI ĐANG Ở TRANG CHÍNH (KHÔNG PHẢI WIZARD)
                    if (!isWizard) {
                        IconButton(onClick = onAddClick) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                // Có ngân sách -> Bút chì. Chưa có -> Dấu cộng
                                if (hasMonthlyBudget) {
                                    Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = Color.White)
                                } else {
                                    Icon(Icons.Default.Add, contentDescription = "Thêm", tint = Color.White)
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )

            // ── Month selector + stats (chỉ hiện khi không ở wizard) ──
            if (!isWizard && currentMonth > 0) {
                // Month row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPrevMonth, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Tháng trước", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onMonthYearClick() }
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Text("Tháng $currentMonth - $currentYear", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                    }
                    IconButton(onClick = onNextMonth, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Tháng sau", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                    }
                }

                // CHỈ HIỆN 2 Ô THỐNG KÊ (Ngân sách tổng, số danh mục) KHI ĐÃ THIẾT LẬP
                if (hasMonthlyBudget) {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeaderStatItem("NGÂN SÁCH TỔNG", formatMoney(totalLimit), Color.White, modifier = Modifier.weight(1f))
                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.3f)))
                        HeaderStatItem("SỐ DANH MỤC", "$categoryCount", Color.White, modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(20.dp))
            } else if (showBackButton) {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// Helper cho stat item trong header xanh
@Composable
private fun HeaderStatItem(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(3.dp))
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// MAIN VIEW — có ngân sách
@Composable
private fun BudgetMainView(
    currentMonth: Int,
    currentYear: Int,
    totalLimit: Double,
    totalSpent: Double,
    categoryUiList: List<CategoryBudgetUiState>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSeeReportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. TÌM CÁC DANH MỤC "VƯỢT RÀO"
    val untrackedCategories = categoryUiList.filter { it.limit <= 0 && it.spent > 0 }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp) // Thêm chút đệm ở trên
    ) {
        // 2. THÊM BANNER CẢNH BÁO NẾU CÓ
        if (untrackedCategories.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 11.dp).offset(y = (-10).dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)), // Cam nhạt
                    //border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFEA580C), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Phát sinh chi tiêu ngoài kế hoạch", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF9A3412))
                            Text("Có ${untrackedCategories.size} danh mục chưa đặt hạn mức phát sinh chi tiêu", fontSize = 12.sp, color = Color(0xFFC2410C))
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Phân bổ từng danh mục", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Text(text = "Xem báo cáo →", fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onSeeReportClick() })
                    }

                    // ─── DANH SÁCH DANH MỤC ───────────────────────
                    categoryUiList.forEachIndexed { index, cat ->
                        CategoryBudgetRow(item = cat)
                        if (index < categoryUiList.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color(0xFFF1F5F9), thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────
// EMPTY VIEW — chưa thiết lập ngân sách
// ────────────────────────────────────────────────────────────────

@Composable
private fun EmptyBudgetView(
    currentMonth: Int,
    currentYear: Int,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSetupClick: () -> Unit,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = "Chưa có ngân sách", tint = Color.White, modifier = Modifier.size(44.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text("Chưa có ngân sách", fontWeight = FontWeight.Bold,
                fontSize = 18.sp, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            Text(
                "Bạn chưa thiết lập ngân sách cho tháng này.\nBắt đầu để kiểm soát chi tiêu tốt hơn!",
                fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onSetupClick,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Thiết lập ngân sách", color = Color.White,
                    fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onCopyClick,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
            ) {
                Text("Sao chép từ tháng trước", color = TextSecondary,
                    fontWeight = FontWeight.Medium, fontSize = 15.sp)
            }
        }

        Spacer(Modifier.weight(1f))
    }
}

// ────────────────────────────────────────────────────────────────
// WIZARD THIẾT LẬP NGÂN SÁCH (6 bước A→F)
// ────────────────────────────────────────────────────────────────

@Composable
private fun BudgetSetupWizard(
    step: BudgetSetupStep,
    totalBudgetInput: String,
    onTotalChange: (String) -> Unit,
    currentMonth: Int,
    currentYear: Int,
    categoryUiList: List<CategoryBudgetUiState>,
    editingCategory: CategoryBudgetUiState?,
    editLimitInput: String,
    onEditLimitChange: (String) -> Unit,
    onEditCategory: (CategoryBudgetUiState) -> Unit,
    onSaveCategory: (CategoryBudgetUiState, Double) -> Unit,
    onProceedToDistribute: () -> Unit,
    onSkipDistribute: () -> Unit,
    onSaveDistribute: () -> Unit,
    onBack: () -> Unit,
    hasExistingBudget: Boolean,
    onDeleteBudget: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = step,
        transitionSpec = {
            (fadeIn() + slideInVertically { it / 4 }).togetherWith(fadeOut())
        },
        label = "wizard_step"
    ) { targetStep ->
        when (targetStep) {
            // ── BƯỚC C: Nhập ngân sách tổng ──────────────────────
            BudgetSetupStep.TOTAL -> StepTotalBudget(
                input = totalBudgetInput,
                onInputChange = onTotalChange,
                currentMonth = currentMonth,
                currentYear = currentYear,
                isEditing = hasExistingBudget,
                onDelete = onDeleteBudget,
                onBack = onBack,
                onNext = onProceedToDistribute,
                modifier = modifier
            )

            // ── BƯỚC D: Phân bổ theo danh mục ────────────────────
            BudgetSetupStep.DISTRIBUTE -> StepDistribute(
                totalBudgetInput = totalBudgetInput,
                categoryUiList = categoryUiList,
                onBack = onBack,
                onEditCategory = onEditCategory,
                onSkip = onSkipDistribute,
                onSave = onSaveDistribute,
                modifier = modifier
            )
        }
    }
}

// ────────────────────────────────────────────────────────────────
// BƯỚC C — Nhập ngân sách tổng
// ────────────────────────────────────────────────────────────────

@Composable
private fun StepTotalBudget(
    input: String,
    onInputChange: (String) -> Unit,
    currentMonth: Int,
    currentYear: Int,
    isEditing: Boolean = false, // <-- THÊM
    onDelete: () -> Unit = {},
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quickOptions = listOf(1000000.0, 2000000.0, 3000000.0, 5000000.0)
    val isValid = input.toDoubleOrNull()?.let { it > 0 } == true

    Column(modifier = modifier.fillMaxSize().background(Surface)) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            "Tháng $currentMonth - $currentYear",
                            fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("TỔNG NGÂN SÁCH THÁNG", fontSize = 11.sp,
                            color = TextMuted, fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp)
                        Spacer(Modifier.height(8.dp))

                        // Input field
                        OutlinedTextField(
                            value = input,
                            onValueChange = { onInputChange(it.filter { c -> c.isDigit() || c == '.' }) },
                            modifier = Modifier.fillMaxWidth(),
                            suffix = { Text("đ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = DividerColor
                            )
                        )

                        // Validation feedback
                        if (input.isNotEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                if (isValid) "✓ Hợp lệ" else "✗ Vui lòng nhập số hợp lệ",
                                fontSize = 12.sp,
                                color = if (isValid) AccentGreen else AccentRed
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        Text("GỢI Ý NHANH", fontSize = 11.sp, color = TextMuted,
                            fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
                        Spacer(Modifier.height(8.dp))
                        QuickAmountSuggestions(currentInput = input, options = quickOptions, onSelect = { onInputChange(it) })
                        Spacer(Modifier.height(16.dp))
                        // Info hint
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryBlueLight)
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("ℹ️", fontSize = 14.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Bước tiếp theo bạn có thể phân bổ cho từng danh mục (Ăn uống, Mua sắm...)",
                                fontSize = 13.sp, color = PrimaryBlue, lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            // Buttons
            item {
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = isValid,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Tiếp theo", color = Color.White,
                        fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }

                // THÊM ĐOẠN NÀY ĐỂ HIỆN NÚT XÓA KHI ĐANG CHỈNH SỬA
                if (isEditing) {
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = AccentRed, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Xóa thiết lập", color = AccentRed, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────
// BƯỚC D — Phân bổ ngân sách theo danh mục
// ────────────────────────────────────────────────────────────────

@Composable
private fun StepDistribute(
    totalBudgetInput: String,
    categoryUiList: List<CategoryBudgetUiState>,
    onBack: () -> Unit,
    onEditCategory: (CategoryBudgetUiState) -> Unit,
    onSkip: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalBudget = totalBudgetInput.toDoubleOrNull() ?: 0.0
    val distributed = categoryUiList.filter { it.limit > 0 }.sumOf { it.limit }
    val remaining   = totalBudget - distributed
    val distPct     = if (totalBudget > 0) (distributed / totalBudget * 100).toInt() else 0

    Column(modifier = modifier.fillMaxSize().background(Surface)) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Progress bar tổng
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tổng: ${formatMoney(totalBudget)}",
                                fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            Text("Còn lại: ${formatMoney(remaining)}",
                                fontWeight = FontWeight.Bold, fontSize = 14.sp,
                                color = if (remaining >= 0) AccentGreen else AccentRed)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("Đã phân bổ: ${formatMoney(distributed)}",
                            fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            item {
                Text("Đặt hạn mức từng danh mục",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                    fontSize = 13.sp, color = TextSecondary)
            }

            itemsIndexed(categoryUiList) { _, cat ->
                CategoryDistributeRow(item = cat, onEdit = { onEditCategory(cat) })
            }

            item {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Lưu thiết lập", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────
// BƯỚC E — POPUP CHỈNH SỬA HẠN MỨC DANH MỤC
// ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryEditPopup(
    category: CategoryBudgetUiState,
    limitInput: String,
    onLimitChange: (String) -> Unit,
    totalMonthlyBudget: Double,
    otherCategoriesSum: Double,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val quickSuggestions = listOf(100000.0, 200000.0, 500000.0, 1000000.0)
    val newLimit = limitInput.toDoubleOrNull() ?: 0.0

    // TÍNH TOÁN LOGIC VƯỢT NGÂN SÁCH
    val newTotalDistributed = otherCategoriesSum + newLimit
    val isOverBudget = totalMonthlyBudget > 0 && newTotalDistributed > totalMonthlyBudget
    val overAmount = newTotalDistributed - totalMonthlyBudget

    // Dùng ModalBottomSheet (kéo từ dưới lên) hoặc Dialog tùy bạn, ở đây tôi dùng Dialog giống TransactionDetailDialog
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp), // Bo góc tròn trịa giống các Dialog khác
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                // 1. Dòng Tiêu đề và Nút Đóng (Góc trên cùng)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cài đặt hạn mức",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFF1F5F9), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2. Khu vực Icon và Tên Danh mục
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(category.iconBgColor.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.iconEmoji,
                            contentDescription = category.name,
                            tint = category.iconBgColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(category.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                        Text(
                            text = "Đã chi: ${formatMoney(category.spent)}",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Ô nhập liệu (Input Field) theo chuẩn theme
                Text("HẠN MỨC MỚI", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = limitInput,
                    onValueChange = { onLimitChange(it.filter { c -> c.isDigit() || c == '.' }) },
                    modifier = Modifier.fillMaxWidth(),
                    suffix = { Text("đ", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedBorderColor = Color(0xFF2E5BFF),
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color(0xFF1E293B),
                        unfocusedTextColor = Color(0xFF1E293B)
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End // Căn phải số tiền cho đẹp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Các nút Gợi ý nhanh
                QuickAmountSuggestions(currentInput = limitInput, options = quickSuggestions, onSelect = { onLimitChange(it) })

                Spacer(modifier = Modifier.height(32.dp))

                // 5. CẢNH BÁO MÀU ĐỎ (Chỉ hiện khi vượt ngân sách)
                if (isOverBudget) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Vượt ngân sách tổng ${formatMoney(overAmount)}",
                            color = Color(0xFFEF4444),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 6. Nút Lưu (Tự động mờ đi và khóa lại nếu vượt ngân sách)
                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = newLimit > 0 && !isOverBudget, // ĐIỀU KIỆN KHÓA NÚT Ở ĐÂY
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E5BFF),
                        disabledContainerColor = Color(0xFFE2E8F0), // Đổi màu xám nhạt khi bị khóa
                        disabledContentColor = Color(0xFF94A3B8)
                    )
                ) {
                    Text("Lưu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
// ────────────────────────────────────────────────────────────────
// BƯỚC F — Thiết lập thành công
// ────────────────────────────────────────────────────────────────

@Composable
fun BudgetSuccessDialog(
    totalBudget: Double,
    categoryCount: Int,
    currentMonth: Int,
    currentYear: Int,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Biểu tượng Checkmark xanh lá
                Box(
                    modifier = Modifier.size(72.dp).background(Color(0xFFD1FAE5), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.size(52.dp).background(Color(0xFF10B981), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓", fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(20.dp))
                Text("Thiết lập hoàn tất!", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1E293B))
                Spacer(Modifier.height(8.dp))
                Text(
                    "Ngân sách tháng $currentMonth - $currentYear đã được thiết lập. FinTrack sẽ thông báo khi bạn chi tiêu gần đạt giới hạn.",
                    fontSize = 13.sp, color = Color(0xFF64748B),
                    textAlign = TextAlign.Center, lineHeight = 20.sp
                )

                Spacer(Modifier.height(24.dp))

                // Ô thông số nhỏ gọn trong Popup
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp)).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("NGÂN SÁCH", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(formatMoney(totalBudget), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFFE2E8F0)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SỐ DANH MỤC", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("$categoryCount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Nút Xác nhận đóng Popup
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E5BFF))
                ) {
                    Text("Xác nhận", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────
// COMPOSABLES DÙNG LẠI
// ────────────────────────────────────────────────────────────────
/** Row một danh mục trong main view */
@Composable
private fun CategoryBudgetRow(
    item: CategoryBudgetUiState,
) {
    val hasLimit = item.limit > 0
    val lightBgColor = item.iconBgColor.copy(alpha = 0.15f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Icon danh mục
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color = lightBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.iconEmoji, contentDescription = null, tint = item.iconBgColor, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))

            // 2. Tên danh mục
            Column(Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold,
                    fontSize = 15.sp, color = TextPrimary, maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
            }

            // 3. Hiển thị Hạn mức hoặc "Chưa đặt" ở bên phải
            Column(horizontalAlignment = Alignment.End) {
                if (hasLimit) {
                    Text(
                        text = formatMoney(item.limit),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                } else {
                    if (item.spent > 0) {
                        Text("Chưa thiết lập", fontSize = 13.sp, color = Color(0xFFEA580C), fontWeight = FontWeight.Bold)
                        Text("Phát sinh: ${formatMoney(item.spent)}", fontSize = 11.sp, color = TextSecondary)
                    } else {
                        Text("Chưa thiết lập", fontSize = 13.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

/** Row danh mục trong bước D */
@Composable
private fun CategoryDistributeRow(
    item: CategoryBudgetUiState,
    onEdit: () -> Unit
) {
    val lightBgColor = item.iconBgColor.copy(alpha = 0.15f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .clickable { onEdit() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color = lightBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(item.iconEmoji, contentDescription = null, tint = item.iconBgColor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)

            if (item.limit > 0) {
                // Đã có hạn mức
                Text(
                    text = "${formatMoney(item.limit)} / tháng",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            } else {
                // Chưa có hạn mức -> Kiểm tra xem đã lỡ chi chưa
                if (item.spent > 0) {
                    Text (text = "Chưa thiết lập, có phát sinh", fontSize = 12.sp, color = Color(0xFFEA580C), fontWeight = FontWeight.Bold)
                } else {
                    Text (text = "Chưa thiết lập", fontSize = 12.sp, color = TextMuted)
                }
            }
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Sửa",
                tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

// Hàm chọn tháng năm ở giao diện ngân sách chính
@Composable
fun MonthYearPickerDialog(
    currentMonth: Int,
    currentYear: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var selectedMonth by remember { mutableIntStateOf(currentMonth) }
    var selectedYear by remember { mutableIntStateOf(currentYear) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedMonth, selectedYear) }) { Text("Xác nhận", fontWeight = FontWeight.Bold, color = PrimaryBlue) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy", color = TextSecondary) }
        },
        title = { Text("Chọn thời gian", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Tái sử dụng Component rút gọn
                DropdownSelector(modifier = Modifier.weight(1f), label = "Tháng", selectedValue = selectedMonth, items = (1..12).toList(), onItemSelected = { selectedMonth = it })
                DropdownSelector(modifier = Modifier.weight(1f), label = "Năm", selectedValue = selectedYear, items = (2020..2030).toList(), onItemSelected = { selectedYear = it })
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

// Component dùng chung cho cả Tháng và Năm
@Composable
private fun DropdownSelector(modifier: Modifier, label: String, selectedValue: Int, items: List<Int>, onItemSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedCard(
            onClick = { expanded = true },
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)), shape = RoundedCornerShape(12.dp)
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("$label $selectedValue", Modifier.weight(1f), fontSize = 14.sp, color = TextPrimary)
                Icon(Icons.Default.ArrowDropDown, null, tint = TextSecondary)
            }
        }
        MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))) {
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White).width(120.dp).heightIn(max = 250.dp)) {
                items.forEach { item -> DropdownMenuItem(text = { Text("$label $item", fontSize = 14.sp) }, onClick = { onItemSelected(item); expanded = false }) }
            }
        }
    }
}

@Composable
private fun QuickAmountSuggestions(currentInput: String, options: List<Double>, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { opt ->
            val optStr = opt.toLong().toString() // Ép về Long để bỏ .0
            val selected = currentInput == optStr
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                    .background(if (selected) PrimaryBlue else PrimaryBlueLight)
                    .border(1.dp, if (selected) PrimaryBlue else DividerColor, RoundedCornerShape(8.dp))
                    .clickable { onSelect(optStr) }.padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                val displayText = if (opt >= 1000000) "${(opt / 1000000).toInt()}Tr" else "${(opt / 1000).toInt()}K"
                Text(text = displayText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (selected) Color.White else PrimaryBlue)
            }
        }
    }
}

private fun formatMoney(amount: Double): String {
    val format = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return "${format.format(amount)} đ"
}

private fun buildCategoryUiList(
    categories: List<Category>,
    spentByCategory: Map<Int, Double>,
    budgetMap: Map<Int?, Budget>
): List<CategoryBudgetUiState> {
    return categories
        .filter { it.type.name == "EXPENSE" }
        .map { cat ->
            val catIcon = resolveCategoryIcon(cat.icon ?: cat.name)

            // Lấy màu từ DB, nếu không có thì cho màu mặc định (VD: Màu Đỏ)
            val bgColor = resolveCategoryColor(cat.color, Color(0xFFEF4444))
            val spent  = spentByCategory[cat.id] ?: 0.0
            val budget = budgetMap[cat.id]
            CategoryBudgetUiState(
                categoryId   = cat.id,
                name         = cat.name,
                iconEmoji    = catIcon,
                iconBgColor  = bgColor,
                spent        = spent,
                limit        = budget?.limitAmount ?: 0.0,
                budget       = budget
            )
        }
}