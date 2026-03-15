package com.fintrack.project.data.repository

import com.fintrack.project.data.dao.BudgetDao
import com.fintrack.project.data.model.Budget

class BudgetRepository(
    private val budgetDao: BudgetDao
) {
    suspend fun insertBudget(budget: Budget): Long {
        return budgetDao.insertBudget(budget)
    }

    suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget)
    }

    suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget)
    }

    suspend fun getBudgetById(id: Int): Budget? {
        return budgetDao.getBudgetById(id)
    }

    suspend fun getUserBudgets(userId: Int): List<Budget> {
        return budgetDao.getUserBudgets(userId)
    }

    suspend fun getBudgetsByMonth(userId: Int, month: Int, year: Int): List<Budget> {
        return budgetDao.getBudgetsByMonth(userId, month, year)
    }

    suspend fun getBudgetByMonthAndCategory(userId: Int, categoryId: Int, month: Int, year: Int): Budget? {
        return budgetDao.getBudgetByMonthAndCategory(userId, categoryId, month, year)
    }

    suspend fun getMonthlyBudget(userId: Int, month: Int, year: Int): Budget? {
        return budgetDao.getMonthlyBudget(userId, month, year)
    }

    suspend fun deleteUserBudgets(userId: Int) {
        budgetDao.deleteUserBudgets(userId)
    }

    suspend fun deleteMonthBudgets(userId: Int, month: Int, year: Int) {
        budgetDao.deleteMonthBudgets(userId, month, year)
    }
}

