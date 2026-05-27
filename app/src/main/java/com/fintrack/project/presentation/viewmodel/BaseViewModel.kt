package com.fintrack.project.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Base ViewModel cung cap trang thai loading/error.
 * Phu thuoc: `ViewModel`, `StateFlow`.
 * Duoc su dung boi cac ViewModel khac trong presentation.
 */
open class BaseViewModel : ViewModel() {
    protected val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    protected val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    /**
     * Bat trang thai loading.
     */
    protected fun showLoading() {
        _loading.value = true
    }

    /**
     * Tat trang thai loading.
     */
    protected fun hideLoading() {
        _loading.value = false
    }

    /**
     * Dat thong bao loi.
     * @param message Noi dung loi.
     */
    protected fun setError(message: String?) {
        _error.value = message
    }

    /**
     * Xoa thong bao loi.
     */
    protected fun clearError() {
        _error.value = null
    }
}

