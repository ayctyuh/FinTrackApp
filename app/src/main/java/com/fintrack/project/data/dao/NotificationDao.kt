package com.fintrack.project.data.dao

import androidx.room.*
import com.fintrack.project.data.model.Notification

@Dao
interface NotificationDao {
    @Insert
    suspend fun insertNotification(notification: Notification): Long

    @Update
    suspend fun updateNotification(notification: Notification)

    @Delete
    suspend fun deleteNotification(notification: Notification)

    // Lấy tất cả thông báo của 1 user, sắp xếp mới nhất lên đầu
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getUserNotifications(userId: Int): List<Notification>

    // Lấy số lượng thông báo CHƯA ĐỌC
    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    suspend fun getUnreadCount(userId: Int): Int

    // Đánh dấu TẤT CẢ đã đọc
    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: Int)

    // Đánh dấu 1 thông báo là đã đọc
    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: Int)

    // Xóa tất cả thông báo của user
    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteUserNotifications(userId: Int)
}