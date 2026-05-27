package com.fintrack.project.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity( // Room: khai bao entity de map data class voi bang SQLite
    tableName = "transactions",
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
        Index("categoryId"),
        Index("transactionDate")
    ]
)
/**
 * Entity giao dich thu/chi trong Room.
 * Phu thuoc: Room annotations va entity `User`, `Category`.
 * Duoc su dung boi `TransactionDao` va `FinTrackDatabase`.
 */
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val categoryId: Int? = null,
    val amount: Double,
    val type: TransactionType,
    val description: String? = null,
    val transactionDate: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val sourceBank: String? = null  // Nguồn nhập (ngân hàng, dịch vụ thanh toán)
)

/**
 * Loai giao dich.
 * Duoc su dung boi `Transaction` va cac truy van thong ke.
 */
enum class TransactionType {
    INCOME,      // Thu nhập
    EXPENSE      // Chi tiêu
}

