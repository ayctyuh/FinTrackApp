package com.fintrack.project.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity( // Room: khai bao entity de map data class voi bang SQLite
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("userId"),
        Index("categoryId")
    ]
)
/**
 * Entity ngan sach theo thang/danh muc trong Room.
 * Phu thuoc: Room annotations va cac entity `User`, `Category`.
 * Duoc su dung boi `BudgetDao` va `FinTrackDatabase` de luu/lay ngan sach.
 */
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val categoryId: Int? = null,  // null nếu là ngân sách tổng hợp
    val limitAmount: Double,
    val month: Int,              // Tháng (1-12)
    val year: Int,               // Năm
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val alertThreshold: Double = 80.0  // Cảnh báo khi đạt (%) - mặc định 80%
)

