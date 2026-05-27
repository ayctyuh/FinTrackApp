package com.fintrack.project.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.fintrack.project.data.model.Transaction
import com.fintrack.project.data.model.TransactionType
import com.fintrack.project.data.repository.CategoryRepository
import com.fintrack.project.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel quan ly giao dich va tong hop so lieu.
 * Phu thuoc: `TransactionRepository`, `CategoryRepository`.
 * Duoc su dung boi cac man hinh giao dich/thong ke.
 */
class TransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : BaseViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions = _transactions.asStateFlow()

    private val _filteredTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val filteredTransactions = _filteredTransactions.asStateFlow()

    private val _selectedTransaction = MutableStateFlow<Transaction?>(null)
    val selectedTransaction = _selectedTransaction.asStateFlow()

    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome = _totalIncome.asStateFlow()

    private val _totalExpense = MutableStateFlow(0.0)
    val totalExpense = _totalExpense.asStateFlow()

    /**
     * Lay tat ca giao dich cua nguoi dung.
     * @param userId ID nguoi dung.
     */
    private val _spentByCategory = MutableStateFlow<Map<Int, Double>>(emptyMap())
    val spentByCategory = _spentByCategory.asStateFlow()
    fun getTransactions(userId: Int) {
        viewModelScope.launch {
            try {
                showLoading()
                clearError()
                val txns = transactionRepository.getUserTransactions(userId)
                _transactions.value = txns
                _filteredTransactions.value = txns
                updateTotals(userId)
                _spentByCategory.value = txns
                    .filter { it.type == TransactionType.EXPENSE && it.categoryId != null }
                    .groupBy { it.categoryId!! }
                    .mapValues { (_, list) -> list.sumOf { it.amount } }
            } catch (e: Exception) {
                setError(e.message ?: "Lỗi không xác định")
            } finally {
                hideLoading()
            }
        }
    }

    /**
     * Lay giao dich theo khoang ngay.
     * @param userId ID nguoi dung.
     * @param startDate Thoi gian bat dau.
     * @param endDate Thoi gian ket thuc.
     */
    fun getTransactionsByDateRange(userId: Int, startDate: Long, endDate: Long) {
        viewModelScope.launch {
            try {
                showLoading()
                clearError()
                val txns = transactionRepository.getTransactionsByDateRange(userId, startDate, endDate)
                _filteredTransactions.value = txns
            } catch (e: Exception) {
                setError(e.message ?: "Lỗi không xác định")
            } finally {
                hideLoading()
            }
        }
    }

    /**
     * Lay giao dich theo loai.
     * @param userId ID nguoi dung.
     * @param type Loai giao dich.
     */
    fun getTransactionsByType(userId: Int, type: TransactionType) {
        viewModelScope.launch {
            try {
                showLoading()
                clearError()
                val txns = transactionRepository.getTransactionsByType(userId, type)
                _filteredTransactions.value = txns
            } catch (e: Exception) {
                setError(e.message ?: "Lỗi không xác định")
            } finally {
                hideLoading()
            }
        }
    }

    /**
     * Lay giao dich theo danh muc.
     * @param userId ID nguoi dung.
     * @param categoryId ID danh muc.
     */
    fun getTransactionsByCategory(userId: Int, categoryId: Int) {
        viewModelScope.launch {
            try {
                showLoading()
                clearError()
                val txns = transactionRepository.getTransactionsByCategory(userId, categoryId)
                _filteredTransactions.value = txns
            } catch (e: Exception) {
                setError(e.message ?: "Lỗi không xác định")
            } finally {
                hideLoading()
            }
        }
    }

    /**
     * Them giao dich moi.
     * @param transaction Doi tuong giao dich.
     */
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                showLoading()
                clearError()
                transactionRepository.insertTransaction(transaction)
                getTransactions(transaction.userId)
            } catch (e: Exception) {
                setError(e.message ?: "Lỗi không xác định")
            } finally {
                hideLoading()
            }
        }
    }

    /**
     * Cap nhat giao dich.
     * @param transaction Doi tuong giao dich.
     */
    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                showLoading()
                clearError()
                transactionRepository.updateTransaction(transaction)
                getTransactions(transaction.userId)
            } catch (e: Exception) {
                setError(e.message ?: "Lỗi không xác định")
            } finally {
                hideLoading()
            }
        }
    }

    /**
     * Xoa giao dich.
     * @param transaction Doi tuong giao dich.
     */
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                showLoading()
                clearError()
                transactionRepository.deleteTransaction(transaction)
                getTransactions(transaction.userId)
            } catch (e: Exception) {
                setError(e.message ?: "Lỗi không xác định")
            } finally {
                hideLoading()
            }
        }
    }

    /**
     * Sap xep giao dich theo so tien.
     * @param ascending Sap xep tang dan neu true.
     */
    fun sortByAmount(ascending: Boolean = true) {
        val sorted = if (ascending) {
            _filteredTransactions.value.sortedBy { it.amount }
        } else {
            _filteredTransactions.value.sortedByDescending { it.amount }
        }
        _filteredTransactions.value = sorted
    }

    /**
     * Sap xep giao dich theo ngay.
     * @param ascending Sap xep tang dan neu true.
     */
    fun sortByDate(ascending: Boolean = false) {
        val sorted = if (ascending) {
            _filteredTransactions.value.sortedBy { it.transactionDate }
        } else {
            _filteredTransactions.value.sortedByDescending { it.transactionDate }
        }
        _filteredTransactions.value = sorted
    }

    /**
     * Cap nhat tong thu chi.
     * @param userId ID nguoi dung.
     */
    private suspend fun updateTotals(userId: Int) {
        _totalIncome.value = transactionRepository.getTotalAmount(userId, TransactionType.INCOME)
        _totalExpense.value = transactionRepository.getTotalAmount(userId, TransactionType.EXPENSE)
    }

    /**
     * Chon giao dich de xem/soan thao.
     * @param transaction Giao dich duoc chon.
     */
    fun setSelectedTransaction(transaction: Transaction) {
        _selectedTransaction.value = transaction
    }
}

