package com.example.newsclient.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.newsclient.NewsApplication
import com.example.newsclient.data.model.News
import com.example.newsclient.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 摘要生成状态
 */
sealed class SummaryState {
    object Idle : SummaryState()           // 空闲状态
    object Loading : SummaryState()       // 生成中
    data class Success(val summary: String) : SummaryState()  // 生成成功
    data class Error(val message: String) : SummaryState()    // 生成失败
}

/**
 * 新闻详情ViewModel
 * 负责管理新闻详情数据、缓存和摘要生成
 */
class NewsDetailViewModel(
    private val repository: NewsRepository
) : ViewModel() {

    // 摘要状态管理
    private val _summaryState = MutableStateFlow<SummaryState>(SummaryState.Idle)
    val summaryState: StateFlow<SummaryState> = _summaryState.asStateFlow()

    // 摘要缓存，避免重复生成
    private val summaryCache = mutableMapOf<String, String>()

    /**
     * 根据新闻ID获取新闻详情
     */
    fun getNewsById(newsId: String): News? {
        return getCachedNews(newsId)
    }

    /**
     * 生成新闻摘要
     * @param news 要生成摘要的新闻
     * @param apiKey GLM API密钥
     */
    fun generateSummary(news: News, apiKey: String) {
        viewModelScope.launch {
            try {
                Log.d("NewsDetailViewModel", "🤖 开始生成摘要")
                Log.d("NewsDetailViewModel", "   新闻ID: ${news.id}")
                Log.d("NewsDetailViewModel", "   API Key长度: ${apiKey.length}")

                // 检查缓存
                if (summaryCache.containsKey(news.id)) {
                    Log.d("NewsDetailViewModel", "📦 从缓存获取摘要")
                    _summaryState.value = SummaryState.Success(summaryCache[news.id]!!)
                    return@launch
                }

                // 验证API Key
                if (apiKey.isBlank()) {
                    _summaryState.value = SummaryState.Error("请先设置GLM API密钥")
                    return@launch
                }

                // 设置加载状态
                _summaryState.value = SummaryState.Loading

                // 调用Repository生成摘要
                val summary = repository.generateNewsSummary(news, apiKey)

                // 缓存摘要
                summaryCache[news.id] = summary

                // 更新状态为成功
                _summaryState.value = SummaryState.Success(summary)

                Log.d("NewsDetailViewModel", "✅ 摘要生成成功")

            } catch (e: Exception) {
                Log.e("NewsDetailViewModel", "❌ 摘要生成失败", e)
                _summaryState.value = SummaryState.Error(e.message ?: "生成摘要失败")
            }
        }
    }

    /**
     * 重置摘要状态
     */
    fun resetSummaryState() {
        _summaryState.value = SummaryState.Idle
    }

    /**
     * 清除摘要缓存
     */
    fun clearSummaryCache() {
        summaryCache.clear()
    }

    companion object {
        // 新闻缓存，用于存储从列表页面传递的新闻数据
        private val newsCache = mutableMapOf<String, News>()

        /**
         * 缓存新闻数据
         */
        fun cacheNews(news: News) {
            newsCache[news.id] = news
        }

        /**
         * 根据ID获取缓存的新闻
         */
        fun getCachedNews(newsId: String): News? {
            return newsCache[newsId]
        }

        /**
         * 清理缓存
         */
        fun clearCache() {
            newsCache.clear()
        }

        /**
         * ViewModel工厂
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as NewsApplication)
                NewsDetailViewModel(
                    repository = application.container.newsRepository
                )
            }
        }
    }
}
