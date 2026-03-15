package com.fintrack.project.di

import android.content.Context
import com.fintrack.project.data.database.FinTrackDatabase
import com.fintrack.project.data.repository.BudgetRepository
import com.fintrack.project.data.repository.CategoryRepository
import com.fintrack.project.data.repository.TransactionRepository
import com.fintrack.project.data.repository.UserRepository

/**
 * Service Locator for Dependency Injection (Manual DI)
 * Sẽ upgrade sang Hilt sau khi base setup ổn định
 */
object ServiceLocator {
    private var database: FinTrackDatabase? = null
    
    private var userRepository: UserRepository? = null
    private var categoryRepository: CategoryRepository? = null
    private var transactionRepository: TransactionRepository? = null
    private var budgetRepository: BudgetRepository? = null

    fun initializeDatabase(context: Context) {
        if (database == null) {
            database = FinTrackDatabase.getInstance(context)
        }
    }

    fun getUserRepository(): UserRepository {
        return userRepository ?: synchronized(this) {
            val db = database ?: throw IllegalStateException("Database not initialized")
            UserRepository(db.userDao()).also { userRepository = it }
        }
    }

    fun getCategoryRepository(): CategoryRepository {
        return categoryRepository ?: synchronized(this) {
            val db = database ?: throw IllegalStateException("Database not initialized")
            CategoryRepository(db.categoryDao()).also { categoryRepository = it }
        }
    }

    fun getTransactionRepository(): TransactionRepository {
        return transactionRepository ?: synchronized(this) {
            val db = database ?: throw IllegalStateException("Database not initialized")
            TransactionRepository(db.transactionDao()).also { transactionRepository = it }
        }
    }

    fun getBudgetRepository(): BudgetRepository {
        return budgetRepository ?: synchronized(this) {
            val db = database ?: throw IllegalStateException("Database not initialized")
            BudgetRepository(db.budgetDao()).also { budgetRepository = it }
        }
    }

    fun reset() {
        database = null
        userRepository = null
        categoryRepository = null
        transactionRepository = null
        budgetRepository = null
    }
}

