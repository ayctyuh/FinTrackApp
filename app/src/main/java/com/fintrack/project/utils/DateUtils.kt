package com.fintrack.project.utils

import android.text.format.DateFormat
import java.util.Calendar
import java.util.Date

/**
 * Tien ich xu ly thoi gian cho ung dung.
 * Phu thuoc: `Calendar`, `DateFormat`.
 * Duoc su dung boi cac man hinh va viewmodel can dinh dang ngay.
 */
object DateUtils {
    /**
    * Chuyen timestamp thanh chuoi theo dinh dang.
    * @param timestamp Thoi gian millis.
    * @param format Dinh dang ngay.
    * @return Chuoi ngay da dinh dang.
     */
    fun formatDate(timestamp: Long, format: String = "dd/MM/yyyy"): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return DateFormat.format(format, calendar).toString()
    }

    /**
        * Lay timestamp dau ngay.
        * @param timestamp Thoi gian millis bat ky.
        * @return Millis dau ngay.
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
        * Lay timestamp cuoi ngay.
        * @param timestamp Thoi gian millis bat ky.
        * @return Millis cuoi ngay.
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
        * Lay timestamp dau thang.
        * @param month Thang (1-12).
        * @param year Nam.
        * @return Millis dau thang.
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
        * Lay timestamp cuoi thang.
        * @param month Thang (1-12).
        * @param year Nam.
        * @return Millis cuoi thang.
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
        * Lay thang va nam hien tai.
        * @return Pair(thang, nam).
     */
    fun getCurrentMonthYear(): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        return Pair(calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))
    }

    /**
        * Lay ngay trong thang tu timestamp.
        * @param timestamp Thoi gian millis.
        * @return So ngay trong thang.
     */
    fun getDayOfMonth(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    /**
        * Lay thang tu timestamp.
        * @param timestamp Thoi gian millis.
        * @return Thang (1-12).
     */
    fun getMonth(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.MONTH) + 1
    }

    /**
        * Lay nam tu timestamp.
        * @param timestamp Thoi gian millis.
        * @return Nam.
     */
    fun getYear(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.YEAR)
    }

    /**
     * Lay thoi gian hien tai.
     * @return Millis hien tai.
     */
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }
}

