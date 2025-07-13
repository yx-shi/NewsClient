package com.example.newsclient.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.newsclient.NewsApplication
import com.example.newsclient.data.local.NewsHistory
import com.example.newsclient.data.local.UserPreferences
import com.example.newsclient.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * 历史记录ViewModel
 * 负责管理历史记录的状态和操作
 */
class HistoryViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<NewsHistory>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<NewsHistory>>> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    /**
     * 加载历史记录
     */
    fun loadHistory() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading

                userPreferences.getHistoryNewsFlow()
                    .catch { exception ->
                        _uiState.value = UiState.Error("加载历史记录失败: ${exception.message}")
                    }
                    .collect { historyList ->
                        _uiState.value = if (historyList.isEmpty()) {
                            UiState.Empty
                        } else {
                            UiState.Success(historyList)
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("加载历史记录失败: ${e.message}")
            }
        }
    }

    /**
     * 删除单条历史记录
     */
    fun deleteHistoryItem(newsId: String) {
        viewModelScope.launch {
            try {
                userPreferences.removeFromHistory(newsId)
                // 刷新状态会通过Flow自动触发
            } catch (e: Exception) {
                _uiState.value = UiState.Error("删除历史记录失败: ${e.message}")
            }
        }
    }

    /**
     * 清空所有历史记录
     */
    fun clearAllHistory() {
        viewModelScope.launch {
            try {
                userPreferences.clearHistory()
                // 刷新状态会通过Flow自动触发
            } catch (e: Exception) {
                _uiState.value = UiState.Error("清空历史记录失败: ${e.message}")
            }
        }
    }

    /**
     * 刷新历史记录
     */
    fun refresh() {
        loadHistory()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NewsApplication)
                HistoryViewModel(
                    userPreferences = application.userPreferences
                )
            }
        }
    }
}
