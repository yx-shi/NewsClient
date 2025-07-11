package com.example.newsclient.data.repository

import android.util.Log
import com.example.newsclient.data.model.News
import com.example.newsclient.data.remote.NewsApiService
import com.example.newsclient.data.model.NewsCategory
import com.example.newsclient.data.remote.END_DATE
import kotlinx.coroutines.flow.Flow
import java.io.IOException

/**
 * 新闻数据仓库接口
 * 定义了获取新闻数据的抽象方法，支持本地存储和网络请求
 */
interface NewsRepository {
    /**
     * 获取新闻列表，支持分页、分类、关键词搜索
     * @param category 新闻分类，为null表示获取所有分类
     * @param keyword 搜索关键词，为空表示不进行关键词搜索
     * @param page 页码，从1开始
     * @param pageSize 每页数量
     * @return 新闻列表
     */
    suspend fun getNews(
        category: NewsCategory? = null,
        keyword: String? = "",
        page: Int = 1,
        pageSize: Int = 15
    ): List<News>

    /**
     * 从本地数据库获取新闻详情
     * 由于API不支持根据ID获取单条新闻，所以只从本地数据库获取
     * @param newsId 新闻ID
     * @return 新闻详情，如果不存在则返回null
     */
    suspend fun getNewsFromLocal(newsId: String): News?

    /**
     * 保存新闻到本地数据库（历史记录或收藏）
     * @param news 要保存的新闻对象
     * @param isFavorite 是否标记为收藏，默认为false（仅保存到历史记录）
     */
    suspend fun saveNewsToLocal(news: News, isFavorite: Boolean = false)

    /**
     * 获取本地历史记录
     * @return 历史记录的Flow，可以实时观察数据变化
     */
    fun getHistoryNews(): Flow<List<News>>

    /**
     * 获取收藏的新闻
     * @return 收藏新闻的Flow，可以实时观察数据变化
     */
    fun getFavoriteNews(): Flow<List<News>>

    /**
     * 检查新闻是否被收藏
     * @param newsId 新闻ID
     * @return 是否被收藏
     */
    suspend fun isNewsFavorite(newsId: String): Boolean

    /**
     * 切换新闻收藏状态
     * @param newsId 新闻ID
     * @return 切换后的收藏状态
     */
    suspend fun toggleFavorite(newsId: String): Boolean

    /**
     * 清除历史记录（保留收藏的新闻）
     */
    suspend fun clearHistory()
}

/**
 * 网络新闻数据仓库实现类
 * 负责从网络API获取新闻数据，并结合本地数据库进行缓存和离线支持
 */
class NetworkNewsRepository(
    private val newsApiService: NewsApiService,  // 网络API服务
    private val newsLocalDataSource: NewsLocalDataSource       // 本地数据库 - 更新引用
): NewsRepository {

    /**
     * 获取新闻列表的实现
     * 优先从网络获取，失败时从本地缓存获取
     */
    override suspend fun getNews(
        category: NewsCategory?,
        keyword: String?,
        page: Int,
        pageSize: Int
    ): List<News> {
        return try {
            // 打印详细的请求参数用于调试
            Log.d("NetworkNewsRepository", "开始获取新闻，参数：")
            Log.d("NetworkNewsRepository", "page: $page")
            Log.d("NetworkNewsRepository", "size: $pageSize")
            Log.d("NetworkNewsRepository", "category: ${category?.value}")
            Log.d("NetworkNewsRepository", "keyword: $keyword")

            // 调用网络API获取新闻列表
            val result = newsApiService.getNewsList(
                page = page,
                size = pageSize,
                categories = category?.value,
                keyword = if (keyword.isNullOrEmpty()) null else keyword, // 避免空字符串
            ).data

            Log.d("NetworkNewsRepository", "成功获取新闻数量: ${result.size}")

            // 将获取到的新闻缓存到本地数据库，但不标记为收藏
            if (result.isNotEmpty()) {
                newsLocalDataSource.cacheNews(result)
                Log.d("NetworkNewsRepository", "新闻已缓存到本地数据库")
            }

            result
        } catch (e: retrofit2.HttpException) {
            // HTTP错误的详细处理
            Log.e("NetworkNewsRepository", "HTTP错误: ${e.code()} - ${e.message()}")
            try {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("NetworkNewsRepository", "错误响应内容: $errorBody")
            } catch (ex: Exception) {
                Log.e("NetworkNewsRepository", "无法读取错误响应", ex)
            }

            // 尝试从本地缓存获取数据
            Log.d("NetworkNewsRepository", "尝试从本地缓存获取新闻")
            val cachedNews = newsLocalDataSource.getCachedNews(category?.value, keyword)
            Log.d("NetworkNewsRepository", "从缓存获取到新闻数量: ${cachedNews.size}")
            cachedNews
        } catch (e: IOException) {
            // 网络连接异常时，从本地数据库获取缓存的新闻
            Log.e("NetworkNewsRepository", "网络连接异常，从本地缓存获取数据", e)
            val cachedNews = newsLocalDataSource.getCachedNews(category?.value, keyword)
            Log.d("NetworkNewsRepository", "从缓存获取到新闻数量: ${cachedNews.size}")
            cachedNews
        } catch (e: Exception) {
            // 其他异常也尝试从本地获取
            Log.e("NetworkNewsRepository", "获取新闻失败，从本地缓存获取数据", e)
            val cachedNews = newsLocalDataSource.getCachedNews(category?.value, keyword)
            Log.d("NetworkNewsRepository", "从缓存获取到新闻数量: ${cachedNews.size}")
            cachedNews
        }
    }

    /**
     * 从本地数据库获取新闻详情
     * 由于API不支持根据ID获取单条新闻，所以只从本地数据库获取
     */
    override suspend fun getNewsFromLocal(newsId: String): News? {
        return try {
            // 从本地数据库获取新闻详情
            newsLocalDataSource.getNewsById(newsId)
        } catch (e: Exception) {
            Log.e("NetworkNewsRepository", "从本地获取新闻详情失败", e)
            null
        }
    }

    /**
     * 保存新闻到本地数据库
     */
    override suspend fun saveNewsToLocal(news: News, isFavorite: Boolean) {
        try {
            newsLocalDataSource.saveNews(news, isFavorite)
        } catch (e: Exception) {
            Log.e("NetworkNewsRepository", "保存新闻到本地失败", e)
        }
    }

    /**
     * 获取历史记录
     */
    override fun getHistoryNews(): Flow<List<News>> {
        return newsLocalDataSource.getHistoryNews()
    }

    /**
     * 获取收藏的新闻
     */
    override fun getFavoriteNews(): Flow<List<News>> {
        return newsLocalDataSource.getFavoriteNews()
    }

    /**
     * 检查新闻是否被收藏
     */
    override suspend fun isNewsFavorite(newsId: String): Boolean {
        return try {
            newsLocalDataSource.isNewsFavorite(newsId)
        } catch (e: Exception) {
            Log.e("NetworkNewsRepository", "检查收藏状态失败", e)
            false
        }
    }

    /**
     * 切换新闻收藏状态
     */
    override suspend fun toggleFavorite(newsId: String): Boolean {
        return try {
            newsLocalDataSource.toggleFavorite(newsId)
        } catch (e: Exception) {
            Log.e("NetworkNewsRepository", "切换收藏状态失败", e)
            false
        }
    }

    /**
     * 清除历史记录
     */
    override suspend fun clearHistory() {
        try {
            newsLocalDataSource.clearHistory()
        } catch (e: Exception) {
            Log.e("NetworkNewsRepository", "清除历史记录失败", e)
        }
    }
}
