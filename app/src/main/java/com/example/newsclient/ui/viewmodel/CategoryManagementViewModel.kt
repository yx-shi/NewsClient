package com.example.newsclient.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.newsclient.NewsApplication
import com.example.newsclient.data.local.UserPreferences
import com.example.newsclient.data.model.NewsCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 分类管理的ViewModel
 * 负责管理用户的分类偏好设置
 */
class CategoryManagementViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    // 用户选择的分类列表
    private val _selectedCategories = MutableStateFlow<List<NewsCategory>>(emptyList())
    val selectedCategories: StateFlow<List<NewsCategory>> = _selectedCategories.asStateFlow()

    // 可添加的分类列表
    private val _availableCategories = MutableStateFlow<List<NewsCategory>>(emptyList())
    val availableCategories: StateFlow<List<NewsCategory>> = _availableCategories.asStateFlow()

    init {
        loadCategories()
    }

    /**
     * 加载分类数据
     */
    private fun loadCategories() {
        viewModelScope.launch {
            _selectedCategories.value = userPreferences.getSelectedCategories()
            _availableCategories.value = userPreferences.getAvailableCategories()
        }
    }

    /**
     * 添加分类
     */
    fun addCategory(category: NewsCategory) {
        viewModelScope.launch {
            userPreferences.addCategory(category)
            loadCategories() // 重新加载数据
        }
    }

    /**
     * 移除分类
     */
    fun removeCategory(category: NewsCategory) {
        viewModelScope.launch {
            // 确保至少保留一个分类
            if (_selectedCategories.value.size > 1) {
                userPreferences.removeCategory(category)
                loadCategories() // 重新加载数据
            }
        }
    }

    /**
     * 重置为默认分类
     */
    fun resetToDefault() {
        viewModelScope.launch {
            userPreferences.resetToDefault()
            loadCategories() // 重新加载数据
        }
    }

    /**
     * 更新分类顺序
     */
    fun updateCategoryOrder(categories: List<NewsCategory>) {
        viewModelScope.launch {
            userPreferences.saveCategoryOrder(categories)
            _selectedCategories.value = categories
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as NewsApplication)
                CategoryManagementViewModel(
                    userPreferences = application.userPreferences
                )
            }
        }
    }
}
