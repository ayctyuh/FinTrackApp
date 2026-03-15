package com.fintrack.project.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fintrack.project.data.dao.BudgetDao
import com.fintrack.project.data.dao.CategoryDao
import com.fintrack.project.data.dao.TransactionDao
import com.fintrack.project.data.dao.UserDao
import com.fintrack.project.data.model.Budget
import com.fintrack.project.data.model.Category
import com.fintrack.project.data.model.Transaction
import com.fintrack.project.data.model.User

@Database(
    entities = [
        User::class,
        Category::class,
        Transaction::class,
        Budget::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FinTrackDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao

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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

