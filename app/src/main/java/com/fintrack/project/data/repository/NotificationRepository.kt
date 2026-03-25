package com.fintrack.project.data.repository

import com.fintrack.project.data.dao.NotificationDao
import com.fintrack.project.data.model.Notification

class NotificationRepository(
    private val notificationDao: NotificationDao
) {
    suspend fun insertNotification(notification: Notification): Long {
        return notificationDao.insertNotification(notification)
    }

    suspend fun updateNotification(notification: Notification) {
        notificationDao.updateNotification(notification)
    }

    suspend fun deleteNotification(notification: Notification) {
        notificationDao.deleteNotification(notification)
    }

    suspend fun getUserNotifications(userId: Int): List<Notification> {
        return notificationDao.getUserNotifications(userId)
    }

    suspend fun getUnreadCount(userId: Int): Int {
        return notificationDao.getUnreadCount(userId)
    }

    suspend fun markAllAsRead(userId: Int) {
        notificationDao.markAllAsRead(userId)
    }

    suspend fun markAsRead(notificationId: Int) {
        notificationDao.markAsRead(notificationId)
    }
}