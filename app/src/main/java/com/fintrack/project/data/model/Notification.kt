package com.fintrack.project.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE // Xóa user thì xóa luôn thông báo
        ),
        // Có thể liên kết với Transaction hoặc Budget nếu thông báo đó liên quan đến 1 giao dịch/ngân sách cụ thể
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Budget::class,
            parentColumns = ["id"],
            childColumns = ["budgetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId"),
        Index("transactionId"),
        Index("budgetId")
    ]
)
data class Notification(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val title: String,
    val description: String,
    val type: NotificationType, // Loại thông báo (Cảnh báo, Nhắc nhở, Cập nhật...)
    val isRead: Boolean = false, // Đã đọc chưa (để hiện dấu chấm đỏ)
    val transactionId: Int? = null, // Lưu ID giao dịch nếu có
    val budgetId: Int? = null, // Lưu ID ngân sách nếu có
    val createdAt: Long = System.currentTimeMillis(),
    val message: String
)

// Enum phân loại thông báo
enum class NotificationType {
    REMINDER,       // Nhắc nhở (màu xanh dương)
    UPDATE,         // Cập nhật (màu xanh lá)
    TRANSACTION,    // Giao dịch mới (màu xanh lá)
    BUDGET_ALERT    // Cảnh báo ngân sách (màu đỏ)
}