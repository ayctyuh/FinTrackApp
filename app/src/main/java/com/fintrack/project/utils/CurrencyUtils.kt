package com.fintrack.project.utils

import java.text.NumberFormat
import java.util.*

/**
 * Tien ich dinh dang tien te.
 * Phu thuoc: `NumberFormat`.
 * Duoc su dung boi cac man hinh hien thi so tien.
 */
object CurrencyUtils {
    /**
     * Dinh dang so tien theo VND.
     * @param amount So tien.
     * @return Chuoi da dinh dang.
     */
    fun formatMoney(amount: Double): String {
        val format = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return "${format.format(amount)} đ"
    }

    /**
     * Dinh dang so tien rut gon (K/Tr).
     * @param amount So tien.
     * @return Chuoi rut gon.
     */
    fun formatMoneyShort(amount: Double): String {
        return when {
            amount >= 1_000_000 -> String.format("%.1fTr", amount / 1_000_000)
            amount >= 1_000 -> String.format("%.0fK", amount / 1_000)
            else -> String.format("%.0f", amount)
        }
    }
}
