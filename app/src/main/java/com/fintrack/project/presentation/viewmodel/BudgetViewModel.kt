package com.fintrack.project.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.fintrack.project.data.model.Budget
import com.fintrack.project.data.repository.BudgetRepository
import com.fintrack.project.data.repository.TransactionRepository
import com.fintrack.project.data.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

data class MonthSummary(
    val month: Int,
    val totalLimit: Double,
    val totalSpent: Double,
    val status: String // "DAT", "GAN_VUOT", "VUOT", "CHUA_DEN"
)

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
    
    private val _spentByCategoryMonth = MutableStateFlow<Map<Int, Double>>(emptyMap())
    val spentByCategoryMonth = _spentByCategoryMonth.asStateFlow()

    private val _yearlySummary = MutableStateFlow<List<MonthSummary>>(emptyList())
    val yearlySummary = _yearlySummary.asStateFlow()

    private val _transactionCount = MutableStateFlow(0)
    val transactionCount = _transactionCount.asStateFlow()

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
                
                val monthlyBudget = budgetRepository.getMonthlyBudget(userId, month, year)
                _monthlyBudget.value = monthlyBudget

                _categoryBudgets.value = budgets.filter { it.categoryId != null }

                val start = getStartOfMonthTimestamp(month, year)
                val end = getEndOfMonthTimestamp(month, year)

                val txns = transactionRepository.getTransactionsByDateRange(userId, start, end)
                _transactionCount.value = txns.size
                
                _spentByCategoryMonth.value = txns
                    .filter { it.type == TransactionType.EXPENSE && it.categoryId != null }
                    .groupBy { it.categoryId!! }
                    .mapValues { (_, list) -> list.sumOf { it.amount } }

                checkBudgetAlerts(userId, month, year)
            } catch (e: Exception) {
                setError(e.message ?: "Lỗi không xác định")
            } finally {
                hideLoading()
            }
        }
    }

    /**
     * Lấy báo cáo tóm tắt cả năm
     */
    fun getYearlySummary(userId: Int, year: Int) {
        viewModelScope.launch {
            try {
                showLoading()
                val summary = mutableListOf<MonthSummary>()
                val currentCal = Calendar.getInstance()
                val currentMonth = currentCal.get(Calendar.MONTH) + 1
                val currentYear = currentCal.get(Calendar.YEAR)

                for (m in 1..12) {
                    if (year > currentYear || (year == currentYear && m > currentMonth)) {
                        summary.add(MonthSummary(m, 0.0, 0.0, "CHUA_DEN"))
                        continue
                    }

                    val limit = budgetRepository.getMonthlyBudget(userId, m, year)?.limitAmount ?: 0.0
                    val spent = transactionRepository.getTotalAmountByDateRange(
                        userId, TransactionType.EXPENSE,
                        getStartOfMonthTimestamp(m, year),
                        getEndOfMonthTimestamp(m, year)
                    )

                    val status = when {
                        limit <= 0 -> "DAT"
                        spent > limit -> "VUOT"
                        spent > limit * 0.8 -> "GAN_VUOT"
                        else -> "DAT"
                    }
                    summary.add(MonthSummary(m, limit, spent, status))
                }
                _yearlySummary.value = summary
            } catch (e: Exception) {
                setError(e.message)
            } finally {
                hideLoading()
            }
        }
    }

    fun createBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                showLoading()
                budgetRepository.insertBudget(budget)
                getBudgetsByMonth(budget.userId, budget.month, budget.year)
            } catch (e: Exception) {
                setError(e.message)
            } finally {
                hideLoading()
            }
        }
    }

    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                showLoading()
                budgetRepository.updateBudget(budget)
                getBudgetsByMonth(budget.userId, budget.month, budget.year)
            } catch (e: Exception) {
                setError(e.message)
            } finally {
                hideLoading()
            }
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                showLoading()
                budgetRepository.deleteBudget(budget)
                getBudgetsByMonth(budget.userId, budget.month, budget.year)
            } catch (e: Exception) {
                setError(e.message)
            } finally {
                hideLoading()
            }
        }
    }

    fun copyBudgetFromPreviousMonth(userId: Int, currentMonth: Int, currentYear: Int) {
        viewModelScope.launch {
            try {
                showLoading()
                val prevMonth = if (currentMonth == 1) 12 else currentMonth - 1
                val prevYear = if (currentMonth == 1) currentYear - 1 else currentYear

                withContext(Dispatchers.IO) {
                    val prevMonthlyBudget = budgetRepository.getMonthlyBudget(userId, prevMonth, prevYear)
                    val prevBudgets = budgetRepository.getBudgetsByMonth(userId, prevMonth, prevYear)

                    prevMonthlyBudget?.let { oldMonthly ->
                        budgetRepository.insertBudget(oldMonthly.copy(id = 0, month = currentMonth, year = currentYear))
                    }

                    prevBudgets.forEach { oldBudget ->
                        if (oldBudget.categoryId != null) {
                            budgetRepository.insertBudget(oldBudget.copy(id = 0, month = currentMonth, year = currentYear))
                        }
                    }
                }
                getBudgetsByMonth(userId, currentMonth, currentYear)
            } catch (e: Exception) {
                setError(e.message)
            } finally {
                hideLoading()
            }
        }
    }

    private suspend fun checkBudgetAlerts(userId: Int, month: Int, year: Int) {
        val alerts = mutableListOf<String>()
        val budgets = budgetRepository.getBudgetsByMonth(userId, month, year)
        for (budget in budgets) {
            if (budget.categoryId != null) {
                val spent = transactionRepository.getTotalAmountByCategory(
                    userId, budget.categoryId, TransactionType.EXPENSE,
                    getStartOfMonthTimestamp(month, year),
                    getEndOfMonthTimestamp(month, year)
                )
                val percentage = if (budget.limitAmount > 0) (spent / budget.limitAmount) * 100 else 0.0
                if (percentage >= 100) alerts.add("Danh mục đã vượt ngân sách: ${percentage.toInt()}%")
                else if (percentage >= budget.alertThreshold) alerts.add("Danh mục gần vượt ngân sách: ${percentage.toInt()}%")
            }
        }
        _budgetAlerts.value = alerts
    }

    private fun getStartOfMonthTimestamp(month: Int, year: Int): Long {
        return Calendar.getInstance().apply {
            set(year, month - 1, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfMonthTimestamp(month: Int, year: Int): Long {
        return Calendar.getInstance().apply {
            set(year, month - 1, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
}
