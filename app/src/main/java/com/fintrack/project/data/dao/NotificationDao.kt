package com.fintrack.project.data.dao

import androidx.room.*
import com.fintrack.project.data.model.Notification

@Dao
/**
 * DAO cho bang thong bao.
 * Phu thuoc: Room va entity `Notification`.
 * Duoc su dung boi `NotificationRepository`.
 */
interface NotificationDao {
    /**
     * Them thong bao.
     * @param notification Doi tuong thong bao.
     */
    @Insert
    suspend fun insertNotification(notification: Notification): Long

    /**
     * Cap nhat thong bao.
     * @param notification Doi tuong thong bao.
     */
    @Update
    suspend fun updateNotification(notification: Notification)

    /**
     * Xoa thong bao.
     * @param notification Doi tuong thong bao.
     */
    @Delete
    suspend fun deleteNotification(notification: Notification)

    /**
     * Lay thong bao cua user, sap xep moi nhat.
     * @param userId ID nguoi dung.
     */
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getUserNotifications(userId: Int): List<Notification>

    /**
     * Lay so luong thong bao chua doc.
     * @param userId ID nguoi dung.
     */
    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    suspend fun getUnreadCount(userId: Int): Int

    /**
     * Danh dau tat ca thong bao la da doc.
     * @param userId ID nguoi dung.
     */
    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: Int)

    /**
     * Danh dau thong bao la da doc.
     * @param notificationId ID thong bao.
     */
    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: Int)

    /**
     * Xoa tat ca thong bao cua user.
     * @param userId ID nguoi dung.
     */
    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteUserNotifications(userId: Int)
}