package com.fintrack.project.data.dao

import androidx.room.*
import com.fintrack.project.data.model.Category
import com.fintrack.project.data.model.CategoryType

@Dao
interface CategoryDao {
    @Insert
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): Category?

    @Query("SELECT * FROM categories WHERE userId = :userId")
    suspend fun getUserCategories(userId: Int): List<Category>

    @Query("SELECT * FROM categories WHERE userId = :userId AND type = :type")
    suspend fun getCategoriesByType(userId: Int, type: CategoryType): List<Category>

    @Query("SELECT * FROM categories WHERE userId = :userId AND isDefault = 1")
    suspend fun getDefaultCategories(userId: Int): List<Category>

    @Query("SELECT * FROM categories WHERE userId = :userId AND name = :name")
    suspend fun getCategoryByName(userId: Int, name: String): Category?

    @Query("DELETE FROM categories WHERE userId = :userId")
    suspend fun deleteUserCategories(userId: Int)
}

