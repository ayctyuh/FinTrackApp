package com.fintrack.project.data.repository

import com.fintrack.project.data.dao.TransactionDao
import com.fintrack.project.data.model.Transaction
import com.fintrack.project.data.model.TransactionType

class TransactionRepository(
    private val transactionDao: TransactionDao
) {
    suspend fun insertTransaction(transaction: Transaction): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun getTransactionById(id: Int): Transaction? {
        return transactionDao.getTransactionById(id)
    }

    suspend fun getUserTransactions(userId: Int): List<Transaction> {
        return transactionDao.getUserTransactions(userId)
    }

    suspend fun getTransactionsByType(userId: Int, type: TransactionType): List<Transaction> {
        return transactionDao.getTransactionsByType(userId, type)
    }

    suspend fun getTransactionsByCategory(userId: Int, categoryId: Int): List<Transaction> {
        return transactionDao.getTransactionsByCategory(userId, categoryId)
    }

    suspend fun getTransactionsByDateRange(userId: Int, startDate: Long, endDate: Long): List<Transaction> {
        return transactionDao.getTransactionsByDateRange(userId, startDate, endDate)
    }

    suspend fun getTransactionsByTypeAndDateRange(
        userId: Int,
        type: TransactionType,
        startDate: Long,
        endDate: Long
    ): List<Transaction> {
        return transactionDao.getTransactionsByTypeAndDateRange(userId, type, startDate, endDate)
    }

    suspend fun getTransactionsByCategoryAndDateRange(
        userId: Int,
        categoryId: Int,
        startDate: Long,
        endDate: Long
    ): List<Transaction> {
        return transactionDao.getTransactionsByCategoryAndDateRange(userId, categoryId, startDate, endDate)
    }

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

    suspend fun deleteUserTransactions(userId: Int) {
        transactionDao.deleteUserTransactions(userId)
    }
}

