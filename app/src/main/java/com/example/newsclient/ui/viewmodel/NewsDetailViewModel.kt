package com.example.newsclient.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.newsclient.data.model.News

/**
 * 新闻详情ViewModel
 * 负责管理新闻详情数据和缓存
 */
class NewsDetailViewModel : ViewModel() {

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
    }

    /**
     * 根据新闻ID获取新闻详情
     */
    fun getNewsById(newsId: String): News? {
        return getCachedNews(newsId)
    }
}
