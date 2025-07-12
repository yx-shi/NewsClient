package com.example.newsclient.data.repository

import android.util.Log
import com.example.newsclient.data.model.News
import com.example.newsclient.data.remote.NewsApiService
import com.example.newsclient.data.model.NewsCategory
import com.example.newsclient.data.remote.END_DATE
import kotlinx.coroutines.flow.Flow
import java.io.IOException

/**
 * 分页新闻数据结果
 * 包含新闻列表和分页相关信息
 */
data class PaginatedNewsResult(
    val news: List<News>,
    val total: Int,
    val hasMoreData: Boolean
)

/**
 * 新闻数据仓库接口
 * 定义了获取新闻数据的抽象方法，支持本地存储和网络请求
 */
interface NewsRepository {
    /**
     * 获取新闻列表，支持分页、分类、关键词搜索
     * @param category 新闻分类，为null表示获取所有分类
     * @param keyword 搜�����关键词，为空表示不进行关键词搜索
     * @param page 页码，从1开始
     * @param pageSize 每页数量
     * @return 分页新闻结果，包含新闻列表和分页信息
     */
    suspend fun getNews(
        category: NewsCategory? = null,
        keyword: String? = "",
        page: Int = 1,
        pageSize: Int = 15
    ): PaginatedNewsResult

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
     * 清除历史记录（保留收藏的��闻）
     */
    suspend fun clearHistory()

    /**
     * 搜索新闻
     * @param keyword 搜索关键词
     * @param category 搜索范围分类，null表示在所有分类中搜索
     * @return 搜索结果
     */
    suspend fun searchNews(
        keyword: String,
        category: NewsCategory? = null
    ): PaginatedNewsResult

    /**
     * 按时间搜索新闻
     * @param dateQuery 时间查询字符串 (YYYY-MM-DD格式)
     * @param category 搜索范围分类，null表示在所有分类中搜索
     * @return 搜索结果
     */
    suspend fun searchNewsByDate(
        dateQuery: String,
        category: NewsCategory? = null
    ): PaginatedNewsResult

    /**
     * 组合搜索新闻（关键词+时间）
     * @param keyword 搜索关键词
     * @param dateQuery 时间查询字符串 (YYYY-MM-DD格式)
     * @param category 搜索范围分类，null表示在所有分类中搜索
     * @return 搜索结果
     */
    suspend fun searchNewsCombined(
        keyword: String,
        dateQuery: String,
        category: NewsCategory? = null
    ): PaginatedNewsResult
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
    ): PaginatedNewsResult {
        return try {
            // 打印详细的请求参数用于调试
            Log.d("NetworkNewsRepository", "📡 开始获取新闻，参数：")
            Log.d("NetworkNewsRepository", "   page: $page")
            Log.d("NetworkNewsRepository", "   size: $pageSize")
            Log.d("NetworkNewsRepository", "   category: ${category?.value}")
            Log.d("NetworkNewsRepository", "   keyword: $keyword")

            // 调用网络API获取新闻列表
            val response = newsApiService.getNewsList(
                page = page,
                size = pageSize,
                categories = category?.value ?: "",
                keyword = keyword ?: ""
            )

            // 打印服务器响应信息
            Log.d("NetworkNewsRepository", "📊 服务器响应：")
            Log.d("NetworkNewsRepository", "   total: ${response.total}")
            Log.d("NetworkNewsRepository", "   pageSize: ${response.pageSize}")
            Log.d("NetworkNewsRepository", "   data.size: ${response.data.size}")

            // 计算是否还有更多数据
            // 注意：使用实际请求的pageSize而不是服务器返回的pageSize（可能为0）
            val currentTotal = page * pageSize
            val hasMoreData = currentTotal < response.total

            Log.d("NetworkNewsRepository", "📈 分页计算：")
            Log.d("NetworkNewsRepository", "   请求页码: $page")
            Log.d("NetworkNewsRepository", "   请求每页数量: $pageSize")
            Log.d("NetworkNewsRepository", "   当前已获取数量: $currentTotal")
            Log.d("NetworkNewsRepository", "   实际返回数量: ${response.data.size}")
            Log.d("NetworkNewsRepository", "   服务器总数: ${response.total}")
            Log.d("NetworkNewsRepository", "   是否还有更多: $hasMoreData")

            // 将获取到的新闻缓存到本地数据库，但不标记为收藏
            if (response.data.isNotEmpty()) {
                newsLocalDataSource.cacheNews(response.data)
                Log.d("NetworkNewsRepository", "💾 新闻已缓存到本地数据库")
            }

            // 返回分页新闻结果
            PaginatedNewsResult(response.data, response.total, hasMoreData)
        } catch (e: retrofit2.HttpException) {
            // HTTP错误的详细处理
            Log.e("NetworkNewsRepository", "❌ HTTP错误: ${e.code()} - ${e.message()}")
            try {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("NetworkNewsRepository", "错误响应内容: $errorBody")
            } catch (ex: Exception) {
                Log.e("NetworkNewsRepository", "无法读取错误响应", ex)
            }

            // 尝试从本地缓存获取数据
            Log.d("NetworkNewsRepository", "🔄 尝试从本地缓存获取新闻")
            val cachedNews = newsLocalDataSource.getCachedNews(category?.value, keyword)
            Log.d("NetworkNewsRepository", "📦 从缓存获取到新闻数量: ${cachedNews.size}")
            PaginatedNewsResult(cachedNews, cachedNews.size, false)
        } catch (e: IOException) {
            // 网络连接异常时，从本地数据库获取缓存的新闻
            Log.e("NetworkNewsRepository", "🌐 网络连接异常，从本地缓存获取数据", e)
            val cachedNews = newsLocalDataSource.getCachedNews(category?.value, keyword)
            Log.d("NetworkNewsRepository", "📦 从缓存获取到新闻数量: ${cachedNews.size}")
            PaginatedNewsResult(cachedNews, cachedNews.size, false)
        } catch (e: Exception) {
            // 其他异常也尝试从本地获取
            Log.e("NetworkNewsRepository", "💥 获取新闻失败，从本地缓存获取数据", e)
            val cachedNews = newsLocalDataSource.getCachedNews(category?.value, keyword)
            Log.d("NetworkNewsRepository", "📦 从缓存��取到新闻数量: ${cachedNews.size}")
            PaginatedNewsResult(cachedNews, cachedNews.size, false)
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

    /**
     * 搜索新闻
     */
    override suspend fun searchNews(
        keyword: String,
        category: NewsCategory?
    ): PaginatedNewsResult {
        return try {
            // 打印搜索参数
            Log.d("NetworkNewsRepository", "🔍 开始搜索新闻，关键词：$keyword")
            Log.d("NetworkNewsRepository", "   分类: ${category?.value}")

            // 调用网络API进行新闻搜索
            val response = newsApiService.searchNews(
                keyword = keyword,
                categories = category?.value ?: ""
            )

            // 打印搜索结果信息
            Log.d("NetworkNewsRepository", "📊 搜索结果：")
            Log.d("NetworkNewsRepository", "   total: ${response.total}")
            Log.d("NetworkNewsRepository", "   data.size: ${response.data.size}")

            // 返回搜索结果，分页信息由总数和每页大小计算得出
            PaginatedNewsResult(response.data, response.total, response.total > response.data.size)
        } catch (e: retrofit2.HttpException) {
            // HTTP错误的详细处理
            Log.e("NetworkNewsRepository", "❌ HTTP错误: ${e.code()} - ${e.message()}")
            try {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("NetworkNewsRepository", "错误响应内容: $errorBody")
            } catch (ex: Exception) {
                Log.e("NetworkNewsRepository", "无法读取错误响应", ex)
            }

            // 返回空的分页结果
            PaginatedNewsResult(emptyList(), 0, false)
        } catch (e: Exception) {
            // 其他异常返回空的分页结果
            Log.e("NetworkNewsRepository", "💥 搜索新闻失败", e)
            PaginatedNewsResult(emptyList(), 0, false)
        }
    }

    /**
     * 按时间搜索新闻
     */
    override suspend fun searchNewsByDate(
        dateQuery: String,
        category: NewsCategory?
    ): PaginatedNewsResult {
        return try {
            // 打印搜索参数
            Log.d("NetworkNewsRepository", "🕒 按时间搜索新闻，日期：$dateQuery")
            Log.d("NetworkNewsRepository", "   分类: ${category?.value}")

            // 解析日期查询参数
            val (startDate, endDate) = parseDateQuery(dateQuery)
            Log.d("NetworkNewsRepository", "=== 日期解析结果 ===")
            Log.d("NetworkNewsRepository", "   原始日期查询: '$dateQuery'")
            Log.d("NetworkNewsRepository", "   解析后startDate: '$startDate'")
            Log.d("NetworkNewsRepository", "   解析后endDate: '$endDate'")

            // 调用网络API进行新闻搜索
            val response = newsApiService.searchNewsByDate(
                dateQuery = startDate,
                endDate = endDate,
                categories = category?.value ?: ""
            )

            // 打印搜索结果信息
            Log.d("NetworkNewsRepository", "📊 搜索结果：")
            Log.d("NetworkNewsRepository", "   total: ${response.total}")
            Log.d("NetworkNewsRepository", "   data.size: ${response.data.size}")

            // 返回搜索结果，分页信息由总数和每页大小计算得出
            PaginatedNewsResult(response.data, response.total, response.total > response.data.size)
        } catch (e: retrofit2.HttpException) {
            // HTTP错误的详细处理
            Log.e("NetworkNewsRepository", "❌ HTTP错误: ${e.code()} - ${e.message()}")
            try {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("NetworkNewsRepository", "错误响应内容: $errorBody")
            } catch (ex: Exception) {
                Log.e("NetworkNewsRepository", "无法读取错误响应", ex)
            }

            // 返回空的分页结果
            PaginatedNewsResult(emptyList(), 0, false)
        } catch (e: Exception) {
            // 其他异常返回空的分页结果
            Log.e("NetworkNewsRepository", "💥 按时间搜索新闻失败", e)
            PaginatedNewsResult(emptyList(), 0, false)
        }
    }

    /**
     * 组合搜索新闻（关键词+时间）
     */
    override suspend fun searchNewsCombined(
        keyword: String,
        dateQuery: String,
        category: NewsCategory?
    ): PaginatedNewsResult {
        return try {
            // 打印搜索参数
            Log.d("NetworkNewsRepository", "🔍 开始组合搜索新闻，关键词：$keyword，日期：$dateQuery")
            Log.d("NetworkNewsRepository", "   分类: ${category?.value}")

            // 解析日期查询参数
            val (startDate, endDate) = parseDateQuery(dateQuery)
            Log.d("NetworkNewsRepository", "=== 组合搜索日期解析结果 ===")
            Log.d("NetworkNewsRepository", "   原始日期查询: '$dateQuery'")
            Log.d("NetworkNewsRepository", "   解析后startDate: '$startDate'")
            Log.d("NetworkNewsRepository", "   解析后endDate: '$endDate'")

            // 调用网络API进行新闻搜索
            val response = newsApiService.searchNewsCombined(
                keyword = keyword,
                dateQuery = startDate,
                endDate = endDate,
                categories = category?.value ?: ""
            )

            // 打印搜索结果信息
            Log.d("NetworkNewsRepository", "📊 组合搜索结果：")
            Log.d("NetworkNewsRepository", "   total: ${response.total}")
            Log.d("NetworkNewsRepository", "   data.size: ${response.data.size}")

            // 返回搜索结果，分页信息由总数和每页大小计算得出
            PaginatedNewsResult(response.data, response.total, response.total > response.data.size)
        } catch (e: retrofit2.HttpException) {
            // HTTP错误的详细处理
            Log.e("NetworkNewsRepository", "❌ 组合搜索HTTP错误: ${e.code()} - ${e.message()}")
            try {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("NetworkNewsRepository", "错误响应内容: $errorBody")
            } catch (ex: Exception) {
                Log.e("NetworkNewsRepository", "无法读取错误响应", ex)
            }

            // 返回空的分页结果
            PaginatedNewsResult(emptyList(), 0, false)
        } catch (e: Exception) {
            // 其他异常返回空的分页结果
            Log.e("NetworkNewsRepository", "💥 组合搜索新闻失败", e)
            PaginatedNewsResult(emptyList(), 0, false)
        }
    }

    /**
     * 解析日期查询参数
     * 将各种日期格式转换为API需要的startDate和endDate
     */
    private fun parseDateQuery(dateQuery: String): Pair<String, String> {
        Log.d("NetworkNewsRepository", "=== parseDateQuery ===")
        Log.d("NetworkNewsRepository", "输入: '$dateQuery'")

        return when {
            // 处理逗号分隔的日期范围：2025-02-01,2025-02-28
            dateQuery.contains(",") -> {
                val dates = dateQuery.split(",")
                val startDate = dates[0].trim()
                val endDate = dates[1]. trim()
                Log.d("NetworkNewsRepository", "逗号分隔格式：startDate='$startDate', endDate='$endDate'")
                Pair(startDate, endDate)
            }
            // 处理中文"至"分隔的日期范围：2025-02-01至2025-02-28
            dateQuery.contains("至") -> {
                val dates = dateQuery.split("至")
                val startDate = dates[0].trim()
                val endDate = dates[1]. trim()
                Log.d("NetworkNewsRepository", "中文至分隔格式：startDate='$startDate', endDate='$endDate'")
                Pair(startDate, endDate)
            }
            // 单个日期：2025-02-01
            else -> {
                Log.d("NetworkNewsRepository", "单个日期格式：startDate='$dateQuery', endDate='$dateQuery'")
                Pair(dateQuery, dateQuery)
            }
        }
    }
}
