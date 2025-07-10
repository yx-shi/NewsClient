package com.example.newsclient.data.repository

import android.util.Log
import com.example.newsclient.data.model.News
import com.example.newsclient.data.remote.NewsApi
import com.example.newsclient.data.remote.NewsCategory
import java.io.IOException

const val START_DATE = "2023-01-01"
const val END_DATE = "2025-07-09"

interface NewsRepository{
    suspend fun getNews(
        category: NewsCategory? = null,
        keyword: String? = "",
        startDate: String? = "",
        endDate: String? = ""
    ): List<News>
}

class NetworkNewsRepository : NewsRepository {
    override suspend fun getNews(
        category: NewsCategory?,
        keyword: String?,
        startDate: String?,
        endDate: String?
    ): List<News> {
        return try {
            NewsApi.service.getNewsList(
                page = 1, // 默认第一页
                categories = category?.value,
                keyword = keyword,
                startDate = START_DATE,
                endDate = END_DATE
            ).data
        } catch (e: IOException) {
            Log.e("NetworkNewsRepository", "网络连接异常", e)
            emptyList()
        }
    }
}


