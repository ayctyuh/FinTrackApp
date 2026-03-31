package com.fintrack.project.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.fintrack.project.data.database.FinTrackDatabase
import com.fintrack.project.data.model.Category
import com.fintrack.project.data.model.CategoryType
import com.fintrack.project.data.model.Notification
import com.fintrack.project.data.model.NotificationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun EditCategoryScreen(
    categoryId: Int,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var categoryToEdit by remember { mutableStateOf<Category?>(null) }

    var catName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(CategoryType.EXPENSE) }
    var selectedColor by remember { mutableStateOf("#2E5BFF") }
    var selectedIcon by remember { mutableStateOf("ic_food") }

    val colorOptions = listOf("#2E5BFF", "#10B981", "#EF4444", "#F59E0B", "#8B5CF6", "#EC4899")
    val defaultIconOptions = listOf("ic_food", "ic_home", "ic_car", "ic_movie", "ic_shopping", "ic_hospital", "ic_school")
    val extendedIconOptions = listOf("ic_food", "ic_home", "ic_car", "ic_movie", "ic_shopping", "ic_hospital", "ic_school", "ic_money", "ic_gift", "ic_store", "ic_trending_up", "ic_flight", "ic_cafe", "ic_pets", "ic_child", "ic_build", "ic_computer", "ic_checkroom", "ic_spa")

    var showSuccess by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(categoryId) {
        withContext(Dispatchers.IO) {
            val db = FinTrackDatabase.getInstance(context)
            categoryToEdit = db.categoryDao().getCategoryById(categoryId)
            categoryToEdit?.let {
                catName = it.name
                selectedType = it.type
                selectedColor = it.color ?: "#2E5BFF"
                selectedIcon = it.icon ?: "ic_food"
            }
            isLoading = false
        }
    }

    fun updateCategory() {
        if (catName.isBlank()) {
            Toast.makeText(context, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            withContext(Dispatchers.IO) {
                val db = FinTrackDatabase.getInstance(context)
                categoryToEdit?.let {
                    db.categoryDao().updateCategory(
                        it.copy(name = catName, icon = selectedIcon, color = selectedColor, type = selectedType)
                    )
                }
            }
            showSuccess = true
        }
    }

    fun deleteCategory() {
        scope.launch {
            withContext(Dispatchers.IO) {
                val db = FinTrackDatabase.getInstance(context)
                categoryToEdit?.let { cat ->
                    // 1. Xóa danh mục
                    db.categoryDao().deleteCategory(cat)

                    // 2. Thêm thông báo xóa
                    db.notificationDao().insertNotification(
                        Notification(
                            userId = cat.userId,
                            title = "Đã xóa danh mục",
                            description = "Danh mục ${cat.name} đã bị xóa",
                            message = "Danh mục '${cat.name}' của bạn đã được xóa thành công khỏi hệ thống.",
                            type = NotificationType.BUDGET_ALERT, // Hiện màu đỏ cảnh báo nhẹ
                            createdAt = System.currentTimeMillis(),
                            isRead = false
                        )
                    )
                }
            }
            Toast.makeText(context, "Đã xóa danh mục", Toast.LENGTH_SHORT).show()
            onBackClick()
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    if (showSuccess) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(80.dp).background(Color(0xFF10B981), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Cập nhật thành công!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth(0.8f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E5BFF))) {
                    Text("Quay về", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        Scaffold(
            containerColor = Color(0xFFF8FAFC),
            contentWindowInsets = WindowInsets(0.dp)
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(bottom = paddingValues.calculateBottomPadding()).verticalScroll(rememberScrollState())) {

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
                        Spacer(modifier = Modifier.height(5.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.Center) {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp).size(36.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(Icons.Default.ChevronLeft, "Quay lại", tint = Color.White)
                            }
                            Text("Sửa Danh Mục", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(80.dp).background(resolveCategoryColor(selectedColor, Color.Gray), RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                            Icon(resolveCategoryIcon(selectedIcon), null, tint = Color.White, modifier = Modifier.size(40.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))

                    Text("LOẠI DANH MỤC", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedType = CategoryType.EXPENSE }) {
                            RadioButton(selected = selectedType == CategoryType.EXPENSE, onClick = { selectedType = CategoryType.EXPENSE })
                            Text("Chi tiêu", fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.width(24.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedType = CategoryType.INCOME }) {
                            RadioButton(selected = selectedType == CategoryType.INCOME, onClick = { selectedType = CategoryType.INCOME })
                            Text("Thu nhập", fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("TÊN DANH MỤC", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = catName, onValueChange = { catName = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("MÀU SẮC", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        colorOptions.forEach { colorHex ->
                            val color = resolveCategoryColor(colorHex, Color.Gray)
                            val isSelected = selectedColor == colorHex
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(color).clickable { selectedColor = colorHex }.border(if (isSelected) 3.dp else 0.dp, if (isSelected) Color.Black.copy(alpha=0.3f) else Color.Transparent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("BIỂU TƯỢNG", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryFlowLayout(spacing = 16.dp) {
                        defaultIconOptions.forEach { iconName ->
                            val isSelected = selectedIcon == iconName
                            Box(
                                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(if (isSelected) resolveCategoryColor(selectedColor, Color.Gray) else Color(0xFFF1F5F9)).clickable { selectedIcon = iconName },
                                contentAlignment = Alignment.Center
                            ) { Icon(resolveCategoryIcon(iconName), null, tint = if (isSelected) Color.White else Color.Gray) }
                        }

                        Box(
                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF1F5F9)).clickable { showIconPicker = true },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.MoreHoriz, null, tint = Color.Gray) }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Button(
                        onClick = { updateCategory() }, modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E5BFF)), shape = RoundedCornerShape(16.dp)
                    ) { Text("Lưu thay đổi", fontSize = 16.sp, fontWeight = FontWeight.Bold) }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { deleteCategory() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444))
                    ) {
                        Icon(Icons.Default.DeleteOutline, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Xóa danh mục", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (showIconPicker) {
            Dialog(onDismissRequest = { showIconPicker = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.7f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Chọn Biểu Tượng", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                            CategoryFlowLayout(spacing = 12.dp) {
                                extendedIconOptions.forEach { iconName ->
                                    val isSelected = selectedIcon == iconName
                                    Box(
                                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(if (isSelected) resolveCategoryColor(selectedColor, Color.Gray) else Color(0xFFF1F5F9)).clickable {
                                            selectedIcon = iconName; showIconPicker = false
                                        },
                                        contentAlignment = Alignment.Center
                                    ) { Icon(resolveCategoryIcon(iconName), null, tint = if (isSelected) Color.White else Color.Gray) }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = { showIconPicker = false }, modifier = Modifier.align(Alignment.End)) {
                            Text("Đóng", color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}