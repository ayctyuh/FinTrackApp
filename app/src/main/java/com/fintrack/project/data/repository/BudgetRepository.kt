package com.fintrack.project.data.repository

import com.fintrack.project.data.dao.BudgetDao
import com.fintrack.project.data.model.Budget

/**
 * Repository dieu phoi du lieu ngan sach.
 * Phu thuoc: `BudgetDao` (Room).
 * Duoc su dung boi `BudgetViewModel` de truy van va cap nhat du lieu.
 */
class BudgetRepository(
    private val budgetDao: BudgetDao
) {
    /**
     * Them ngan sach.
     * @param budget Doi tuong ngan sach can them.
     * @return ID tu dong sinh cua ban ghi.
     */
    suspend fun insertBudget(budget: Budget): Long {
        return budgetDao.insertBudget(budget)
    }

    /**
     * Cap nhat ngan sach.
     * @param budget Doi tuong ngan sach can cap nhat.
     * @return Khong tra ve.
     */
    suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget)
    }

    /**
     * Xoa ngan sach.
     * @param budget Doi tuong ngan sach can xoa.
     * @return Khong tra ve.
     */
    suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget)
    }

    /**
     * Lay ngan sach theo ID.
     * @param id ID ngan sach.
     * @return Ngan sach tuong ung hoac null.
     */
    suspend fun getBudgetById(id: Int): Budget? {
        return budgetDao.getBudgetById(id)
    }

    /**
     * Lay ngan sach cua nguoi dung.
     * @param userId ID nguoi dung.
     * @return Danh sach ngan sach.
     */
    suspend fun getUserBudgets(userId: Int): List<Budget> {
        return budgetDao.getUserBudgets(userId)
    }

    /**
     * Lay ngan sach theo thang.
     * @param userId ID nguoi dung.
     * @param month Thang (1-12).
     * @param year Nam.
     * @return Danh sach ngan sach trong thang.
     */
    suspend fun getBudgetsByMonth(userId: Int, month: Int, year: Int): List<Budget> {
        return budgetDao.getBudgetsByMonth(userId, month, year)
    }

    /**
     * Lay ngan sach theo thang va danh muc.
     * @param userId ID nguoi dung.
     * @param categoryId ID danh muc.
     * @param month Thang (1-12).
     * @param year Nam.
     * @return Ngan sach danh muc hoac null.
     */
    suspend fun getBudgetByMonthAndCategory(userId: Int, categoryId: Int, month: Int, year: Int): Budget? {
        return budgetDao.getBudgetByMonthAndCategory(userId, categoryId, month, year)
    }

    /**
     * Lay ngan sach tong theo thang.
     * @param userId ID nguoi dung.
     * @param month Thang (1-12).
     * @param year Nam.
     * @return Ngan sach tong hoac null.
     */
    suspend fun getMonthlyBudget(userId: Int, month: Int, year: Int): Budget? {
        return budgetDao.getMonthlyBudget(userId, month, year)
    }

    /**
     * Xoa toan bo ngan sach cua nguoi dung.
     * @param userId ID nguoi dung.
     * @return Khong tra ve.
     */
    suspend fun deleteUserBudgets(userId: Int) {
        budgetDao.deleteUserBudgets(userId)
    }

    /**
     * Xoa ngan sach theo thang.
     * @param userId ID nguoi dung.
     * @param month Thang (1-12).
     * @param year Nam.
     * @return Khong tra ve.
     */
    suspend fun deleteMonthBudgets(userId: Int, month: Int, year: Int) {
        budgetDao.deleteMonthBudgets(userId, month, year)
    }
}

