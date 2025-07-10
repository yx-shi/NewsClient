package com.example.newsclient.data.repository

import android.util.Log
import com.example.newsclient.data.model.News
import com.example.newsclient.data.remote.NewsApiService
import com.example.newsclient.data.model.NewsCategory
import com.example.newsclient.data.remote.END_DATE
import java.io.IOException

const val PAGE=1;

interface NewsRepository{
    suspend fun getNews(
        category: NewsCategory? = null,
        keyword: String? = "",
    ): List<News>
}

class NetworkNewsRepository  (
    private val newsApiService: NewsApiService
): NewsRepository{
    override suspend fun getNews(
        category: NewsCategory?,
        keyword: String?,
    ): List<News> {
        return try {
            newsApiService.getNewsList(
                page = PAGE, // 默认第一页
                categories = category?.value,
                keyword = keyword,
            ).data
        } catch (e: IOException) {
            Log.e("NetworkNewsRepository", "网络连接异常", e)
            emptyList()
        }
    }
}


