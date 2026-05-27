package com.fintrack.project.data.dao

import androidx.room.*
import com.fintrack.project.data.model.Transaction
import com.fintrack.project.data.model.TransactionType

@Dao
/**
 * DAO cho bang giao dich.
 * Phu thuoc: Room va entity `Transaction`.
 * Duoc su dung boi `TransactionRepository`.
 */
interface TransactionDao {
    /**
     * Them giao dich.
     * @param transaction Doi tuong giao dich.
     */
    @Insert
    suspend fun insertTransaction(transaction: Transaction): Long

    /**
     * Cap nhat giao dich.
     * @param transaction Doi tuong giao dich.
     */
    @Update
    suspend fun updateTransaction(transaction: Transaction)

    /**
     * Xoa giao dich.
     * @param transaction Doi tuong giao dich.
     */
    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    /**
     * Lay giao dich theo ID.
     * @param id ID giao dich.
     */
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Int): Transaction?

    /**
     * Lay tat ca giao dich theo nguoi dung (dong bo).
     * @param userId ID nguoi dung.
     */
    @Query("SELECT * FROM transactions WHERE userId = :userId")
    fun getAllTransactionsByUser(userId: Int): List<Transaction>

    /**
     * Lay 5 giao dich gan nhat.
     * @param userId ID nguoi dung.
     */
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY transactionDate DESC LIMIT 5")
    suspend fun getRecentTransactions(userId: Int): List<Transaction>

    /**
     * Lay danh sach giao dich cua nguoi dung.
     * @param userId ID nguoi dung.
     */
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY transactionDate DESC")
    suspend fun getUserTransactions(userId: Int): List<Transaction>

    /**
     * Lay giao dich theo loai.
     * @param userId ID nguoi dung.
     * @param type Loai giao dich.
     */
    @Query("SELECT * FROM transactions WHERE userId = :userId AND type = :type ORDER BY transactionDate DESC")
    suspend fun getTransactionsByType(userId: Int, type: TransactionType): List<Transaction>

    /**
     * Lay giao dich theo danh muc.
     * @param userId ID nguoi dung.
     * @param categoryId ID danh muc.
     */
    @Query("SELECT * FROM transactions WHERE userId = :userId AND categoryId = :categoryId ORDER BY transactionDate DESC")
    suspend fun getTransactionsByCategory(userId: Int, categoryId: Int): List<Transaction>

    /**
     * Lay giao dich theo khoang ngay.
     * @param userId ID nguoi dung.
     * @param startDate Bat dau.
     * @param endDate Ket thuc.
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND transactionDate BETWEEN :startDate AND :endDate 
        ORDER BY transactionDate DESC
    """)
    suspend fun getTransactionsByDateRange(userId: Int, startDate: Long, endDate: Long): List<Transaction>

    /**
     * Lay giao dich theo loai va khoang ngay.
     * @param userId ID nguoi dung.
     * @param type Loai giao dich.
     * @param startDate Bat dau.
     * @param endDate Ket thuc.
     */
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

    /**
     * Lay giao dich theo danh muc va khoang ngay.
     * @param userId ID nguoi dung.
     * @param categoryId ID danh muc.
     * @param startDate Bat dau.
     * @param endDate Ket thuc.
     */
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

    /**
     * Tong so tien theo loai.
     * @param userId ID nguoi dung.
     * @param type Loai giao dich.
     */
    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = :type")
    suspend fun getTotalAmount(userId: Int, type: TransactionType): Double?

    /**
     * Tong so tien theo loai va khoang ngay.
     * @param userId ID nguoi dung.
     * @param type Loai giao dich.
     * @param startDate Bat dau.
     * @param endDate Ket thuc.
     */
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

    /**
     * Tong so tien theo danh muc va khoang ngay.
     * @param userId ID nguoi dung.
     * @param categoryId ID danh muc.
     * @param type Loai giao dich.
     * @param startDate Bat dau.
     * @param endDate Ket thuc.
     */
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

    /**
     * Xoa toan bo giao dich cua nguoi dung.
     * @param userId ID nguoi dung.
     */
    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun deleteUserTransactions(userId: Int)
}

