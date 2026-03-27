package com.fintrack.project.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val name: String,
    val icon: String? = null,
    val color: String? = null,
    val type: CategoryType,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class CategoryType {
    INCOME,      // Thu nhập
    EXPENSE      // Chi tiêu
}

// Danh sách danh mục mặc định
val DEFAULT_EXPENSE_CATEGORIES = listOf(
    "Ăn uống",
    "Giáo dục",
    "Y tế",
    "Giải trí",
    "Giao thông",
    "Nhà ở",
    "Mua sắm"
)

val DEFAULT_INCOME_CATEGORIES = listOf(
    "Lương",
    "Thưởng",
    "Kinh doanh",
    "Đầu tư"
)

