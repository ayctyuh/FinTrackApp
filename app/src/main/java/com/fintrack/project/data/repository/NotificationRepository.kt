package com.fintrack.project.data.repository

import com.fintrack.project.data.dao.NotificationDao
import com.fintrack.project.data.model.Notification

/**
 * Repository dieu phoi du lieu thong bao.
 * Phu thuoc: `NotificationDao` (Room).
 * Duoc su dung boi cac ViewModel/man hinh thong bao.
 */
class NotificationRepository(
    private val notificationDao: NotificationDao
) {
    /**
     * Them thong bao.
     * @param notification Doi tuong thong bao.
     */
    suspend fun insertNotification(notification: Notification): Long {
        return notificationDao.insertNotification(notification)
    }

    /**
     * Cap nhat thong bao.
     * @param notification Doi tuong thong bao.
     */
    suspend fun updateNotification(notification: Notification) {
        notificationDao.updateNotification(notification)
    }

    /**
     * Xoa thong bao.
     * @param notification Doi tuong thong bao.
     */
    suspend fun deleteNotification(notification: Notification) {
        notificationDao.deleteNotification(notification)
    }

    /**
     * Lay thong bao cua user.
     * @param userId ID nguoi dung.
     */
    suspend fun getUserNotifications(userId: Int): List<Notification> {
        return notificationDao.getUserNotifications(userId)
    }

    /**
     * Lay so thong bao chua doc.
     * @param userId ID nguoi dung.
     */
    suspend fun getUnreadCount(userId: Int): Int {
        return notificationDao.getUnreadCount(userId)
    }

    /**
     * Danh dau tat ca la da doc.
     * @param userId ID nguoi dung.
     */
    suspend fun markAllAsRead(userId: Int) {
        notificationDao.markAllAsRead(userId)
    }

    /**
     * Danh dau mot thong bao la da doc.
     * @param notificationId ID thong bao.
     */
    suspend fun markAsRead(notificationId: Int) {
        notificationDao.markAsRead(notificationId)
    }
}