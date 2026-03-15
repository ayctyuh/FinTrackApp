package com.fintrack.project.utils

import android.text.format.DateFormat
import java.util.Calendar
import java.util.Date

object DateUtils {
    /**
     * Chuyển timestamp thành string
     */
    fun formatDate(timestamp: Long, format: String = "dd/MM/yyyy"): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return DateFormat.format(format, calendar).toString()
    }

    /**
     * Lấy timestamp của đầu ngày
     */
    fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Lấy timestamp của cuối ngày
     */
    fun getEndOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    /**
     * Lấy timestamp của đầu tháng
     */
    fun getStartOfMonth(month: Int, year: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Lấy timestamp của cuối tháng
     */
    fun getEndOfMonth(month: Int, year: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    /**
     * Lấy tháng và năm hiện tại
     */
    fun getCurrentMonthYear(): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        return Pair(calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))
    }

    /**
     * Chuyển ngày thành số
     */
    fun getDayOfMonth(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    /**
     * Chuyển tháng thành số
     */
    fun getMonth(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.MONTH) + 1
    }

    /**
     * Chuyển năm thành số
     */
    fun getYear(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.YEAR)
    }

    /**
     * Lấy ngày hiện tại
     */
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }
}

