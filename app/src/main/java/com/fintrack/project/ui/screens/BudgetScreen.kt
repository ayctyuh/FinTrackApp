package com.fintrack.project.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.fintrack.project.data.model.Budget
import com.fintrack.project.data.model.Category
import com.fintrack.project.presentation.viewmodel.BudgetViewModel
import com.fintrack.project.utils.CategoryUtils.resolveCategoryColor
import com.fintrack.project.utils.CategoryUtils.resolveCategoryIcon
import com.fintrack.project.utils.CurrencyUtils
import kotlinx.coroutines.launch
import java.util.*

// ────────────────────────────────────────────────────────────────
// COLOR PALETTE
// ────────────────────────────────────────────────────────────────
private val PrimaryBlue       = Color(0xFF2563EB)
private val PrimaryBlueLight  = Color(0xFFEFF6FF)
private val AccentGreen       = Color(0xFF10B981)
private val AccentRed         = Color(0xFFEF4444)
private val TextPrimary       = Color(0xFF1E293B)
private val TextSecondary     = Color(0xFF64748B)
private val TextMuted         = Color(0xFF94A3B8)
private val Surface           = Color(0xFFF8FAFC)
private val CardBg            = Color(0xFFFFFFFF)
private val DividerColor      = Color(0xFFE2E8F0)

data class CategoryBudgetUiState(
    val categoryId: Int,
    val name: String,
    val iconEmoji: ImageVector,
    val iconBgColor: Color,
    val spent: Double,
    val limit: Double,
    val budget: Budget?
)

enum class BudgetSetupStep { TOTAL, DISTRIBUTE, SUCCESS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    userId: Int,
    viewModel: BudgetViewModel,
    spentByCategory: Map<Int, Double> = emptyMap(),
    categories: List<Category> = emptyList(),
    onNavigateTo: (Int) -> Unit = {},
    onSeeReport: () -> Unit = {}
) {
    val budgets by viewModel.budgets.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val isLoading by viewModel.loading.collectAsState()

    var currentMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1) }
    var currentYear  by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var showMonthYearPicker by remember { mutableStateOf(false) }

    var showSetupWizard by remember { mutableStateOf(false) }
    var setupStep        by remember { mutableStateOf(BudgetSetupStep.TOTAL) }
    var totalBudgetInput by remember { mutableStateOf("") }
    var editingCategory  by remember { mutableStateOf<CategoryBudgetUiState?>(null) }
    var editLimitInput   by remember { mutableStateOf("") }
    var showEditDialog   by remember { mutableStateOf(false) }
    
    val budgetMap = remember(budgets) { budgets.associateBy { it.categoryId } }
    val categoryUiList = buildCategoryUiList(categories, spentByCategory, budgetMap)

    if (showMonthYearPicker) {
        MonthYearPickerDialog(
            initialMonth = currentMonth,
            initialYear = currentYear,
            onDismiss = { showMonthYearPicker = false },
            onConfirm = { month, year ->
                currentMonth = month
                currentYear = year
                showMonthYearPicker = false
            }
        )
    }

    if (showEditDialog && editingCategory != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Thiết lập ngân sách", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(editingCategory?.name ?: "", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = editLimitInput,
                        onValueChange = { editLimitInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Số tiền (đ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = editLimitInput.toDoubleOrNull() ?: 0.0
                        val b = editingCategory?.budget?.copy(limitAmount = amount)
                            ?: Budget(
                                userId = userId,
                                categoryId = editingCategory?.categoryId,
                                limitAmount = amount,
                                month = currentMonth,
                                year = currentYear
                            )
                        if (editingCategory?.budget != null) viewModel.updateBudget(b)
                        else viewModel.createBudget(b)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Lưu", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Hủy", color = TextSecondary)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    LaunchedEffect(currentMonth, currentYear) {
        viewModel.getBudgetsByMonth(userId, currentMonth, currentYear)
    }

    val totalSpent = spentByCategory.values.sumOf { it }
    val totalLimit = monthlyBudget?.limitAmount ?: 0.0

    Scaffold(
        containerColor = Surface,
        topBar = {
            BudgetTopBar(
                hasMonthlyBudget = monthlyBudget != null,
                showBackButton = showSetupWizard && setupStep != BudgetSetupStep.SUCCESS,
                isWizard = showSetupWizard,
                onMonthYearClick = { showMonthYearPicker = true },
                onBackClick = {
                    if (setupStep == BudgetSetupStep.DISTRIBUTE) setupStep = BudgetSetupStep.TOTAL
                    else showSetupWizard = false
                },
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
        },
        bottomBar = {
            ProfileBottomNavigationBar(
                onHomeClick = { onNavigateTo(0) },
                onStatisticsClick = { onNavigateTo(1) },
                onAddClick = { onNavigateTo(2) },
                onBudgetClick = { },
                onProfileClick = { onNavigateTo(4) },
                currentScreen = "Ngân sách"
            )
        }
    ) { padding ->
        if (showSetupWizard) {
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
                    budgets.forEach { viewModel.deleteBudget(it) }
                    showSetupWizard = false
                },
                onEditCategory = { cat ->
                    editingCategory = cat
                    editLimitInput = if (cat.limit > 0) cat.limit.toLong().toString() else ""
                    showEditDialog = true
                },
                onProceedToDistribute = {
                    val amount = totalBudgetInput.toDoubleOrNull() ?: return@BudgetSetupWizard
                    val b = monthlyBudget?.copy(limitAmount = amount)
                        ?: Budget(userId = userId, categoryId = null, limitAmount = amount, month = currentMonth, year = currentYear)
                    if (monthlyBudget != null) viewModel.updateBudget(b)
                    else viewModel.createBudget(b)
                    setupStep = BudgetSetupStep.DISTRIBUTE
                },
                onSaveDistribute = { setupStep = BudgetSetupStep.SUCCESS },
                onFinish = { showSetupWizard = false },
                modifier = Modifier.padding(padding)
            )
        } else {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else if (monthlyBudget == null) {
                EmptyBudgetView(
                    onSetupClick = {
                        showSetupWizard = true
                        setupStep = BudgetSetupStep.TOTAL
                    },
                    onCopyClick = { viewModel.copyBudgetFromPreviousMonth(userId, currentMonth, currentYear) },
                    modifier = Modifier.padding(padding)
                )
            } else {
                BudgetMainView(
                    categoryUiList = categoryUiList,
                    onSeeReport = onSeeReport,
                    onEditCategory = { cat ->
                        editingCategory = cat
                        editLimitInput = if (cat.limit > 0) cat.limit.toLong().toString() else ""
                        showEditDialog = true
                    },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetTopBar(
    hasMonthlyBudget: Boolean,
    showBackButton: Boolean,
    isWizard: Boolean,
    onAddClick: () -> Unit,
    onBackClick: () -> Unit,
    currentMonth: Int,
    currentYear: Int,
    totalLimit: Double,
    categoryCount: Int,
    onMonthYearClick: () -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)).background(Brush.verticalGradient(listOf(Color(0xFF1A3FBF), Color(0xFF3B82F6))))) {
        Box(modifier = Modifier.size(160.dp).align(Alignment.TopEnd).offset(x = 40.dp, y = (-40).dp).background(Color.White.copy(alpha = 0.08f), CircleShape))
        Box(modifier = Modifier.size(100.dp).align(Alignment.BottomStart).offset(x = (-30).dp, y = 20.dp).background(Color.White.copy(alpha = 0.08f), CircleShape))
        Column {
            CenterAlignedTopAppBar(
                title = { Text("Ngân sách", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White) },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBackClick) {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                            }
                        }
                    }
                },
                actions = {
                    if (!isWizard) {
                        IconButton(onClick = onAddClick) {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                Icon(if (hasMonthlyBudget) Icons.Default.Edit else Icons.Default.Add, null, tint = Color.White)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )

            if (!isWizard) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrevMonth) { Icon(Icons.Default.ChevronLeft, null, tint = Color.White.copy(alpha = 0.8f)) }
                    Text("Tháng $currentMonth - $currentYear", modifier = Modifier.clickable { onMonthYearClick() }, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                    IconButton(onClick = onNextMonth) { Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.8f)) }
                }
                if (hasMonthlyBudget) {
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        HeaderStatItem("NGÂN SÁCH TỔNG", CurrencyUtils.formatMoney(totalLimit), Color.White, Modifier.weight(1f))
                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.3f)))
                        HeaderStatItem("SỐ DANH MỤC", "$categoryCount", Color.White, Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun HeaderStatItem(label: String, value: String, color: Color, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun BudgetMainView(
    categoryUiList: List<CategoryBudgetUiState>,
    onSeeReport: () -> Unit,
    onEditCategory: (CategoryBudgetUiState) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardBg), elevation = CardDefaults.cardElevation(1.dp)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Phân chia theo danh mục", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        Text("Xem báo cáo \u2192", modifier = Modifier.clickable { onSeeReport() }, fontSize = 13.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = DividerColor)
                    categoryUiList.forEachIndexed { index, cat ->
                        CategoryBudgetRow(item = cat, onEdit = { onEditCategory(cat) })
                        if (index < categoryUiList.size - 1) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = DividerColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyBudgetView(onSetupClick: () -> Unit, onCopyClick: () -> Unit, modifier: Modifier) {
    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(88.dp).clip(RoundedCornerShape(24.dp)).background(PrimaryBlue), contentAlignment = Alignment.Center) {
            Text("\uD83D\uDCBC", fontSize = 40.sp)
        }
        Spacer(Modifier.height(20.dp))
        Text("Chưa có ngân sách", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
        Text("Thiết lập ngay để kiểm soát chi tiêu!", modifier = Modifier.padding(horizontal = 32.dp), textAlign = TextAlign.Center, color = TextSecondary)
        Spacer(Modifier.height(28.dp))
        Button(onClick = onSetupClick, modifier = Modifier.fillMaxWidth(0.8f).height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
            Text("Thiết lập ngân sách", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onCopyClick, modifier = Modifier.fillMaxWidth(0.8f).height(52.dp), shape = RoundedCornerShape(14.dp)) {
            Text("Sao chép từ tháng trước", color = TextSecondary)
        }
    }
}

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
    hasExistingBudget: Boolean,
    onDeleteBudget: () -> Unit,
    onEditCategory: (CategoryBudgetUiState) -> Unit,
    onProceedToDistribute: () -> Unit,
    onSaveDistribute: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier
) {
    AnimatedContent(targetState = step, transitionSpec = { (fadeIn() + slideInVertically { it / 4 }).togetherWith(fadeOut()) }, label = "") { targetStep ->
        when (targetStep) {
            BudgetSetupStep.TOTAL -> StepTotalBudget(totalBudgetInput, onTotalChange, currentMonth, currentYear, hasExistingBudget, onDeleteBudget, onProceedToDistribute, modifier)
            BudgetSetupStep.DISTRIBUTE -> StepDistribute(totalBudgetInput, categoryUiList, onEditCategory, onSaveDistribute, modifier)
            BudgetSetupStep.SUCCESS -> StepSuccess(totalBudgetInput.toDoubleOrNull() ?: 0.0, categoryUiList.count { it.limit > 0 }, currentMonth, currentYear, onFinish, modifier)
        }
    }
}

@Composable
private fun StepTotalBudget(input: String, onInputChange: (String) -> Unit, m: Int, y: Int, isEdit: Boolean, onDelete: () -> Unit, onNext: () -> Unit, modifier: Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardBg)) {
            Column(Modifier.padding(20.dp)) {
                Text("Tháng $m - $y", fontSize = 13.sp, color = TextSecondary)
                Text("TỔNG NGÂN SÁCH THÁNG", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                OutlinedTextField(
                    value = input, onValueChange = { onInputChange(it.filter { c -> c.isDigit() }) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
            Text("Tiếp theo", color = Color.White)
        }
        if (isEdit) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onDelete, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
                Text("Xóa ngân sách", color = AccentRed)
            }
        }
    }
}

@Composable
private fun StepDistribute(totalInput: String, list: List<CategoryBudgetUiState>, onEdit: (CategoryBudgetUiState) -> Unit, onSave: () -> Unit, modifier: Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardBg)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Tổng: ${CurrencyUtils.formatMoney(totalInput.toDoubleOrNull() ?: 0.0)}", fontWeight = FontWeight.Bold)
                }
            }
        }
        itemsIndexed(list) { _, cat ->
            CategoryDistributeRow(cat, onEdit = { onEdit(cat) })
        }
        item {
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp), shape = RoundedCornerShape(14.dp)) {
                Text("Lưu thiết lập")
            }
        }
    }
}

@Composable
private fun StepSuccess(total: Double, count: Int, m: Int, y: Int, onFinish: () -> Unit, modifier: Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(AccentGreen), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("Thiết lập hoàn tất!", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text("Ngân sách tháng $m - $y đã sẵn sàng.", textAlign = TextAlign.Center, color = TextSecondary)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onFinish, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
            Text("Xong")
        }
    }
}

@Composable
private fun CategoryBudgetRow(item: CategoryBudgetUiState, onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AccentRed.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(item.iconEmoji, null, tint = AccentRed, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
            Text(
                if (item.limit > 0) CurrencyUtils.formatMoney(item.limit) else "Chưa đặt",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun CategoryDistributeRow(item: CategoryBudgetUiState, onEdit: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clip(RoundedCornerShape(12.dp)).background(CardBg).clickable { onEdit() }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(item.iconBgColor.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
            Icon(item.iconEmoji, null, tint = item.iconBgColor)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name, fontWeight = FontWeight.SemiBold)
            Text(if (item.limit > 0) CurrencyUtils.formatMoney(item.limit) else "Chưa đặt", fontSize = 12.sp, color = TextSecondary)
        }
        Icon(Icons.Default.Edit, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun MonthYearPickerDialog(
    initialMonth: Int,
    initialYear: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var selectedMonth by remember { mutableIntStateOf(initialMonth) }
    var selectedYear by remember { mutableIntStateOf(initialYear) }
    
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = ((currentYear - 10)..(currentYear + 10)).toList()
    val months = (1..12).toList()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Chọn thời gian", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                Spacer(Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Month Wheel
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        WheelPicker(
                            items = months.map { "T$it" },
                            initialIndex = months.indexOf(selectedMonth),
                            onItemSelected = { selectedMonth = months[it] }
                        )
                    }
                    
                    Box(modifier = Modifier.width(1.dp).height(100.dp).background(DividerColor))
                    
                    // Year Wheel
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        WheelPicker(
                            items = years.map { it.toString() },
                            initialIndex = years.indexOf(selectedYear),
                            onItemSelected = { selectedYear = years[it] }
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Hủy", color = TextSecondary, fontWeight = FontWeight.SemiBold) }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(selectedMonth, selectedYear) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Xác nhận", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WheelPicker(
    items: List<String>,
    initialIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    val itemHeight = 40.dp
    val visibleItemsCount = 3
    val scrollState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (!scrollState.isScrollInProgress) {
            onItemSelected(scrollState.firstVisibleItemIndex)
        }
    }

    Box(
        modifier = Modifier.height(itemHeight * visibleItemsCount).fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Highlight selection
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
        )
        
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeight),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(items) { index, item ->
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .clickable {
                            coroutineScope.launch {
                                scrollState.animateScrollToItem(index)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val isSelected = scrollState.firstVisibleItemIndex == index
                    Text(
                        text = item,
                        fontSize = if (isSelected) 18.sp else 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) PrimaryBlue else TextSecondary
                    )
                }
            }
        }
    }
}

private fun buildCategoryUiList(categories: List<Category>, spentMap: Map<Int, Double>, budgetMap: Map<Int?, Budget>): List<CategoryBudgetUiState> {
    return categories.filter { it.type.name == "EXPENSE" }.map { cat ->
        CategoryBudgetUiState(
            categoryId = cat.id, name = cat.name, iconEmoji = resolveCategoryIcon(cat.icon ?: cat.name),
            iconBgColor = resolveCategoryColor(cat.color, Color.Gray),
            spent = spentMap[cat.id] ?: 0.0, limit = budgetMap[cat.id]?.limitAmount ?: 0.0, budget = budgetMap[cat.id]
        )
    }
}
