package com.fintrack.project.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.Layout
import com.fintrack.project.data.database.FinTrackDatabase
import com.fintrack.project.data.model.Category
import com.fintrack.project.data.model.CategoryType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

@Composable
fun CategoryScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onAddClick: () -> Unit,
    onNavigateToAddCategory: () -> Unit,
    onCategoryClick: (Int) -> Unit
) {
    val context = LocalContext.current
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var selectedType by remember { mutableStateOf(CategoryType.EXPENSE) }

    LaunchedEffect(selectedType) {
        withContext(Dispatchers.IO) {
            val sharedPreferences = context.getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getInt("LOGGED_IN_USER_ID", -1)
            if (userId != -1) {
                val db = FinTrackDatabase.getInstance(context)
                categories = db.categoryDao().getCategoriesByType(userId, selectedType)
            }
        }
    }

    Scaffold(
        bottomBar = { ProfileBottomNavigationBar(onHomeClick = onHomeClick, onAddClick = onAddClick, onProfileClick = {}, currentScreen = "Cá nhân") },
        containerColor = Color(0xFFF8FAFC),
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // --- HEADER XANH ĐỒNG BỘ CÓ BONG BÓNG ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF1A3FBF), Color(0xFF3B82F6))))
            ) {
                Box(modifier = Modifier.size(160.dp).align(Alignment.TopEnd).offset(x = 40.dp, y = (-40).dp).background(Color.White.copy(alpha = 0.08f), CircleShape))
                Box(modifier = Modifier.size(100.dp).align(Alignment.BottomStart).offset(x = (-30).dp, y = 20.dp).background(Color.White.copy(alpha = 0.08f), CircleShape))

                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp, bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.Center) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp).size(36.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.ChevronLeft, "Quay lại", tint = Color.White)
                        }
                        Text("Danh mục", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Tabs chọn Thu / Chi
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp).background(Color.White, RoundedCornerShape(12.dp)).padding(4.dp)) {
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (selectedType == CategoryType.EXPENSE) Color(0xFF2E5BFF) else Color.Transparent).clickable { selectedType = CategoryType.EXPENSE }.padding(12.dp), contentAlignment = Alignment.Center) {
                    Text("Chi tiêu", color = if (selectedType == CategoryType.EXPENSE) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (selectedType == CategoryType.INCOME) Color(0xFF2E5BFF) else Color.Transparent).clickable { selectedType = CategoryType.INCOME }.padding(12.dp), contentAlignment = Alignment.Center) {
                    Text("Thu nhập", color = if (selectedType == CategoryType.INCOME) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                }
            }

            Text("CÁC DANH MỤC HIỆN TẠI", modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(categories) { cat ->
                    val catIcon = resolveCategoryIcon(cat.icon ?: cat.name)
                    val bgColor = resolveCategoryColor(cat.color, if (selectedType == CategoryType.EXPENSE) Color(0xFFEF4444) else Color(0xFF10B981))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onCategoryClick(cat.id) }
                    ) {
                        Card(modifier = Modifier.size(72.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Box(modifier = Modifier.size(48.dp).background(bgColor, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Icon(catIcon, null, tint = Color.White)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(cat.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), maxLines = 1, textAlign = TextAlign.Center)
                    }
                }

                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onNavigateToAddCategory() }) {
                        Card(modifier = Modifier.size(72.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Box(modifier = Modifier.size(48.dp).background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Add, null, tint = Color.Gray)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Thêm", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                }
            }
        }
    }
}

// CÁC HÀM PHỤ TRỢ (Đã đổi tên)
fun resolveCategoryColor(colorString: String?, defaultColor: Color): Color {
    return try {
        if (colorString != null && colorString.startsWith("#")) {
            Color(android.graphics.Color.parseColor(colorString))
        } else defaultColor
    } catch (e: Exception) {
        defaultColor
    }
}

fun resolveCategoryIcon(iconKey: String?): ImageVector {
    return when (iconKey) {
        "ic_food" -> Icons.Default.Fastfood
        "ic_school" -> Icons.Default.School
        "ic_hospital" -> Icons.Default.LocalHospital
        "ic_movie" -> Icons.Default.Movie
        "ic_car" -> Icons.Default.DirectionsCar
        "ic_home" -> Icons.Default.Home
        "ic_shopping" -> Icons.Default.ShoppingCart
        "ic_money" -> Icons.Default.AttachMoney
        "ic_gift" -> Icons.Default.CardGiftcard
        "ic_store" -> Icons.Default.Store
        "ic_trending_up" -> Icons.Default.TrendingUp
        "ic_flight" -> Icons.Default.Flight
        "ic_cafe" -> Icons.Default.LocalCafe
        "ic_pets" -> Icons.Default.Pets
        "ic_child" -> Icons.Default.ChildFriendly
        "ic_build" -> Icons.Default.Build
        "ic_computer" -> Icons.Default.Computer
        "ic_checkroom" -> Icons.Default.Checkroom
        "ic_spa" -> Icons.Default.Spa
        "ic_more" -> Icons.Default.MoreHoriz

        "Ăn uống" -> Icons.Default.Fastfood
        "Giáo dục" -> Icons.Default.School
        "Y tế", "Sức khỏe" -> Icons.Default.LocalHospital
        "Giải trí" -> Icons.Default.Movie
        "Giao thông" -> Icons.Default.DirectionsCar
        "Nhà ở" -> Icons.Default.Home
        "Mua sắm" -> Icons.Default.ShoppingCart
        "Lương" -> Icons.Default.AttachMoney
        "Thưởng" -> Icons.Default.CardGiftcard
        "Kinh doanh" -> Icons.Default.Store
        "Đầu tư" -> Icons.Default.TrendingUp
        else -> Icons.Default.MoreHoriz
    }
}

@Composable
fun CategoryFlowLayout(modifier: Modifier = Modifier, spacing: Dp = 8.dp, content: @Composable () -> Unit) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val horizontalSpacing = spacing.roundToPx(); val verticalSpacing = spacing.roundToPx()
        var currentRowWidth = 0; var currentRowHeight = 0; var totalHeight = 0
        val placeables = measurables.map { measurable ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
            if (currentRowWidth + placeable.width > constraints.maxWidth) {
                totalHeight += currentRowHeight + verticalSpacing; currentRowWidth = 0; currentRowHeight = 0
            }
            currentRowWidth += placeable.width + horizontalSpacing; currentRowHeight = max(currentRowHeight, placeable.height)
            placeable
        }
        totalHeight += currentRowHeight
        layout(constraints.maxWidth, totalHeight) {
            var xPosition = 0; var yPosition = 0; var rowHeight = 0
            placeables.forEach { placeable ->
                if (xPosition + placeable.width > constraints.maxWidth) { xPosition = 0; yPosition += rowHeight + verticalSpacing; rowHeight = 0 }
                placeable.placeRelative(xPosition, yPosition)
                xPosition += placeable.width + horizontalSpacing; rowHeight = max(rowHeight, placeable.height)
            }
        }
    }
}