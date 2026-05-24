package com.fintrack.project.ui.screens

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fintrack.project.presentation.viewmodel.StatisticsViewModel
import com.fintrack.project.presentation.viewmodel.WeeklyComparisonData
import com.fintrack.project.utils.CurrencyUtils
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChiTietBieuDoScreen(
    userId: Int,
    viewModel: StatisticsViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMonthPicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(userId) {
        viewModel.loadMonthlyComparison(userId, uiState.monthA, uiState.yearA, uiState.monthB, uiState.yearB)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Chi tiết biểu đồ", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.2f))) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF1565C0))
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Month Selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonthSelectorBox(
                    label = "Mốc A",
                    subLabel = "Tháng bắt đầu",
                    month = uiState.monthA,
                    year = uiState.yearA,
                    color = Color(0xFF60A5FA),
                    modifier = Modifier.weight(1f),
                    onClick = { showMonthPicker = true }
                )
                Text("VS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                MonthSelectorBox(
                    label = "Mốc B",
                    subLabel = "Tháng kết thúc",
                    month = uiState.monthB,
                    year = uiState.yearB,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    onClick = { showMonthPicker = true }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Comparison Chart Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text("So sánh chi tiêu", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF1E293B))
                            Text("Tháng ${uiState.monthB} vs Tháng ${uiState.monthA} · ${uiState.yearB}", fontSize = 13.sp, color = Color.Gray)
                        }
                        // Legend
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                LegendItem("T${uiState.monthB}", Color(0xFF10B981))
                                LegendItem("T${uiState.monthA}", Color(0xFF60A5FA))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                LegendItem("Chi T${uiState.monthB}", Color(0xFFEF4444))
                                LegendItem("Chi T${uiState.monthA}", Color(0xFFF97316))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Box(modifier = Modifier.height(260.dp).fillMaxWidth()) {
                        ComparisonBarChart(uiState.weeklyComparison)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Summary Details
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    title = "THU NHẬP",
                    monthA = uiState.monthA,
                    valueA = uiState.totalIncomeA,
                    monthB = uiState.monthB,
                    valueB = uiState.totalIncomeB,
                    modifier = Modifier.weight(1f),
                    colorA = Color(0xFF60A5FA),
                    colorB = Color(0xFF10B981)
                )
                SummaryCard(
                    title = "CHI TIÊU",
                    monthA = uiState.monthA,
                    valueA = uiState.totalExpenseA,
                    monthB = uiState.monthB,
                    valueB = uiState.totalExpenseB,
                    modifier = Modifier.weight(1f),
                    colorA = Color(0xFFF97316),
                    colorB = Color(0xFFEF4444),
                    isExpense = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Variation
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0)),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Chênh lệch tháng", color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text("Tháng ${uiState.monthB} vs Tháng ${uiState.monthA}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Row(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        VariationItem("Thu", uiState.totalIncomeA, uiState.totalIncomeB)
                        Divider(modifier = Modifier.fillMaxHeight().width(1.dp).padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.2f))
                        VariationItem("Chi", uiState.totalExpenseA, uiState.totalExpenseB)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showMonthPicker) {
        MonthComparisonPicker(
            initialMonthA = uiState.monthA,
            initialYearA = uiState.yearA,
            initialMonthB = uiState.monthB,
            initialYearB = uiState.yearB,
            onDismiss = { showMonthPicker = false },
            onConfirm = { mA, yA, mB, yB ->
                viewModel.loadMonthlyComparison(userId, mA, yA, mB, yB)
                showMonthPicker = false
            }
        )
    }
}

@Composable
fun MonthSelectorBox(label: String, subLabel: String, month: Int, year: Int, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(modifier = Modifier.border(1.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(20.dp)).padding(16.dp)) {
            Column {
                Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                    Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
                Text(subLabel, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Tháng $month/$year", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.CalendarMonth, null, tint = color.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Text(label, fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SummaryCard(title: String, monthA: Int, valueA: Double, monthB: Int, valueB: Double, modifier: Modifier, colorA: Color, colorB: Color, isExpense: Boolean = false) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            SummaryRow("T$monthB", valueB, colorB)
            Spacer(modifier = Modifier.height(8.dp))
            SummaryRow("T$monthA", valueA, colorA)
            
            Divider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.8.dp, color = Color(0xFFF1F5F9))
            
            Text("BIẾN ĐỘNG", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            val diff = valueB - valueA
            val percent = if (valueA != 0.0) (diff / valueA) * 100 else 0.0
            val sign = if (diff >= 0) "↑" else "↓"
            val isPositiveTrend = if (isExpense) diff <= 0 else diff >= 0
            val displayColor = if (isPositiveTrend) Color(0xFF10B981) else Color(0xFFEF4444)
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Text(sign, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = displayColor)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${if (diff >= 0) "+" else ""}${CurrencyUtils.formatMoneyShort(diff)}",
                    fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = displayColor
                )
                Text(
                    " (${String.format("%.1f", Math.abs(percent))}%)",
                    fontSize = 11.sp, fontWeight = FontWeight.Medium, color = displayColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: Double, dotColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(dotColor))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.weight(1f))
        Text(CurrencyUtils.formatMoneyShort(value), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
    }
}

@Composable
fun VariationItem(label: String, valA: Double, valB: Double) {
    val diff = valB - valA
    val percent = if (valA != 0.0) (diff / valA) * 100 else 0.0
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            val isUp = diff >= 0
            Text(if (isUp) "↑" else "↓", color = if (isUp) Color(0xFF4ADE80) else Color(0xFFFCA5A5), fontWeight = FontWeight.Black)
            Text("${String.format("%.1f", Math.abs(percent))}%", color = if (isUp) Color(0xFF4ADE80) else Color(0xFFFCA5A5), fontWeight = FontWeight.Black, fontSize = 17.sp)
        }
    }
}

@Composable
fun ComparisonBarChart(data: List<WeeklyComparisonData>) {
    if (data.isEmpty()) return
    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                setPinchZoom(false)
                setDrawBarShadow(false)
                setDrawGridBackground(false)
                
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    setCenterAxisLabels(true)
                    textColor = android.graphics.Color.parseColor("#94A3B8")
                    textSize = 10f
                    axisLineColor = android.graphics.Color.parseColor("#E2E8F0")
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return data.getOrNull(value.toInt())?.weekLabel ?: ""
                        }
                    }
                }
                
                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = android.graphics.Color.parseColor("#F1F5F9")
                    gridLineWidth = 1f
                    axisMinimum = 0f
                    axisLineColor = android.graphics.Color.TRANSPARENT
                    textColor = android.graphics.Color.parseColor("#94A3B8")
                    textSize = 10f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return when {
                                value >= 1000000 -> "${(value / 1000000).toInt()}M"
                                value >= 1000 -> "${(value / 1000).toInt()}K"
                                else -> value.toInt().toString()
                            }
                        }
                    }
                }
                axisRight.isEnabled = false
                legend.isEnabled = false
                setScaleEnabled(false)
                setExtraOffsets(0f, 15f, 0f, 10f)
            }
        },
        update = { chart ->
            val entriesIB = data.mapIndexed { i, d -> BarEntry(i.toFloat(), d.incomeB.toFloat()) }
            val entriesIA = data.mapIndexed { i, d -> BarEntry(i.toFloat(), d.incomeA.toFloat()) }
            val entriesEB = data.mapIndexed { i, d -> BarEntry(i.toFloat(), d.expenseB.toFloat()) }
            val entriesEA = data.mapIndexed { i, d -> BarEntry(i.toFloat(), d.expenseA.toFloat()) }

            val setIB = BarDataSet(entriesIB, "InB").apply { color = Color(0xFF10B981).toArgb(); setDrawValues(false) }
            val setIA = BarDataSet(entriesIA, "InA").apply { color = Color(0xFF60A5FA).toArgb(); setDrawValues(false) }
            val setEB = BarDataSet(entriesEB, "ExB").apply { color = Color(0xFFEF4444).toArgb(); setDrawValues(false) }
            val setEA = BarDataSet(entriesEA, "ExA").apply { color = Color(0xFFF97316).toArgb(); setDrawValues(false) }

            val barData = BarData(setIB, setIA, setEB, setEA)
            val groupSpace = 0.35f
            val barSpace = 0.02f
            val barWidth = 0.14f
            
            barData.barWidth = barWidth
            chart.data = barData
            chart.groupBars(0f, groupSpace, barSpace)
            
            chart.xAxis.axisMinimum = 0f
            chart.xAxis.axisMaximum = chart.barData.getGroupWidth(groupSpace, barSpace) * data.size
            chart.xAxis.labelCount = data.size
            
            // Apply Custom Renderer for rounded corners and Gradient
            chart.renderer = BeautifulBarChartRenderer(chart, chart.animator, chart.viewPortHandler, 18f)
            
            chart.animateY(1200)
            chart.invalidate()
        },
        modifier = Modifier.fillMaxSize()
    )
}

class BeautifulBarChartRenderer(
    chart: BarChart,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler,
    private val mRadius: Float
) : BarChartRenderer(chart, animator, viewPortHandler) {

    private val mBarRect = RectF()

    override fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {
        val trans = mChart.getTransformer(dataSet.axisDependency)
        mRenderPaint.style = Paint.Style.FILL
        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY

        val buffer = mBarBuffers[index]
        buffer.setPhases(phaseX, phaseY)
        buffer.setDataSet(index)
        buffer.setInverted(mChart.isInverted(dataSet.axisDependency))
        buffer.setBarWidth(mChart.barData.barWidth)
        buffer.feed(dataSet)

        trans.pointValuesToPixel(buffer.buffer)

        var j = 0
        while (j < buffer.size()) {
            if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) {
                j += 4
                continue
            }
            if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j])) break

            val color = dataSet.getColor(j / 4)
            mBarRect.set(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2], buffer.buffer[j + 3])

            // Create Gradient Shader
            val gradient = LinearGradient(
                mBarRect.left, mBarRect.top,
                mBarRect.left, mBarRect.bottom,
                color, adjustAlpha(color, 0.6f),
                Shader.TileMode.CLAMP
            )
            mRenderPaint.shader = gradient

            // Draw rounded top rect
            c.drawRoundRect(mBarRect, mRadius, mRadius, mRenderPaint)
            
            // Draw flat bottom
            if (mBarRect.height() > mRadius) {
                val bottomFlatRect = RectF(mBarRect.left, mBarRect.bottom - mRadius, mBarRect.right, mBarRect.bottom)
                c.drawRect(bottomFlatRect, mRenderPaint)
            }

            j += 4
        }
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(android.graphics.Color.alpha(color) * factor)
        val red = android.graphics.Color.red(color)
        val green = android.graphics.Color.green(color)
        val blue = android.graphics.Color.blue(color)
        return android.graphics.Color.argb(alpha, red, green, blue)
    }
}

@Composable
fun MonthComparisonPicker(
    initialMonthA: Int, initialYearA: Int,
    initialMonthB: Int, initialYearB: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Int, Int) -> Unit
) {
    var selectedMoc by remember { mutableStateOf("B") }
    var tempMonthA by remember { mutableStateOf(initialMonthA) }
    var tempYearA by remember { mutableStateOf(initialYearA) }
    var tempMonthB by remember { mutableStateOf(initialMonthB) }
    var tempYearB by remember { mutableStateOf(initialYearB) }
    
    var currentDisplayYear by remember { mutableStateOf(tempYearB) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).clickable(enabled = false) {}) {
            Card(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Chọn tháng", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
                        IconButton(onClick = onDismiss, modifier = Modifier.background(Color(0xFFF1F5F9), CircleShape).size(40.dp)) { 
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp), tint = Color.Gray) 
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(4.dp)
                    ) {
                        MocToggleButton(
                            label = "Chọn Mốc A",
                            isSelected = selectedMoc == "A",
                            color = Color(0xFF1565C0),
                            onClick = { selectedMoc = "A"; currentDisplayYear = tempYearA },
                            modifier = Modifier.weight(1f)
                        )
                        MocToggleButton(
                            label = "Chọn Mốc B",
                            isSelected = selectedMoc == "B",
                            color = Color(0xFF10B981),
                            onClick = { selectedMoc = "B"; currentDisplayYear = tempYearB },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { currentDisplayYear-- }, modifier = Modifier.background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)).size(36.dp)) { 
                            Icon(Icons.Default.ChevronLeft, null, tint = Color.DarkGray) 
                        }
                        Text(currentDisplayYear.toString(), fontSize = 24.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 28.dp), color = Color(0xFF1E293B))
                        IconButton(onClick = { currentDisplayYear++ }, modifier = Modifier.background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)).size(36.dp)) { 
                            Icon(Icons.Default.ChevronRight, null, tint = Color.DarkGray) 
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    LazyVerticalGrid(columns = GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.heightIn(max = 320.dp)) {
                        items((1..12).toList()) { month ->
                            val isSelectedA = tempMonthA == month && tempYearA == currentDisplayYear
                            val isSelectedB = tempMonthB == month && tempYearB == currentDisplayYear
                            
                            val isCurrentSelected = if (selectedMoc == "A") isSelectedA else isSelectedB
                            val color = if (selectedMoc == "A") Color(0xFF1565C0) else Color(0xFF10B981)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isCurrentSelected) color else Color(0xFFF8FAFC))
                                    .clickable {
                                        if (selectedMoc == "A") { tempMonthA = month; tempYearA = currentDisplayYear }
                                        else { tempMonthB = month; tempYearB = currentDisplayYear }
                                    }
                                    .padding(vertical = 18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Tháng $month", color = if (isCurrentSelected) Color.White else Color(0xFF1E293B), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Text(currentDisplayYear.toString(), color = if (isCurrentSelected) Color.White.copy(0.7f) else Color(0xFF94A3B8), fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SelectionInfoBoxDialog("Mốc A", "Tháng $tempMonthA/$tempYearA", Color(0xFF1565C0).copy(alpha = 0.05f), Color(0xFF1565C0), Modifier.weight(1f))
                        SelectionInfoBoxDialog("Mốc B", "Tháng $tempMonthB/$tempYearB", Color(0xFF10B981).copy(alpha = 0.05f), Color(0xFF10B981), Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = { onConfirm(tempMonthA, tempYearA, tempMonthB, tempYearB) },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                    ) {
                        Text("Xác nhận lựa chọn", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MocToggleButton(label: String, isSelected: Boolean, color: Color, onClick: () -> Unit, modifier: Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Color.White else Color.Transparent),
        elevation = if (isSelected) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null,
        modifier = modifier.height(44.dp),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (isSelected) color else Color.LightGray))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = if (isSelected) color else Color.Gray, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium)
        }
    }
}

@Composable
fun SelectionInfoBoxDialog(label: String, date: String, bgColor: Color, textColor: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Box(modifier = Modifier.border(1.5.dp, textColor.copy(alpha = 0.2f), RoundedCornerShape(18.dp)).padding(14.dp)) {
            Column {
                Text(label, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                Text(date, color = Color(0xFF1E293B), fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
