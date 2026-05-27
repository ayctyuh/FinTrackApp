package com.fintrack.project.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.project.data.model.Transaction
import com.fintrack.project.data.model.TransactionType
import com.fintrack.project.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

/**
 * Diem du lieu so sanh theo ngay.
 * Duoc su dung boi `StatisticsViewModel` va man hinh thong ke.
 */
data class ComparisonDataPoint(
    val dateLabel: String,
    val income: Double,
    val expense: Double,
    val timestamp: Long
)

/**
 * Du lieu so sanh theo tuan.
 * Duoc su dung boi bieu do so sanh.
 */
data class WeeklyComparisonData(
    val weekLabel: String,
    val incomeA: Double,
    val incomeB: Double,
    val expenseA: Double,
    val expenseB: Double
)

/**
 * Trang thai UI cho thong ke.
 * Duoc su dung boi `StatisticsScreen`.
 */
data class StatisticsState(
    val comparisonData: List<ComparisonDataPoint> = emptyList(),
    val weeklyComparison: List<WeeklyComparisonData> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalIncomeA: Double = 0.0,
    val totalIncomeB: Double = 0.0,
    val totalExpenseA: Double = 0.0,
    val totalExpenseB: Double = 0.0,
    val monthA: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val yearA: Int = Calendar.getInstance().get(Calendar.YEAR),
    val monthB: Int = (if (Calendar.getInstance().get(Calendar.MONTH) == 0) 12 else Calendar.getInstance().get(Calendar.MONTH)),
    val yearB: Int = (if (Calendar.getInstance().get(Calendar.MONTH) == 0) Calendar.getInstance().get(Calendar.YEAR) - 1 else Calendar.getInstance().get(Calendar.YEAR)),
    val isLoading: Boolean = false
)

/**
 * ViewModel thong ke thu/chi.
 * Phu thuoc: `TransactionRepository`.
 * Duoc su dung boi `StatisticsScreen` va `ChiTietBieuDoScreen`.
 */
class StatisticsViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsState())
    val uiState: StateFlow<StatisticsState> = _uiState.asStateFlow()

    /**
     * Tai du lieu so sanh theo ngay.
     * @param userId ID nguoi dung.
     * @param startDate Bat dau.
     * @param endDate Ket thuc.
     */
    fun loadComparisonData(userId: Int, startDate: Long, endDate: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val transactions = transactionRepository.getTransactionsByDateRange(userId, startDate, endDate)
            
            val grouped = transactions.groupBy { 
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.transactionDate
                "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}"
            }

            val resultList = mutableListOf<ComparisonDataPoint>()
            val currentCal = Calendar.getInstance()
            currentCal.timeInMillis = startDate
            currentCal.set(Calendar.HOUR_OF_DAY, 0)
            currentCal.set(Calendar.MINUTE, 0)
            currentCal.set(Calendar.SECOND, 0)
            
            val endCal = Calendar.getInstance()
            endCal.timeInMillis = endDate

            while (currentCal.before(endCal) || isSameDay(currentCal, endCal)) {
                val label = "${currentCal.get(Calendar.DAY_OF_MONTH)}/${currentCal.get(Calendar.MONTH) + 1}"
                val dayTransactions = grouped[label] ?: emptyList()
                
                val inc = dayTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val exp = dayTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                
                resultList.add(ComparisonDataPoint(label, inc, exp, currentCal.timeInMillis))
                currentCal.add(Calendar.DAY_OF_MONTH, 1)
            }

            val totalInc = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val totalExp = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

            _uiState.value = _uiState.value.copy(
                comparisonData = resultList,
                totalIncome = totalInc,
                totalExpense = totalExp,
                isLoading = false
            )
        }
    }

    /**
     * Tai du lieu so sanh theo thang.
     * @param userId ID nguoi dung.
     * @param monthA Thang moc A.
     * @param yearA Nam moc A.
     * @param monthB Thang moc B.
     * @param yearB Nam moc B.
     */
    fun loadMonthlyComparison(userId: Int, monthA: Int, yearA: Int, monthB: Int, yearB: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                monthA = monthA, yearA = yearA,
                monthB = monthB, yearB = yearB
            )

            val startA = getStartOfMonth(monthA, yearA)
            val endA = getEndOfMonth(monthA, yearA)
            val transactionsA = transactionRepository.getTransactionsByDateRange(userId, startA, endA)

            val startB = getStartOfMonth(monthB, yearB)
            val endB = getEndOfMonth(monthB, yearB)
            val transactionsB = transactionRepository.getTransactionsByDateRange(userId, startB, endB)

            val weeklyData = mutableListOf<WeeklyComparisonData>()
            for (week in 1..4) {
                val weekLabel = "Tuần $week"
                val (wStartA, wEndA) = getWeekRange(week, monthA, yearA)
                val (wStartB, wEndB) = getWeekRange(week, monthB, yearB)

                val incA = transactionsA.filter { it.transactionDate in wStartA..wEndA && it.type == TransactionType.INCOME }.sumOf { it.amount }
                val expA = transactionsA.filter { it.transactionDate in wStartA..wEndA && it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                
                val incB = transactionsB.filter { it.transactionDate in wStartB..wEndB && it.type == TransactionType.INCOME }.sumOf { it.amount }
                val expB = transactionsB.filter { it.transactionDate in wStartB..wEndB && it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                weeklyData.add(WeeklyComparisonData(weekLabel, incA, incB, expA, expB))
            }

            val totalIncA = transactionsA.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val totalExpA = transactionsA.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val totalIncB = transactionsB.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val totalExpB = transactionsB.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

            _uiState.value = _uiState.value.copy(
                weeklyComparison = weeklyData,
                totalIncomeA = totalIncA,
                totalExpenseA = totalExpA,
                totalIncomeB = totalIncB,
                totalExpenseB = totalExpB,
                isLoading = false
            )
        }
    }

    /**
     * Tinh timestamp dau thang.
     * @param month Thang (1-12).
     * @param year Nam.
     */
    private fun getStartOfMonth(month: Int, year: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * Tinh timestamp cuoi thang.
     * @param month Thang (1-12).
     * @param year Nam.
     */
    private fun getEndOfMonth(month: Int, year: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 23, 59, 59)
        cal.set(Calendar.MILLISECOND, 999)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        return cal.timeInMillis
    }

    /**
     * Lay khoang thoi gian theo tuan trong thang.
     * @param week So tuan (1-4).
     * @param month Thang.
     * @param year Nam.
     */
    private fun getWeekRange(week: Int, month: Int, year: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        
        val startDay = (week - 1) * 7 + 1
        val endDay = if (week == 4) cal.getActualMaximum(Calendar.DAY_OF_MONTH) else week * 7
        
        cal.set(Calendar.DAY_OF_MONTH, startDay)
        val start = cal.timeInMillis
        
        cal.set(Calendar.DAY_OF_MONTH, endDay)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        
        return Pair(start, end)
    }

    /**
     * Kiem tra cung ngay.
     * @param cal1 Calendar 1.
     * @param cal2 Calendar 2.
     */
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
