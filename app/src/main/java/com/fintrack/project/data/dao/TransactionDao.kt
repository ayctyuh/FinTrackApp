package com.fintrack.project.data.dao

import androidx.room.*
import com.fintrack.project.data.model.Transaction
import com.fintrack.project.data.model.TransactionType

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Int): Transaction?

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY transactionDate DESC")
    suspend fun getUserTransactions(userId: Int): List<Transaction>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND type = :type ORDER BY transactionDate DESC")
    suspend fun getTransactionsByType(userId: Int, type: TransactionType): List<Transaction>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND categoryId = :categoryId ORDER BY transactionDate DESC")
    suspend fun getTransactionsByCategory(userId: Int, categoryId: Int): List<Transaction>

    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND transactionDate BETWEEN :startDate AND :endDate 
        ORDER BY transactionDate DESC
    """)
    suspend fun getTransactionsByDateRange(userId: Int, startDate: Long, endDate: Long): List<Transaction>

    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND type = :type
        AND transactionDate BETWEEN :startDate AND :endDate 
        ORDER BY transactionDate DESC
    """)
    suspend fun getTransactionsByTypeAndDateRange(
        userId: Int,
        type: TransactionType,
        startDate: Long,
        endDate: Long
    ): List<Transaction>

    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND categoryId = :categoryId
        AND transactionDate BETWEEN :startDate AND :endDate 
        ORDER BY transactionDate DESC
    """)
    suspend fun getTransactionsByCategoryAndDateRange(
        userId: Int,
        categoryId: Int,
        startDate: Long,
        endDate: Long
    ): List<Transaction>

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = :type")
    suspend fun getTotalAmount(userId: Int, type: TransactionType): Double?

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE userId = :userId 
        AND type = :type
        AND transactionDate BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalAmountByDateRange(
        userId: Int,
        type: TransactionType,
        startDate: Long,
        endDate: Long
    ): Double?

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE userId = :userId 
        AND categoryId = :categoryId
        AND type = :type
        AND transactionDate BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalAmountByCategory(
        userId: Int,
        categoryId: Int,
        type: TransactionType,
        startDate: Long,
        endDate: Long
    ): Double?

    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun deleteUserTransactions(userId: Int)
}

