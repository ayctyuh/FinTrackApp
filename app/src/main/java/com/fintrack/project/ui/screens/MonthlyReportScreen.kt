package com.fintrack.project.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.project.data.model.Category
import com.fintrack.project.presentation.viewmodel.BudgetViewModel
import com.fintrack.project.presentation.viewmodel.MonthSummary
import com.fintrack.project.utils.CategoryUtils
import com.fintrack.project.utils.CurrencyUtils
import java.util.*

@Composable
fun MonthlyReportScreen(
    userId: Int,
    viewModel: BudgetViewModel,
    categories: List<Category>,
    onBackClick: () -> Unit
) {
    var viewingMonth by remember { mutableStateOf<Int?>(null) }
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    val yearlySummary by viewModel.yearlySummary.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val spentByCategory by viewModel.spentByCategoryMonth.collectAsState()
    val transactionCount by viewModel.transactionCount.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getYearlySummary(userId, currentYear)
    }

    if (viewingMonth == null) {
        YearlyReportView(
            year = currentYear,
            summaryList = yearlySummary,
            onBackClick = onBackClick,
            onMonthClick = {
                viewingMonth = it
                viewModel.getBudgetsByMonth(userId, it, currentYear)
            }
        )
    } else {
        MonthlyDetailView(
            month = viewingMonth!!,
            year = currentYear,
            totalLimit = monthlyBudget?.limitAmount ?: 0.0,
            spentByCategory = spentByCategory,
            categoryBudgets = budgets.filter { it.categoryId != null }.associate { it.categoryId!! to it.limitAmount },
            categories = categories,
            transactionCount = transactionCount,
            onBack = { viewingMonth = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearlyReportView(
    year: Int,
    summaryList: List<MonthSummary>,
    onBackClick: () -> Unit,
    onMonthClick: (Int) -> Unit
) {
    val totalSpent = summaryList.sumOf { it.totalSpent }
    val activeMonths = summaryList.count { it.totalSpent > 0 || it.totalLimit > 0 }
    val avgSpent = if (activeMonths > 0) totalSpent / activeMonths else 0.0
    val totalSavings = summaryList.sumOf { if (it.totalLimit > it.totalSpent) it.totalLimit - it.totalSpent else 0.0 }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Báo cáo theo năm", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.padding(start = 8.dp).size(36.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)) {
                        Icon(Icons.Default.ChevronLeft, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF1A3FBF))
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF8FAFC))) {

            // BOX NỀN XANH CÓ CHỨA BÓNG MỜ
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF1A3FBF), Color(0xFF3B82F6))))
                    .padding(bottom = 24.dp)
            ) {
                // 2 quả bóng mờ
                Box(modifier = Modifier.size(160.dp).align(Alignment.TopEnd).offset(x = 40.dp, y = (-40).dp).background(Color.White.copy(alpha = 0.08f), CircleShape))
                Box(modifier = Modifier.size(100.dp).align(Alignment.BottomStart).offset(x = (-30).dp, y = 20.dp).background(Color.White.copy(alpha = 0.08f), CircleShape))

                // Nội dung chữ nằm bên trên bóng mờ
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    StatBoxSmall("TỔNG CHI", CurrencyUtils.formatMoneyShort(totalSpent), Color(0xFFF59E0B), Modifier.weight(1f))
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(alpha = 0.2f)))
                    StatBoxSmall("TB/THÁNG", CurrencyUtils.formatMoneyShort(avgSpent), Color.White, Modifier.weight(1f))
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(alpha = 0.2f)))
                    StatBoxSmall("TIẾT KIỆM", "+${CurrencyUtils.formatMoneyShort(totalSavings)}", Color(0xFF10B981), Modifier.weight(1f))
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(alpha = 0.2f)))
                    StatBoxSmall("ĐÃ GHI NHẬN", "$activeMonths tháng", Color.White, Modifier.weight(1f))
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatusChipSquare(summaryList.count { it.status == "DAT" }, "ĐẠT", Color(0xFFDCFCE7), Color(0xFF10B981), Modifier.weight(1f))
                    StatusChipSquare(summaryList.count { it.status == "GAN_VUOT" }, "GẦN VƯỢT", Color(0xFFFEF3C7), Color(0xFFF59E0B), Modifier.weight(1f))
                    StatusChipSquare(summaryList.count { it.status == "VUOT" }, "VƯỢT", Color(0xFFFEE2E2), Color(0xFFEF4444), Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text("CHỌN THÁNG — $year", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(columns = GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                    items(summaryList) { item ->
                        MonthGridCard(item, onClick = { onMonthClick(item.month) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyDetailView(
    month: Int,
    year: Int,
    totalLimit: Double,
    spentByCategory: Map<Int, Double>,
    categoryBudgets: Map<Int, Double>,
    categories: List<Category>,
    transactionCount: Int,
    onBack: () -> Unit
) {
    val totalSpent = spentByCategory.values.sumOf { it }
    val remaining = totalLimit - totalSpent
    val progressRatio = if (totalLimit > 0) (totalSpent / totalLimit).toFloat() else 0f
    val progress = progressRatio.coerceIn(0f, 1f)

    val (progressColor, statusColor) = when {
        progressRatio >= 1f -> Color(0xFFEF4444) to Color(0xFFEF4444) // Đỏ (Vượt)
        progressRatio >= 0.8f -> Color(0xFFF59E0B) to Color(0xFFF59E0B) // Cam (Gần vượt)
        else -> Color(0xFF4ADE80) to Color(0xFF10B981) // Xanh (Tốt)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Báo cáo theo tháng", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp).size(36.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)) {
                        Icon(Icons.Default.ChevronLeft, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF1A3FBF))
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF8FAFC)).verticalScroll(rememberScrollState())) {

            // BOX NỀN XANH CÓ CHỨA BÓNG MỜ
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF1A3FBF), Color(0xFF3B82F6))))
                    .padding(bottom = 24.dp)
            ) {
                // 2 quả bóng mờ
                Box(modifier = Modifier.size(160.dp).align(Alignment.TopEnd).offset(x = 40.dp, y = (-40).dp).background(Color.White.copy(alpha = 0.08f), CircleShape))
                Box(modifier = Modifier.size(100.dp).align(Alignment.BottomStart).offset(x = (-30).dp, y = 20.dp).background(Color.White.copy(alpha = 0.08f), CircleShape))

                // Nội dung
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Tháng $month - $year", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        StatBoxSmall("NGÂN SÁCH", CurrencyUtils.formatMoneyShort(totalLimit), Color.White, Modifier.weight(1f))
                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(alpha = 0.2f)))
                        StatBoxSmall("THỰC CHI", CurrencyUtils.formatMoneyShort(totalSpent), Color(0xFFF59E0B), Modifier.weight(1f))
                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(alpha = 0.2f)))
                        StatBoxSmall("CÒN LẠI", CurrencyUtils.formatMoneyShort(maxOf(0.0, remaining)), statusColor, Modifier.weight(1f))
                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(alpha = 0.2f)))
                        StatBoxSmall("GIAO DỊCH", "$transactionCount", Color.White, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${(progressRatio * 100).toInt()}% đã dùng", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape), color = progressColor, trackColor = Color.White.copy(alpha = 0.2f), strokeCap = StrokeCap.Round)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tổng ngân sách tháng", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    ReportRow("Kế hoạch dự kiến", CurrencyUtils.formatMoney(totalLimit), Color(0xFF3B82F6), 1.0f)
                    ReportRow("Thực tế chi tiêu", CurrencyUtils.formatMoney(totalSpent), statusColor, progress)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Chênh lệch (tiết kiệm hơn KH)", fontSize = 12.sp, color = Color.Gray)
                        val diffColor = if (remaining >= 0) Color(0xFF10B981) else Color.Red
                        Text((if(remaining >= 0) "+" else "") + CurrencyUtils.formatMoney(remaining), fontWeight = FontWeight.Bold, color = diffColor)
                    }
                }
            }

            Text("CHI TIẾT TỪNG DANH MỤC", modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 12.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 32.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Danh mục", modifier = Modifier.weight(1f), fontSize = 11.sp, color = Color.Gray)
                        Text("KH", modifier = Modifier.width(70.dp), textAlign = TextAlign.End, fontSize = 11.sp, color = Color.Gray)
                        Text("Thực tế", modifier = Modifier.width(70.dp), textAlign = TextAlign.End, fontSize = 11.sp, color = Color.Gray)
                        Text("±", modifier = Modifier.width(70.dp), textAlign = TextAlign.End, fontSize = 11.sp, color = Color.Gray)
                    }

                    categories.forEach { category ->
                        val limit = categoryBudgets[category.id] ?: 0.0
                        val spent = spentByCategory[category.id] ?: 0.0
                        if (limit > 0 || spent > 0) {
                            val icon = CategoryUtils.resolveCategoryIcon(category.icon ?: category.name)
                            val color = CategoryUtils.resolveCategoryColor(category.color, Color(0xFFEF4444))
                            CategoryDetailRow(category.name, limit, spent, icon, color)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF1F5F9))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Tổng cộng", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(CurrencyUtils.formatMoneyShort(totalLimit), modifier = Modifier.width(70.dp), textAlign = TextAlign.End, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1A3FBF))
                        Text(CurrencyUtils.formatMoneyShort(totalSpent), modifier = Modifier.width(70.dp), textAlign = TextAlign.End, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF10B981))
                        val finalDiffColor = if (remaining >= 0) Color(0xFF10B981) else Color.Red
                        Text((if(remaining >= 0) "+" else "") + CurrencyUtils.formatMoneyShort(remaining), modifier = Modifier.width(70.dp), textAlign = TextAlign.End, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = finalDiffColor)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryDetailRow(name: String, limit: Double, spent: Double, icon: ImageVector, color: Color) {
    val diff = limit - spent
    val progressRatio = if(limit > 0) (spent/limit).toFloat() else 0f
    val progress = progressRatio.coerceIn(0f, 1f)

    val (progressColor, spentTextColor) = when {
        progressRatio >= 1f -> Color(0xFFEF4444) to Color(0xFFEF4444) // Đỏ (Vượt)
        progressRatio >= 0.8f -> Color(0xFFF59E0B) to Color(0xFFF59E0B) // Cam (Gần vượt)
        else -> Color(0xFF10B981) to Color(0xFF10B981) // Xanh (Tốt)
    }

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                name,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF1E293B)
            )
            Text(
                CurrencyUtils.formatMoneyShort(limit),
                modifier = Modifier.width(70.dp),
                textAlign = TextAlign.End,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                CurrencyUtils.formatMoneyShort(spent),
                modifier = Modifier.width(70.dp),
                textAlign = TextAlign.End,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = spentTextColor
            )
            val diffColor = if(diff >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
            Text(
                (if(diff >= 0) "+" else "") + CurrencyUtils.formatMoneyShort(diff),
                modifier = Modifier.width(70.dp),
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = diffColor
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = progressColor,
            trackColor = Color(0xFFF1F5F9),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun ReportRow(label: String, value: String, color: Color, progress: Float) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = color.copy(alpha = 0.5f), trackColor = Color(0xFFF1F5F9), strokeCap = StrokeCap.Round)
    }
}

@Composable
fun StatBoxSmall(label: String, value: String, color: Color, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
        Text(value, fontSize = 13.sp, color = color, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun StatusChipSquare(count: Int, label: String, bg: Color, fg: Color, modifier: Modifier) {
    Card(modifier = modifier.height(60.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(count.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = fg)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(fg))
                Spacer(modifier = Modifier.width(4.dp))
                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
        }
    }
}

@Composable
fun MonthGridCard(item: MonthSummary, onClick: () -> Unit) {
    val isCurrent = Calendar.getInstance().get(Calendar.MONTH) + 1 == item.month
    val isDisabled = item.status == "CHUA_DEN"

    Card(
        modifier = Modifier.fillMaxWidth().height(110.dp).clickable(enabled = !isDisabled, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDisabled) Color(0xFFF8FAFC) else Color.White),
        border = if (isCurrent) BorderStroke(2.dp, Color(0xFF2E5BFF)) else BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            if (isCurrent) {
                Box(modifier = Modifier.align(Alignment.TopEnd).clip(RoundedCornerShape(6.dp)).background(Color(0xFF2E5BFF)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text("Hiện tại", fontSize = 7.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Text("T${item.month}", fontSize = 10.sp, color = Color.Gray)
                Text("Tháng ${item.month}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if(isDisabled) Color.LightGray else Color.Black)

                if (!isDisabled) {
                    val statusColor = when(item.status) { "VUOT" -> Color.Red; "GAN_VUOT" -> Color(0xFFF59E0B); else -> Color(0xFF10B981) }
                    val progress = if (item.totalLimit > 0) (item.totalSpent / item.totalLimit).toFloat().coerceIn(0f, 1f) else 0f
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape), color = statusColor, trackColor = Color(0xFFF1F5F9))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(CurrencyUtils.formatMoneyShort(item.totalSpent), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor)
                        Text(if(item.status == "DAT") "Đạt" else if(item.status=="VUOT") "Vượt" else "Gần vượt", fontSize = 8.sp, color = statusColor, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("Chưa đến", fontSize = 10.sp, color = Color.LightGray)
                }
            }
        }
    }
}