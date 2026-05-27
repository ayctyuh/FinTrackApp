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

/**
 * DTO tong hop theo thang cho man hinh bao cao.
 * Phu thuoc: du lieu tinh toan tu `BudgetRepository` va `TransactionRepository`.
 * Duoc su dung boi `MonthlyReportScreen`.
 */
data class MonthSummary(
    val month: Int,
    val totalLimit: Double,
    val totalSpent: Double,
    val status: String, // "DAT", "GAN_VUOT", "VUOT", "CHUA_DEN"
    val totalIncome: Double = 0.0,
    )

/**
 * ViewModel xu ly logic bao cao va canh bao ngan sach.
 * Phu thuoc: `BudgetRepository`, `TransactionRepository`.
 * Duoc su dung boi cac man hinh bao cao/chi tiet (MonthlyReportScreen).
 */
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

    private val _incomeByCategoryMonth = MutableStateFlow<Map<Int, Double>>(emptyMap())
    val incomeByCategoryMonth = _incomeByCategoryMonth.asStateFlow()

    private val _yearlySummary = MutableStateFlow<List<MonthSummary>>(emptyList())
    val yearlySummary = _yearlySummary.asStateFlow()

    private val _transactionCount = MutableStateFlow(0)
    val transactionCount = _transactionCount.asStateFlow()

    /**
     * Lay toan bo ngan sach cua nguoi dung.
     * @param userId ID nguoi dung can truy van.
     * @return Khong tra ve, cap nhat StateFlow `budgets`.
     * Logic: goi repository lay danh sach va day vao state.
     */
    fun getUserBudgets(userId: Int) {
        viewModelScope.launch { // Kotlin Coroutines: chay bat dong bo trong lifecycle ViewModel
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
     * Lay ngan sach theo thang va tinh cac so lieu tong hop.
     * @param userId ID nguoi dung.
     * @param month Thang can xem (1-12).
     * @param year Nam can xem.
     * @return Khong tra ve, cap nhat cac StateFlow lien quan.
     * Logic: truy van ngan sach, giao dich, gom nhom theo danh muc va kiem tra canh bao.
     */
    fun getBudgetsByMonth(userId: Int, month: Int, year: Int) {
        viewModelScope.launch { // Kotlin Coroutines: chay bat dong bo trong lifecycle ViewModel
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
                    .filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.categoryId ?: -1 } // Nếu không có danh mục thì cho vào nhóm -1 (Khác)
                    .mapValues { (_, list) -> list.sumOf { it.amount } }

                _incomeByCategoryMonth.value = txns
                    .filter { it.type == TransactionType.INCOME }
                    .groupBy { it.categoryId ?: -1 }
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
     * Lay bao cao tong hop ca nam theo thang.
     * @param userId ID nguoi dung.
     * @param year Nam can tong hop.
     * @return Khong tra ve, cap nhat StateFlow `yearlySummary`.
     * Logic: lap 12 thang, tinh han muc/chi/thu va xep trang thai.
     */
    fun getYearlySummary(userId: Int, year: Int) {
        viewModelScope.launch { // Kotlin Coroutines: chay bat dong bo trong lifecycle ViewModel
            try {
                showLoading()
                val summary = mutableListOf<MonthSummary>()
                val currentCal = Calendar.getInstance()
                val currentMonth = currentCal.get(Calendar.MONTH) + 1
                val currentYear = currentCal.get(Calendar.YEAR)

                for (m in 1..12) {
                    if (year > currentYear || (year == currentYear && m > currentMonth)) {
                        summary.add(MonthSummary(m, 0.0, 0.0, "CHUA_DEN", 0.0))
                        continue
                    }
                    val startTs = getStartOfMonthTimestamp(m, year)
                    val endTs = getEndOfMonthTimestamp(m, year)
                    val limit = budgetRepository.getMonthlyBudget(userId, m, year)?.limitAmount ?: 0.0
                    val spent = transactionRepository.getTotalAmountByDateRange(
                        userId, TransactionType.EXPENSE, startTs, endTs
                    )
                    val income = transactionRepository.getTotalAmountByDateRange(
                        userId, TransactionType.INCOME, startTs, endTs
                    )
                    val status = when {
                        limit <= 0 -> "DAT"
                        spent > limit -> "VUOT"
                        spent > limit * 0.8 -> "GAN_VUOT"
                        else -> "DAT"
                    }
                    summary.add(MonthSummary(m, limit, spent, status, income))
                }
                _yearlySummary.value = summary
            } catch (e: Exception) {
                setError(e.message)
            } finally {
                hideLoading()
            }
        }
    }

    /**
     * Tao moi ngan sach.
     * @param budget Doi tuong ngan sach can them.
     * @return Khong tra ve, tai lai du lieu thang tuong ung.
     * Logic: insert vao Room qua repository, sau do refresh.
     */
    fun createBudget(budget: Budget) {
        viewModelScope.launch { // Kotlin Coroutines: thuc thi tac vu I/O khong chan UI
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

    /**
     * Cap nhat ngan sach.
     * @param budget Ngan sach da chinh sua.
     * @return Khong tra ve, tai lai du lieu thang tuong ung.
     */
    fun updateBudget(budget: Budget) {
        viewModelScope.launch { // Kotlin Coroutines: thuc thi tac vu I/O khong chan UI
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

    /**
     * Xoa ngan sach.
     * @param budget Ngan sach can xoa.
     * @return Khong tra ve, tai lai du lieu thang tuong ung.
     */
    fun deleteBudget(budget: Budget) {
        viewModelScope.launch { // Kotlin Coroutines: thuc thi tac vu I/O khong chan UI
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

    /**
     * Sao chep ngan sach tu thang truoc sang thang hien tai.
     * @param userId ID nguoi dung.
     * @param currentMonth Thang hien tai.
     * @param currentYear Nam hien tai.
     * @return Khong tra ve, cap nhat du lieu thang hien tai.
     * Logic: lay ngan sach thang truoc, copy sang thang moi.
     */
    fun copyBudgetFromPreviousMonth(userId: Int, currentMonth: Int, currentYear: Int) {
        viewModelScope.launch { // Kotlin Coroutines: chay bat dong bo trong lifecycle ViewModel
            try {
                showLoading()
                val prevMonth = if (currentMonth == 1) 12 else currentMonth - 1
                val prevYear = if (currentMonth == 1) currentYear - 1 else currentYear

                withContext(Dispatchers.IO) { // Kotlin Coroutines: chuyen sang thread I/O de truy van Room
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

    /**
     * Kiem tra va tao danh sach canh bao ngan sach theo danh muc.
     * @param userId ID nguoi dung.
     * @param month Thang can kiem tra.
     * @param year Nam can kiem tra.
     * @return Khong tra ve, cap nhat StateFlow `budgetAlerts`.
     * Logic: tinh % su dung va so sanh nguong canh bao.
     */
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

    /**
     * Tinh timestamp bat dau thang.
     * @param month Thang (1-12).
     * @param year Nam.
     * @return Millis dau thang theo mui gio he thong.
     */
    private fun getStartOfMonthTimestamp(month: Int, year: Int): Long {
        return Calendar.getInstance().apply {
            set(year, month - 1, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Tinh timestamp ket thuc thang.
     * @param month Thang (1-12).
     * @param year Nam.
     * @return Millis cuoi thang theo mui gio he thong.
     */
    private fun getEndOfMonthTimestamp(month: Int, year: Int): Long {
        return Calendar.getInstance().apply {
            set(year, month - 1, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
}
