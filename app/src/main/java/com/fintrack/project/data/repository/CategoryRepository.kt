package com.fintrack.project.data.repository

import com.fintrack.project.data.dao.CategoryDao
import com.fintrack.project.data.model.Category
import com.fintrack.project.data.model.CategoryType

class CategoryRepository(
    private val categoryDao: CategoryDao
) {
    suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    suspend fun getCategoryById(id: Int): Category? {
        return categoryDao.getCategoryById(id)
    }

    suspend fun getUserCategories(userId: Int): List<Category> {
        return categoryDao.getUserCategories(userId)
    }

    suspend fun getCategoriesByType(userId: Int, type: CategoryType): List<Category> {
        return categoryDao.getCategoriesByType(userId, type)
    }

    suspend fun getDefaultCategories(userId: Int): List<Category> {
        return categoryDao.getDefaultCategories(userId)
    }

    suspend fun getCategoryByName(userId: Int, name: String): Category? {
        return categoryDao.getCategoryByName(userId, name)
    }

    suspend fun deleteUserCategories(userId: Int) {
        categoryDao.deleteUserCategories(userId)
    }
}

