package com.fintrack.project.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.fintrack.project.data.model.Transaction
import com.fintrack.project.data.model.TransactionType
import com.fintrack.project.data.repository.CategoryRepository
import com.fintrack.project.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
     * Lấy tất cả giao dịch của người dùng
     */
    fun getTransactions(userId: Int) {
        viewModelScope.launch {
            try {
                showLoading()
                clearError()
                val txns = transactionRepository.getUserTransactions(userId)
                _transactions.value = txns
                _filteredTransactions.value = txns
                updateTotals(userId)
            } catch (e: Exception) {
                setError(e.message ?: "Lỗi không xác định")
            } finally {
                hideLoading()
            }
        }
    }

    /**
     * Lấy giao dịch theo ngày
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
     * Lấy giao dịch theo loại
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
     * Lấy giao dịch theo danh mục
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
     * Thêm giao dịch mới
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
     * Cập nhật giao dịch
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
     * Xóa giao dịch
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
     * Sắp xếp giao dịch theo số tiền (tăng/giảm)
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
     * Sắp xếp giao dịch theo ngày
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
     * Cập nhật tổng thu chi
     */
    private suspend fun updateTotals(userId: Int) {
        _totalIncome.value = transactionRepository.getTotalAmount(userId, TransactionType.INCOME)
        _totalExpense.value = transactionRepository.getTotalAmount(userId, TransactionType.EXPENSE)
    }

    fun setSelectedTransaction(transaction: Transaction) {
        _selectedTransaction.value = transaction
    }
}

