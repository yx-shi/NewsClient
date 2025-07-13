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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
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

    // 添加Job引用来管理协程生命周期
    private var flowCollectionJob: kotlinx.coroutines.Job? = null

    init {
        startFlowCollection()
    }

    /**
     * 启动Flow收集，避免重复启动
     */
    private fun startFlowCollection() {
        // 取消之前的Job（如果存在）
        flowCollectionJob?.cancel()

        // 强制刷新UserPreferences的StateFlow，确保数据同步
        userPreferences.refreshFavoriteFlow()

        // 立即获取当前数据并更新UI
        val currentFavorites = userPreferences.getFavoriteNews()
        android.util.Log.d("FavoriteViewModel", "立即加载收藏数据: ${currentFavorites.size} 条")

        _uiState.value = if (currentFavorites.isEmpty()) {
            UiState.Empty
        } else {
            UiState.Success(currentFavorites)
        }

        // 启动新的Flow监听
        flowCollectionJob = viewModelScope.launch {
            try {
                android.util.Log.d("FavoriteViewModel", "开始监听收藏Flow")
                userPreferences.getFavoriteNewsFlow()
                    .collect { favoriteList ->
                        android.util.Log.d("FavoriteViewModel", "收藏Flow更新: ${favoriteList.size} 条")
                        _uiState.value = if (favoriteList.isEmpty()) {
                            UiState.Empty
                        } else {
                            UiState.Success(favoriteList)
                        }
                    }
            } catch (e: CancellationException) {
                android.util.Log.d("FavoriteViewModel", "Flow监听被正常取消")
                // CancellationException是正常的，不需要特殊处理
            } catch (e: Exception) {
                android.util.Log.e("FavoriteViewModel", "Flow监听异常", e)
                // 其他异常才需要错误处理
                _uiState.value = UiState.Error("加载收藏失败: ${e.message}")
            }
        }
    }

    /**
     * 加载收藏列表 - 简化为重新启动Flow收集
     */
    fun loadFavorites() {
        startFlowCollection()
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
                _uiState.value = UiState.Error("删除收藏��败: ${e.message}")
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

    override fun onCleared() {
        super.onCleared()
        flowCollectionJob?.cancel()
        android.util.Log.d("FavoriteViewModel", "ViewModel被清��，取消Flow监听")
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
