package com.example.newsclient.data.repository

import android.util.Log
import com.example.newsclient.data.model.News
import com.example.newsclient.data.remote.NewsApi
import java.io.IOException

class NewsRepository(
    private val db: NewsDatabase // 稍后实现
) {
    // 获取新闻列表
    suspend fun getNews(
        page: Int,
        category: String? = null,
        keyword: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): List<News> {
        return try {
            NewsApi.service.getNewsList(
                page = page,
                category = category,
                keyword = keyword,
                startDate = startDate,
                endDate = endDate
            ).data
        } catch (e: IOException) {
            // 失败时尝试从本地获取
            Log.e("NewsRepository", "网络连接异常", e)
            db.newsDao().getNewsByPage(offset = page * 10)
        }
    }

    // 获取所有分类
    fun getAllCategories(): List<String> {
        return listOf("全部", "娱乐", "军事", "教育", "文化", "健康", "财经", "体育", "汽车", "科技", "社会")
    }
}