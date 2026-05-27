package com.fintrack.project.data.dao

import androidx.room.*
import com.fintrack.project.data.model.Category
import com.fintrack.project.data.model.CategoryType

@Dao
/**
 * DAO cho bang danh muc.
 * Phu thuoc: Room va entity `Category`.
 * Duoc su dung boi `CategoryRepository`.
 */
interface CategoryDao {
    /**
     * Them danh muc.
     * @param category Doi tuong danh muc.
     */
    @Insert
    suspend fun insertCategory(category: Category): Long

    /**
     * Cap nhat danh muc.
     * @param category Doi tuong danh muc.
     */
    @Update
    suspend fun updateCategory(category: Category)

    /**
     * Xoa danh muc.
     * @param category Doi tuong danh muc.
     */
    @Delete
    suspend fun deleteCategory(category: Category)

    /**
     * Lay danh muc theo ID.
     * @param id ID danh muc.
     */
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): Category?

    /**
     * Lay danh sach danh muc theo user.
     * @param userId ID nguoi dung.
     */
    @Query("SELECT * FROM categories WHERE userId = :userId")
    suspend fun getUserCategories(userId: Int): List<Category>

    /**
     * Lay danh muc theo loai.
     * @param userId ID nguoi dung.
     * @param type Loai danh muc.
     */
    @Query("SELECT * FROM categories WHERE userId = :userId AND type = :type")
    suspend fun getCategoriesByType(userId: Int, type: CategoryType): List<Category>

    /**
     * Lay danh muc mac dinh.
     * @param userId ID nguoi dung.
     */
    @Query("SELECT * FROM categories WHERE userId = :userId AND isDefault = 1")
    suspend fun getDefaultCategories(userId: Int): List<Category>

    /**
     * Lay danh muc theo ten.
     * @param userId ID nguoi dung.
     * @param name Ten danh muc.
     */
    @Query("SELECT * FROM categories WHERE userId = :userId AND name = :name")
    suspend fun getCategoryByName(userId: Int, name: String): Category?

    /**
     * Xoa danh muc cua nguoi dung.
     * @param userId ID nguoi dung.
     */
    @Query("DELETE FROM categories WHERE userId = :userId")
    suspend fun deleteUserCategories(userId: Int)
}

