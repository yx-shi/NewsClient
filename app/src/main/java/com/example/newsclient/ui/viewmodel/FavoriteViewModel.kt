package com.example.newsclient.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.newsclient.NewsApplication
import com.example.newsclient.data.local.NewsFavorite
import com.example.newsclient.data.local.UserPreferences
import com.example.newsclient.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * 收藏ViewModel
 * 负责管理收藏的状态和操作
 */
class FavoriteViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<NewsFavorite>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<NewsFavorite>>> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    /**
     * 加载收藏列表
     */
    fun loadFavorites() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading

                userPreferences.getFavoriteNewsFlow()
                    .catch { exception ->
                        _uiState.value = UiState.Error("加载收藏失败: ${exception.message}")
                    }
                    .collect { favoriteList ->
                        _uiState.value = if (favoriteList.isEmpty()) {
                            UiState.Empty
                        } else {
                            UiState.Success(favoriteList)
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("加载收藏失败: ${e.message}")
            }
        }
    }

    /**
     * 删除单条收藏
     */
    fun deleteFavoriteItem(newsId: String) {
        viewModelScope.launch {
            try {
                userPreferences.removeFromFavorites(newsId)
                // 刷新状态会通过Flow自动触发
            } catch (e: Exception) {
                _uiState.value = UiState.Error("删除收藏失败: ${e.message}")
            }
        }
    }

    /**
     * 清空所有收藏
     */
    fun clearAllFavorites() {
        viewModelScope.launch {
            try {
                userPreferences.clearFavorites()
                // 刷新状态会通过Flow自动触发
            } catch (e: Exception) {
                _uiState.value = UiState.Error("清空收藏失败: ${e.message}")
            }
        }
    }

    /**
     * 刷新收藏列表
     */
    fun refresh() {
        loadFavorites()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NewsApplication)
                FavoriteViewModel(
                    userPreferences = application.userPreferences
                )
            }
        }
    }
}
