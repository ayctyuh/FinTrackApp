package com.fintrack.project.data.dao

import androidx.room.*
import com.fintrack.project.data.model.Budget

@Dao
interface BudgetDao {
    @Insert
    suspend fun insertBudget(budget: Budget): Long

    @Update
    suspend fun updateBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: Int): Budget?

    @Query("SELECT * FROM budgets WHERE userId = :userId")
    suspend fun getUserBudgets(userId: Int): List<Budget>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month AND year = :year")
    suspend fun getBudgetsByMonth(userId: Int, month: Int, year: Int): List<Budget>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND categoryId = :categoryId AND month = :month AND year = :year")
    suspend fun getBudgetByMonthAndCategory(userId: Int, categoryId: Int, month: Int, year: Int): Budget?

    @Query("SELECT * FROM budgets WHERE userId = :userId AND categoryId IS NULL AND month = :month AND year = :year")
    suspend fun getMonthlyBudget(userId: Int, month: Int, year: Int): Budget?

    @Query("DELETE FROM budgets WHERE userId = :userId")
    suspend fun deleteUserBudgets(userId: Int)

    @Query("DELETE FROM budgets WHERE userId = :userId AND month = :month AND year = :year")
    suspend fun deleteMonthBudgets(userId: Int, month: Int, year: Int)
}

