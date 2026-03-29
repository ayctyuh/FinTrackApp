package com.fintrack.project.utils

import java.text.NumberFormat
import java.util.*

object CurrencyUtils {
    fun formatMoney(amount: Double): String {
        val format = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return "${format.format(amount)} đ"
    }

    fun formatMoneyShort(amount: Double): String {
        return when {
            amount >= 1_000_000 -> String.format("%.1fTr", amount / 1_000_000)
            amount >= 1_000 -> String.format("%.0fK", amount / 1_000)
            else -> String.format("%.0f", amount)
        }
    }
}
