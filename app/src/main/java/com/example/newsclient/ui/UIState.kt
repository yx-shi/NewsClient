package com.example.newsclient.ui

import com.example.newsclient.data.model.News

// ui/common/UiState.kt

/**
 * 通用UI状态封装
 * T: 数据类型
 *
 * 状态说明:
 * - Loading: 加载中，显示进度条
 * - Success: 加载成功，包含数据
 * - Empty: 数据为空，显示空状态UI
 * - Error: 加载失败，显示错误信息
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    object Empty : UiState<Nothing>()
    data class Error(val message: String) : UiState<Nothing>()
}

// 新闻列表特定状态
data class NewsListState(
    val news: List<News> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false // 是否到达列表末尾
)

// 新闻详情状态
data class NewsDetailState(
    val news: News? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)