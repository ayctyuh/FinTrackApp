package com.fintrack.project.data.repository

import com.fintrack.project.data.dao.TransactionDao
import com.fintrack.project.data.model.Transaction
import com.fintrack.project.data.model.TransactionType

/**
 * Repository dieu phoi du lieu giao dich.
 * Phu thuoc: `TransactionDao` (Room).
 * Duoc su dung boi `TransactionViewModel`.
 */
class TransactionRepository(
    private val transactionDao: TransactionDao
) {
    /**
     * Them giao dich.
     * @param transaction Doi tuong giao dich.
     */
    suspend fun insertTransaction(transaction: Transaction): Long {
        return transactionDao.insertTransaction(transaction)
    }

    /**
     * Cap nhat giao dich.
     * @param transaction Doi tuong giao dich.
     */
    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    /**
     * Xoa giao dich.
     * @param transaction Doi tuong giao dich.
     */
    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    /**
     * Lay giao dich theo ID.
     * @param id ID giao dich.
     */
    suspend fun getTransactionById(id: Int): Transaction? {
        return transactionDao.getTransactionById(id)
    }

    /**
     * Lay danh sach giao dich cua nguoi dung.
     * @param userId ID nguoi dung.
     */
    suspend fun getUserTransactions(userId: Int): List<Transaction> {
        return transactionDao.getUserTransactions(userId)
    }

    /**
     * Lay giao dich theo loai.
     * @param userId ID nguoi dung.
     * @param type Loai giao dich.
     */
    suspend fun getTransactionsByType(userId: Int, type: TransactionType): List<Transaction> {
        return transactionDao.getTransactionsByType(userId, type)
    }

    /**
     * Lay giao dich theo danh muc.
     * @param userId ID nguoi dung.
     * @param categoryId ID danh muc.
     */
    suspend fun getTransactionsByCategory(userId: Int, categoryId: Int): List<Transaction> {
        return transactionDao.getTransactionsByCategory(userId, categoryId)
    }

    /**
     * Lay giao dich theo khoang ngay.
     * @param userId ID nguoi dung.
     * @param startDate Bat dau.
     * @param endDate Ket thuc.
     */
    suspend fun getTransactionsByDateRange(userId: Int, startDate: Long, endDate: Long): List<Transaction> {
        return transactionDao.getTransactionsByDateRange(userId, startDate, endDate)
    }

    /**
     * Lay giao dich theo loai va khoang ngay.
     * @param userId ID nguoi dung.
     * @param type Loai giao dich.
     * @param startDate Bat dau.
     * @param endDate Ket thuc.
     */
    suspend fun getTransactionsByTypeAndDateRange(
        userId: Int,
        type: TransactionType,
        startDate: Long,
        endDate: Long
    ): List<Transaction> {
        return transactionDao.getTransactionsByTypeAndDateRange(userId, type, startDate, endDate)
    }

    /**
     * Lay giao dich theo danh muc va khoang ngay.
     * @param userId ID nguoi dung.
     * @param categoryId ID danh muc.
     * @param startDate Bat dau.
     * @param endDate Ket thuc.
     */
    suspend fun getTransactionsByCategoryAndDateRange(
        userId: Int,
        categoryId: Int,
        startDate: Long,
        endDate: Long
    ): List<Transaction> {
        return transactionDao.getTransactionsByCategoryAndDateRange(userId, categoryId, startDate, endDate)
    }

    /**
     * Lay tong so tien theo loai.
     * @param userId ID nguoi dung.
     * @param type Loai giao dich.
     */
    suspend fun getTotalAmount(userId: Int, type: TransactionType): Double {
        return transactionDao.getTotalAmount(userId, type) ?: 0.0
    }

    suspend fun getTotalAmountByDateRange(
        userId: Int,
        type: TransactionType,
        startDate: Long,
        endDate: Long
    ): Double {
        return transactionDao.getTotalAmountByDateRange(userId, type, startDate, endDate) ?: 0.0
    }

    suspend fun getTotalAmountByCategory(
        userId: Int,
        categoryId: Int,
        type: TransactionType,
        startDate: Long,
        endDate: Long
    ): Double {
        return transactionDao.getTotalAmountByCategory(userId, categoryId, type, startDate, endDate) ?: 0.0
    }

    /**
     * Xoa toan bo giao dich cua nguoi dung.
     * @param userId ID nguoi dung.
     */
    suspend fun deleteUserTransactions(userId: Int) {
        transactionDao.deleteUserTransactions(userId)
    }
}

