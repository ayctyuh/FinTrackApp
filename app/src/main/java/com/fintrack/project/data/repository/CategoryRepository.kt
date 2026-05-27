package com.fintrack.project.data.repository

import com.fintrack.project.data.dao.CategoryDao
import com.fintrack.project.data.model.Category
import com.fintrack.project.data.model.CategoryType

/**
 * Repository dieu phoi du lieu danh muc.
 * Phu thuoc: `CategoryDao` (Room).
 * Duoc su dung boi cac ViewModel danh muc/giao dich.
 */
class CategoryRepository(
    private val categoryDao: CategoryDao
) {
    /**
     * Them danh muc.
     * @param category Doi tuong danh muc.
     */
    suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category)
    }

    /**
     * Cap nhat danh muc.
     * @param category Doi tuong danh muc.
     */
    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }

    /**
     * Xoa danh muc.
     * @param category Doi tuong danh muc.
     */
    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    /**
     * Lay danh muc theo ID.
     * @param id ID danh muc.
     */
    suspend fun getCategoryById(id: Int): Category? {
        return categoryDao.getCategoryById(id)
    }

    /**
     * Lay danh sach danh muc theo user.
     * @param userId ID nguoi dung.
     */
    suspend fun getUserCategories(userId: Int): List<Category> {
        return categoryDao.getUserCategories(userId)
    }

    /**
     * Lay danh muc theo loai.
     * @param userId ID nguoi dung.
     * @param type Loai danh muc.
     */
    suspend fun getCategoriesByType(userId: Int, type: CategoryType): List<Category> {
        return categoryDao.getCategoriesByType(userId, type)
    }

    /**
     * Lay danh muc mac dinh.
     * @param userId ID nguoi dung.
     */
    suspend fun getDefaultCategories(userId: Int): List<Category> {
        return categoryDao.getDefaultCategories(userId)
    }

    /**
     * Lay danh muc theo ten.
     * @param userId ID nguoi dung.
     * @param name Ten danh muc.
     */
    suspend fun getCategoryByName(userId: Int, name: String): Category? {
        return categoryDao.getCategoryByName(userId, name)
    }

    /**
     * Xoa danh muc cua nguoi dung.
     * @param userId ID nguoi dung.
     */
    suspend fun deleteUserCategories(userId: Int) {
        categoryDao.deleteUserCategories(userId)
    }
}

