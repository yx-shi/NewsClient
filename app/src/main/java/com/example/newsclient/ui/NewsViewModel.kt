package com.example.newsclient.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.newsclient.NewsApplication
import com.example.newsclient.data.model.News
import com.example.newsclient.data.model.NewsCategory
import com.example.newsclient.data.repository.NewsRepository
import com.example.newsclient.data.local.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI状态封装类，用于表示不同的加载状态
 * 使用sealed class确保状态的类型安全
 */
sealed class UiState<out T> {
    /** 加载中状态 */
    data object Loading : UiState<Nothing>()
    /** 空数据状态 */
    data object Empty : UiState<Nothing>()
    /** 成功状态，包含具体数据 */
    data class Success<T>(val data: T) : UiState<T>()
    /** 错误状态，包含错误信息 */
    data class Error(val message: String) : UiState<Nothing>()
}

/**
 * 新闻列表的UI状态数据类
 * 包含新闻列表和分页相关的状态信息
 */
data class NewsListState(
    val news: List<News> = emptyList(),        // 新闻列表数据
    val isRefreshing: Boolean = false,         // 是否正在刷新
    val isLoadingMore: Boolean = false,        // 是否正在加载更多
    val hasMoreData: Boolean = true,           // 是否还有更多数据
    val readNewsIds: Set<String> = emptySet()  // 已读新闻ID集合
)

/**
 * 新闻ViewModel，负责管理新闻相关的业务逻辑和UI状态
 * 遵循MVVM架构模式，作为View和Model之间的桥梁
 */
class NewsViewModel(
    private val repository: NewsRepository,     // 新闻数据仓库
    private val userPreferences: UserPreferences  // 用户偏好设置管理器
) : ViewModel() {

    // === 分类和搜索状态 ===

    /**
     * 当前选择的新闻分类
     * 使用MutableStateFlow支持响应式编程
     */
    private val _currentCategory = MutableStateFlow<NewsCategory?>(null)
    val currentCategory: StateFlow<NewsCategory?> = _currentCategory.asStateFlow()

    /**
     * 用户选择的分类列表
     * 从用户偏好设置中获取
     */
    private val _userCategories = MutableStateFlow<List<NewsCategory>>(emptyList())
    val userCategories: StateFlow<List<NewsCategory>> = _userCategories.asStateFlow()

    /**
     * 已读新闻ID集合
     */
    private val _readNewsIds = MutableStateFlow<Set<String>>(emptySet())

    // === 新闻列表状态 ===

    /**
     * 新闻列表的UI状态
     * 包含新闻数据、加载状态、分页状态等
     */
    private val _newsListState = MutableStateFlow(NewsListState())
    val newsListState: StateFlow<NewsListState> = _newsListState.asStateFlow()

    // === 分页相关变量 ===

    /**
     * 当前页码，用于分页加载
     */
    private var currentPage = 1

    /**
     * 每页显示的新闻数量
     */
    private val pageSize = 15

    init {
        // 初始化用户分类和已读状态
        loadUserCategories()
        loadReadNewsIds()

        // 添加ViewModel初始化日志
        Log.d("NewsViewModel", "🎯 NewsViewModel 初始化完成")
    }

    // === 初始化相关方法 ===

    /**
     * 加载用户分类设置
     */
    private fun loadUserCategories() {
        viewModelScope.launch {
            userPreferences.getUserCategories().collect { categories ->
                Log.d("NewsViewModel", "🏷️ 用户分类加载完成: ${categories.map { it.value }}")
                _userCategories.value = categories

                // 如果当前分类为空，设置默认分类
                if (_currentCategory.value == null) {
                    Log.d("NewsViewModel", "🔄 当前分类为空，设置默认分类")
                    if (categories.isNotEmpty()) {
                        Log.d("NewsViewModel", "   设置为第一个分类: ${categories.first().value}")
                        _currentCategory.value = categories.first()
                    } else {
                        Log.d("NewsViewModel", "   设置为全部分类")
                        _currentCategory.value = null
                    }
                    loadNews(refresh = true)
                }
            }
        }
    }

    /**
     * 加载已读新闻ID
     */
    private fun loadReadNewsIds() {
        viewModelScope.launch {
            val readIds = userPreferences.getReadNewsIds()
            _readNewsIds.value = readIds
            // 更新新闻列表状态中的已读ID
            _newsListState.value = _newsListState.value.copy(readNewsIds = readIds)
        }
    }

    /**
     * 强制刷新当前分类的新闻
     * 用于解决分类切换不响应的问题
     */
    fun forceRefreshCurrentCategory() {
        Log.d("NewsViewModel", "🔄 强制刷新当前分类")
        Log.d("NewsViewModel", "   当前分类: ${_currentCategory.value?.value ?: "全部"}")

        // 清空当前列表并重新加载
        _newsListState.value = _newsListState.value.copy(
            news = emptyList(),
            isRefreshing = true
        )

        loadNews(refresh = true)
    }

    // === 新闻列表相关方法 ===

    /**
     * 加载新闻列表
     * @param refresh 是否为刷新操作
     */
    fun loadNews(refresh: Boolean = false) {
        viewModelScope.launch {
            try {
                // 更新加载状态
                if (refresh) {
                    _newsListState.value = _newsListState.value.copy(isRefreshing = true)
                    currentPage = 1
                } else {
                    _newsListState.value = _newsListState.value.copy(isLoadingMore = true)
                }

                // 调用Repository获取新闻数据
                val result = repository.getNews(
                    category = _currentCategory.value,
                    page = currentPage,
                    pageSize = pageSize
                )

                // 根据结果更新UI状态
                val newsList = if (refresh) {
                    result.news
                } else {
                    _newsListState.value.news + result.news
                }

                _newsListState.value = _newsListState.value.copy(
                    news = newsList,
                    isRefreshing = false,
                    isLoadingMore = false,
                    hasMoreData = result.hasMoreData,
                    readNewsIds = _readNewsIds.value
                )

                // 如果成功加载，页码+1
                if (!refresh) {
                    currentPage++
                }

            } catch (e: Exception) {
                _newsListState.value = _newsListState.value.copy(
                    isRefreshing = false,
                    isLoadingMore = false
                )
                Log.e("NewsViewModel", "加载新闻异常", e)
            }
        }
    }

    /**
     * 加载更多新闻
     */
    fun loadMoreNews() {
        if (!_newsListState.value.isLoadingMore &&
            !_newsListState.value.isRefreshing &&
            _newsListState.value.hasMoreData) {
            loadNews(refresh = false)
        }
    }

    /**
     * 刷新新闻列表
     */
    fun refreshNews() {
        loadNews(refresh = true)
    }

    /**
     * 切换新闻分类
     */
    fun selectCategory(category: NewsCategory?) {
        Log.d("NewsViewModel", "🔄 selectCategory 被调用")
        Log.d("NewsViewModel", "   当前分类: ${_currentCategory.value?.value ?: "全部"}")
        Log.d("NewsViewModel", "   目标分类: ${category?.value ?: "全部"}")

        if (_currentCategory.value != category) {
            Log.d("NewsViewModel", "✅ 分类发生变化，开始切换")
            _currentCategory.value = category

            // 立即更新新闻列表状态，清空当前列表
            _newsListState.value = _newsListState.value.copy(
                news = emptyList(),
                isRefreshing = true
            )

            Log.d("NewsViewModel", "🔄 开始加载新分类的新闻")
            loadNews(refresh = true)
        } else {
            Log.d("NewsViewModel", "⏭️ 分类无变化，跳过切换")
        }
    }

    /**
     * 标记新闻为已读并添加到历史记录
     */
    fun markNewsAsRead(news: News) {
        viewModelScope.launch {
            try {
                // 添加到历史记录
                userPreferences.addToHistory(news)

                // 更新已读状态
                val updatedReadIds = _readNewsIds.value + news.id
                _readNewsIds.value = updatedReadIds

                // 更新新闻列表状态
                _newsListState.value = _newsListState.value.copy(
                    readNewsIds = updatedReadIds
                )

            } catch (e: Exception) {
                Log.e("NewsViewModel", "标记新闻已读失败", e)
            }
        }
    }

    /**
     * 检查新闻是否已读
     */
    fun isNewsRead(newsId: String): Boolean {
        return _readNewsIds.value.contains(newsId)
    }

    // === 搜索相关方法 ===

    /**
     * 搜索新闻（关键词搜索）
     * @param keyword 搜索关键词
     * @param category 搜索范围分类
     * @return 搜索结果的StateFlow
     */
    fun searchNews(
        keyword: String,
        category: NewsCategory? = null
    ): StateFlow<UiState<List<News>>> {
        val searchState = MutableStateFlow<UiState<List<News>>>(UiState.Loading)

        viewModelScope.launch {
            try {
                searchState.value = UiState.Loading

                val result = repository.getNews(
                    keyword = keyword,
                    category = category
                )

                searchState.value = if (result.news.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(result.news)
                }

            } catch (e: Exception) {
                searchState.value = UiState.Error("搜索失败: ${e.message}")
            }
        }

        return searchState.asStateFlow()
    }

    /**
     * 按时间搜索新闻
     * @param dateQuery 时间查询字符串 (YYYY-MM-DD格式)
     * @param category 搜索范围分类
     * @return 搜索结果的StateFlow
     */
    fun searchNewsByDate(
        dateQuery: String,
        category: NewsCategory? = null
    ): StateFlow<UiState<List<News>>> {
        val searchState = MutableStateFlow<UiState<List<News>>>(UiState.Loading)

        viewModelScope.launch {
            try {
                Log.d("NewsViewModel", "🕒 开始按时间搜索新闻")
                Log.d("NewsViewModel", "   日期查询: '$dateQuery'")
                Log.d("NewsViewModel", "   分类: ${category?.value}")

                searchState.value = UiState.Loading

                val result = repository.searchNewsByDate(
                    dateQuery = dateQuery,
                    category = category
                )

                Log.d("NewsViewModel", "📊 按时间搜索结果: ${result.news.size} 条新闻")

                searchState.value = if (result.news.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(result.news)
                }

            } catch (e: Exception) {
                Log.e("NewsViewModel", "按时间搜索失败", e)
                searchState.value = UiState.Error("按时间搜索失败: ${e.message}")
            }
        }

        return searchState.asStateFlow()
    }

    /**
     * 组合搜索新闻（关键词+时间）
     * @param keyword 搜索关键词
     * @param dateQuery 时间查询字符串 (YYYY-MM-DD格式)
     * @param category 搜索范围分类
     * @return 搜索结果的StateFlow
     */
    fun searchNewsCombined(
        keyword: String,
        dateQuery: String,
        category: NewsCategory? = null
    ): StateFlow<UiState<List<News>>> {
        val searchState = MutableStateFlow<UiState<List<News>>>(UiState.Loading)

        viewModelScope.launch {
            try {
                Log.d("NewsViewModel", "🔍 开始组合搜索新闻")
                Log.d("NewsViewModel", "   关键词: '$keyword'")
                Log.d("NewsViewModel", "   日期查询: '$dateQuery'")
                Log.d("NewsViewModel", "   分类: ${category?.value}")

                searchState.value = UiState.Loading

                val result = repository.searchNewsCombined(
                    keyword = keyword,
                    dateQuery = dateQuery,
                    category = category
                )

                Log.d("NewsViewModel", "📊 组合搜索结果: ${result.news.size} 条新闻")

                searchState.value = if (result.news.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(result.news)
                }

            } catch (e: Exception) {
                Log.e("NewsViewModel", "组合搜索失败", e)
                searchState.value = UiState.Error("组合搜索失败: ${e.message}")
            }
        }

        return searchState.asStateFlow()
    }

    // === ViewModel工厂 ===

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as NewsApplication)
                NewsViewModel(
                    repository = application.container.newsRepository,
                    userPreferences = application.userPreferences
                )
            }
        }
    }
}
