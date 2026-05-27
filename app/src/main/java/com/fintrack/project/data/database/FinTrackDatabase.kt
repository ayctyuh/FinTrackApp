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

@Database( // Room: khai bao database va danh sach entity
    entities = [
        User::class,
        Category::class,
        Transaction::class,
        Budget::class,
        Notification::class // <-- Thêm class này vào danh sách
    ],
    version = 7,
    exportSchema = false
)
/**
 * Room database tong the cua ung dung.
 * Phu thuoc: Room, cac entity va DAO.
 * Duoc su dung boi ServiceLocator/Repository de lay DAO.
 */
abstract class FinTrackDatabase : RoomDatabase() {
    /**
     * Lay `UserDao` tu database.
     * @return DAO quan ly bang user.
     */
    abstract fun userDao(): UserDao

    /**
     * Lay `CategoryDao` tu database.
     * @return DAO quan ly bang category.
     */
    abstract fun categoryDao(): CategoryDao

    /**
     * Lay `TransactionDao` tu database.
     * @return DAO quan ly bang transaction.
     */
    abstract fun transactionDao(): TransactionDao

    /**
     * Lay `BudgetDao` tu database.
     * @return DAO quan ly bang budget.
     */
    abstract fun budgetDao(): BudgetDao

    /**
     * Lay `NotificationDao` tu database.
     * @return DAO quan ly bang notification.
     */
    abstract fun notificationDao(): NotificationDao // <-- Khai báo Dao mới

    companion object {
        @Volatile
        private var INSTANCE: FinTrackDatabase? = null

        /**
         * Lay singleton instance cua Room database.
         * @param context Context de tao database.
         * @return Instance duy nhat cua `FinTrackDatabase`.
         */
        fun getInstance(context: Context): FinTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder( // Room: tao database builder voi ten file luu tru
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