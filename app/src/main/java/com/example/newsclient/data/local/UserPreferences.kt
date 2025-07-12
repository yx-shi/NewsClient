package com.example.newsclient.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.newsclient.data.model.NewsCategory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 用户偏好设置管理类
 * 负责保存和读取用户的分类偏好设置
 */
class UserPreferences(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "news_client_preferences",
        Context.MODE_PRIVATE
    )

    private val gson = Gson()

    // 用于响应式更新的StateFlow
    private val _userCategories = MutableStateFlow<List<NewsCategory>>(emptyList())

    companion object {
        private const val KEY_SELECTED_CATEGORIES = "selected_categories"
        private const val KEY_CATEGORY_ORDER = "category_order"
    }

    init {
        // 初始化时加载用户分类
        _userCategories.value = getSelectedCategories()
    }

    /**
     * 获取用户选择的分类列表
     * 如果没有保存过偏好，返回默认的前5个分类
     */
    fun getSelectedCategories(): List<NewsCategory> {
        val savedJson = sharedPreferences.getString(KEY_SELECTED_CATEGORIES, null)
        return if (savedJson != null) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                val categoryValues: List<String> = gson.fromJson(savedJson, type)
                categoryValues.mapNotNull { value ->
                    NewsCategory.entries.find { it.value == value }
                }
            } catch (e: Exception) {
                getDefaultCategories()
            }
        } else {
            getDefaultCategories()
        }
    }

    /**
     * 获取用户分类的响应式Flow
     */
    fun getUserCategories(): Flow<List<NewsCategory>> {
        return _userCategories.asStateFlow()
    }

    /**
     * 保存用户选择的分类列表
     */
    fun saveSelectedCategories(categories: List<NewsCategory>) {
        val categoryValues = categories.map { it.value }
        val json = gson.toJson(categoryValues)
        sharedPreferences.edit()
            .putString(KEY_SELECTED_CATEGORIES, json)
            .apply()

        // 更新响应式状态
        _userCategories.value = categories
    }

    /**
     * 获取分类显示顺序
     */
    fun getCategoryOrder(): List<NewsCategory> {
        val savedJson = sharedPreferences.getString(KEY_CATEGORY_ORDER, null)
        return if (savedJson != null) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                val categoryValues: List<String> = gson.fromJson(savedJson, type)
                categoryValues.mapNotNull { value ->
                    NewsCategory.entries.find { it.value == value }
                }
            } catch (e: Exception) {
                getSelectedCategories()
            }
        } else {
            getSelectedCategories()
        }
    }

    /**
     * 保存分类显示顺序
     */
    fun saveCategoryOrder(categories: List<NewsCategory>) {
        val categoryValues = categories.map { it.value }
        val json = gson.toJson(categoryValues)
        sharedPreferences.edit()
            .putString(KEY_CATEGORY_ORDER, json)
            .apply()
    }

    /**
     * 添加一个分类到用户偏好
     */
    fun addCategory(category: NewsCategory) {
        val currentCategories = getSelectedCategories().toMutableList()
        if (!currentCategories.contains(category)) {
            currentCategories.add(category)
            saveSelectedCategories(currentCategories)
        }
    }

    /**
     * 从用户偏好中移除一个分类
     */
    fun removeCategory(category: NewsCategory) {
        val currentCategories = getSelectedCategories().toMutableList()
        if (currentCategories.contains(category) && currentCategories.size > 1) {
            currentCategories.remove(category)
            saveSelectedCategories(currentCategories)
        }
    }

    /**
     * 检查分类是否已被用户选择
     */
    fun isCategorySelected(category: NewsCategory): Boolean {
        return getSelectedCategories().contains(category)
    }

    /**
     * 获取可添加的分类列表（未被用户选择的分类）
     */
    fun getAvailableCategories(): List<NewsCategory> {
        val selectedCategories = getSelectedCategories()
        return NewsCategory.entries.filter { !selectedCategories.contains(it) }
    }

    /**
     * 重置为默认分类
     */
    fun resetToDefault() {
        saveSelectedCategories(getDefaultCategories())
    }

    /**
     * 获取默认分类（前5个）
     */
    private fun getDefaultCategories(): List<NewsCategory> {
        return listOf(
            NewsCategory.ENTERTAINMENT,
            NewsCategory.TECHNOLOGY,
            NewsCategory.SPORTS,
            NewsCategory.FINANCE,
            NewsCategory.SOCIETY
        )
    }
}
