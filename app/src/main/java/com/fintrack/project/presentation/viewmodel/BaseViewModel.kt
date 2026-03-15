package com.fintrack.project.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

open class BaseViewModel : ViewModel() {
    protected val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    protected val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    protected fun showLoading() {
        _loading.value = true
    }

    protected fun hideLoading() {
        _loading.value = false
    }

    protected fun setError(message: String?) {
        _error.value = message
    }

    protected fun clearError() {
        _error.value = null
    }
}

