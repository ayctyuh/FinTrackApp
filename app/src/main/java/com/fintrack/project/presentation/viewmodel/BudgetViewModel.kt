package com.fintrack.project.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.fintrack.project.data.model.Budget
import com.fintrack.project.data.repository.BudgetRepository
import com.fintrack.project.data.repository.TransactionRepository
import com.fintrack.project.data.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) : BaseViewModel() {

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets = _budgets.asStateFlow()

    private val _monthlyBudget = MutableStateFlow<Budget?>(null)
    val monthlyBudget = _monthlyBudget.asStateFlow()

    private val _categoryBudgets = MutableStateFlow<List<Budget>>(emptyList())
    val categoryBudgets = _categoryBudgets.asStateFlow()

    private val _budgetAlerts = MutableStateFlow<List<String>>(emptyList())
    val budgetAlerts = _budgetAlerts.asStateFlow()

    /**
     * Lấy ngân sách của người dùng
     */
    fun getUserBudgets(userId: Int) {
        viewModelScope.launch {
            try {
                showLoading()
                clearError()
                val budgets = budgetRepository.getUserBudgets(userId)
                _budgets.value = budgets
            } catch (e: Exception) {
                setError(e.message ?: "Lỗi không xác định")
            } finally {
                hideLoading()
            }
        }
    }

    /**
     * Lấy ngân sách theo tháng
     */
    fun getBudgetsByMonth(userId: Int, month: Int, year: Int) {
        viewModelScope.launch {
            try {
                showLoading()
                clearError()
                val budgets = budgetRepository.getBudgetsByMonth(userId, month, year)
                _budgets.value = budgets
                
                // Get monthly total budget
                val monthlyBudget = budgetRepository.getMonthlyBudget(userId, month, year)
                _monthlyBudget.value = monthlyBudget

                // Get category budgets
                _categoryBudgets.value = budgets.filter { it.categoryId != null }

                // Check for alerts
                checkBudgetAlerts(userId, month, year)
            } catch (e: Exception) {
                setError(e.message ?: "Lỗi không xác định")
            } finally {
                hideLoading()
            }
        }
    }

    /**
     * Thiết lập ngân sách mới
     */
    fun createBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                showLoading()
                clearError()
                budgetRepository.insertBudget(budget)
                getBudgetsByMonth(budget.userId, budget.month, budget.year)
            } catch (e: Exception) {
                setError(e.message ?: "Lỗi không xác định")
            } finally {
                hideLoading()
            }
        }
    }

    /**
     * Cập nhật ngân sách
     */
    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                showLoading()
                clearError()
                budgetRepository.updateBudget(budget)
                getBudgetsByMonth(budget.userId, budget.month, budget.year)
            } catch (e: Exception) {
                setError(e.message ?: "Lỗi không xác định")
            } finally {
                hideLoading()
            }
        }
    }

    /**
     * Xóa ngân sách
     */
    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                showLoading()
                clearError()
                budgetRepository.deleteBudget(budget)
                getUserBudgets(budget.userId)
            } catch (e: Exception) {
                setError(e.message ?: "Lỗi không xác định")
            } finally {
                hideLoading()
            }
        }
    }

    /**
     * Kiểm tra cảnh báo ngân sách
     */
    private suspend fun checkBudgetAlerts(userId: Int, month: Int, year: Int) {
        try {
            val alerts = mutableListOf<String>()
            val budgets = budgetRepository.getBudgetsByMonth(userId, month, year)

            for (budget in budgets) {
                if (budget.categoryId != null) {
                    val spent = transactionRepository.getTotalAmountByCategory(
                        userId, budget.categoryId, TransactionType.EXPENSE,
                        getStartOfMonthTimestamp(month, year),
                        getEndOfMonthTimestamp(month, year)
                    )

                    val percentage = (spent / budget.limitAmount) * 100

                    if (percentage >= 100) {
                        alerts.add("Danh mục đã vượt ngân sách: ${percentage.toInt()}%")
                    } else if (percentage >= budget.alertThreshold) {
                        alerts.add("Danh mục gần vượt ngân sách: ${percentage.toInt()}%")
                    }
                }
            }

            _budgetAlerts.value = alerts
        } catch (e: Exception) {
            setError(e.message)
        }
    }

    private fun getStartOfMonthTimestamp(month: Int, year: Int): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.YEAR, year)
        calendar.set(java.util.Calendar.MONTH, month - 1)
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfMonthTimestamp(month: Int, year: Int): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.YEAR, year)
        calendar.set(java.util.Calendar.MONTH, month)
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 0)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
        calendar.set(java.util.Calendar.MINUTE, 59)
        calendar.set(java.util.Calendar.SECOND, 59)
        return calendar.timeInMillis
    }
}

