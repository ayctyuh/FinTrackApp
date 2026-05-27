package com.fintrack.project.data.dao

import androidx.room.*
import com.fintrack.project.data.model.Budget

@Dao // Room: dinh nghia DAO de thuc thi truy van SQLite qua annotation
/**
 * DAO cho bang ngan sach.
 * Phu thuoc: Room va entity `Budget`.
 * Duoc su dung boi `BudgetRepository` de truy van du lieu.
 */
interface BudgetDao {
    /**
     * Them ngan sach.
     * @param budget Doi tuong ngan sach can them.
     * @return ID tu dong sinh cua ban ghi.
     */
    @Insert
    suspend fun insertBudget(budget: Budget): Long

    /**
     * Cap nhat ngan sach.
     * @param budget Doi tuong ngan sach can cap nhat.
     * @return Khong tra ve.
     */
    @Update
    suspend fun updateBudget(budget: Budget)

    /**
     * Xoa ngan sach.
     * @param budget Doi tuong ngan sach can xoa.
     * @return Khong tra ve.
     */
    @Delete
    suspend fun deleteBudget(budget: Budget)

    /**
     * Lay ngan sach theo ID.
     * @param id ID ngan sach.
     * @return Ngan sach tuong ung hoac null neu khong co.
     */
    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: Int): Budget?

    /**
     * Lay danh sach ngan sach cua nguoi dung.
     * @param userId ID nguoi dung.
     * @return Danh sach ngan sach.
     */
    @Query("SELECT * FROM budgets WHERE userId = :userId")
    suspend fun getUserBudgets(userId: Int): List<Budget>

    /**
     * Lay ngan sach theo thang.
     * @param userId ID nguoi dung.
     * @param month Thang (1-12).
     * @param year Nam.
     * @return Danh sach ngan sach cua thang.
     */
    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month AND year = :year")
    suspend fun getBudgetsByMonth(userId: Int, month: Int, year: Int): List<Budget>

    /**
     * Lay ngan sach theo thang va danh muc.
     * @param userId ID nguoi dung.
     * @param categoryId ID danh muc.
     * @param month Thang (1-12).
     * @param year Nam.
     * @return Ngan sach danh muc hoac null neu khong co.
     */
    @Query("SELECT * FROM budgets WHERE userId = :userId AND categoryId = :categoryId AND month = :month AND year = :year")
    suspend fun getBudgetByMonthAndCategory(userId: Int, categoryId: Int, month: Int, year: Int): Budget?

    /**
     * Lay ngan sach tong (khong theo danh muc) cua thang.
     * @param userId ID nguoi dung.
     * @param month Thang (1-12).
     * @param year Nam.
     * @return Ngan sach tong hoac null.
     */
    @Query("SELECT * FROM budgets WHERE userId = :userId AND categoryId IS NULL AND month = :month AND year = :year")
    suspend fun getMonthlyBudget(userId: Int, month: Int, year: Int): Budget?

    /**
     * Xoa toan bo ngan sach cua nguoi dung.
     * @param userId ID nguoi dung.
     * @return Khong tra ve.
     */
    @Query("DELETE FROM budgets WHERE userId = :userId")
    suspend fun deleteUserBudgets(userId: Int)

    /**
     * Xoa ngan sach theo thang.
     * @param userId ID nguoi dung.
     * @param month Thang (1-12).
     * @param year Nam.
     * @return Khong tra ve.
     */
    @Query("DELETE FROM budgets WHERE userId = :userId AND month = :month AND year = :year")
    suspend fun deleteMonthBudgets(userId: Int, month: Int, year: Int)
}

