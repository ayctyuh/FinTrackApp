package com.fintrack.project.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fintrack.project.data.dao.BudgetDao
import com.fintrack.project.data.dao.CategoryDao
import com.fintrack.project.data.dao.NotificationDao // <-- Thêm dòng này
import com.fintrack.project.data.dao.TransactionDao
import com.fintrack.project.data.dao.UserDao
import com.fintrack.project.data.model.Budget
import com.fintrack.project.data.model.Category
import com.fintrack.project.data.model.Notification // <-- Thêm dòng này
import com.fintrack.project.data.model.Transaction
import com.fintrack.project.data.model.User

@Database(
    entities = [
        User::class,
        Category::class,
        Transaction::class,
        Budget::class,
        Notification::class // <-- Thêm class này vào danh sách
    ],
    version = 6,
    exportSchema = false
)
abstract class FinTrackDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun notificationDao(): NotificationDao // <-- Khai báo Dao mới

    companion object {
        @Volatile
        private var INSTANCE: FinTrackDatabase? = null

        fun getInstance(context: Context): FinTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinTrackDatabase::class.java,
                    "fintrack_database"
                )
                    .fallbackToDestructiveMigration() // Tùy chọn này sẽ xóa trắng dữ liệu cũ khi đổi version. Cẩn thận nếu đang có data quan trọng!
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}