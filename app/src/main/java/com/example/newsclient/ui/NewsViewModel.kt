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
import com.example.newsclient.data.repository.PaginatedNewsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

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
    val hasMoreData: Boolean = true            // 是否还有更多数据
)

/**
 * 新闻详情的UI状态数据类
 * 包含新闻详情和收藏状态
 */
data class NewsDetailState(
    val news: News? = null,                    // 新闻详情数据
    val isFavorite: Boolean = false            // 是否已收藏
)

/**
 * 新闻ViewModel，负责管理新闻相关的业务逻辑和UI状态
 * 遵循MVVM架构模式，作为View和Model之间的桥梁
 */
class NewsViewModel(
    private val repository: NewsRepository     // 新闻数据仓库
) : ViewModel() {

    // === 分类和搜索状态 ===

    /**
     * 当前选择的新闻分类
     * 使用MutableStateFlow支持响应式编程
     */
    private val _currentCategory = MutableStateFlow<NewsCategory?>(null)
    val currentCategory: StateFlow<NewsCategory?> = _currentCategory.asStateFlow()

    /**
     * 当前搜索关键词
     */
    private val _searchKeyword = MutableStateFlow<String?>(null)
    val searchKeyword: StateFlow<String?> = _searchKeyword.asStateFlow()

    // === 新闻列表状态 ===

    /**
     * 新闻列表的UI状态
     * 包含加载、成功、错误等状态
     */
    private val _newsState = MutableStateFlow<UiState<NewsListState>>(UiState.Loading)
    val newsState: StateFlow<UiState<NewsListState>> = _newsState.asStateFlow()

    // === 新闻详情状态 ===

    /**
     * 新闻详情的UI状态
     */
    private val _detailState = MutableStateFlow<UiState<NewsDetailState>>(UiState.Loading)
    val detailState: StateFlow<UiState<NewsDetailState>> = _detailState.asStateFlow()

    // === 历史记录和收藏状态 ===

    /**
     * 历史记录的UI状态
     */
    private val _historyState = MutableStateFlow<UiState<List<News>>>(UiState.Loading)
    val historyState: StateFlow<UiState<List<News>>> = _historyState.asStateFlow()

    /**
     * 收藏新闻的UI状态
     */
    private val _favoritesState = MutableStateFlow<UiState<List<News>>>(UiState.Loading)
    val favoritesState: StateFlow<UiState<List<News>>> = _favoritesState.asStateFlow()

    // === 分页控制 ===

    /**
     * 当前页码，用于分页加载
     */
    private var currentPage = 1

    /**
     * 每页显示的新闻数量
     */
    private val pageSize = 15

    /**
     * ViewModel初始化
     * 在创建时自动加载初��数据
     */
    init {
        // 加载默认的新闻列表
        getNewsList()
        // 加载历史记录
        loadHistoryNews()
        // 加载收藏新闻
        loadFavoriteNews()
    }

    // === 分类和搜索相关方法 ===

    /**
     * 设置当前新闻分类并重新加载新闻
     * @param category 要设置的新闻分类，null表示全部分类
     */
    fun setCategory(category: NewsCategory?) {
        Log.d("NewsViewModel", "🏷️ setCategory() 被调用:")
        Log.d("NewsViewModel", "   新分类: ${category?.value ?: "全部"}")
        Log.d("NewsViewModel", "   当前分类: ${_currentCategory.value?.value ?: "全部"}")

        // 只有当分类真正发生变化时才重新加载
        if (_currentCategory.value != category) {
            Log.d("NewsViewModel", "✅ 分类发生变化，开始重新加载")
            _currentCategory.value = category
            refreshNewsList()
        } else {
            Log.d("NewsViewModel", "❌ 分类未变化，跳过重新加载")
        }
    }

    /**
     * 设置搜索关键词并重新加载新闻
     * @param keyword 搜索关键词，null或空字符串表示不搜索
     */
    fun setSearchKeyword(keyword: String?) {
        Log.d("NewsViewModel", "🔍 setSearchKeyword() 被调用:")
        Log.d("NewsViewModel", "   新关键词: '$keyword'")
        Log.d("NewsViewModel", "   当前关键词: '${_searchKeyword.value}'")

        // 只有当关键词真正发生变化时才重新加载
        if (_searchKeyword.value != keyword) {
            Log.d("NewsViewModel", "✅ 关键词发生变化，开始重新加载")
            _searchKeyword.value = keyword
            refreshNewsList()
        } else {
            Log.d("NewsViewModel", "❌ 关键词未变化，跳过重新加载")
        }
    }

    // === 新闻列表相关方法 ===

    /**
     * 获取新闻列表的核心方法
     * @param refresh 是否刷新（重置分页）
     * @param loadMore 是否加载更多（增加页码）
     */
    fun getNewsList(refresh: Boolean = false, loadMore: Boolean = false) {
        // 处理分页逻辑
        when {
            refresh -> {
                // 刷新时重置页码并显示加载状态
                currentPage = 1
                _newsState.value = UiState.Loading
            }
            loadMore -> {
                // 加载更多时增加页码并显示加载更多指示器
                val currentState = _newsState.value
                if (currentState is UiState.Success) {
                    _newsState.value = UiState.Success(
                        currentState.data.copy(isLoadingMore = true)
                    )
                }
                currentPage++
            }
        }

        // 在协程中执行网络请求
        viewModelScope.launch {
            try {
                // 调用Repository获取新闻数据（现在返回PaginatedNewsResult��
                val result = repository.getNews(
                    category = _currentCategory.value,
                    keyword = _searchKeyword.value,
                    page = currentPage,
                    pageSize = pageSize
                )

                // 处理获取到的数据
                handleNewsListResult(result, refresh, loadMore)

            } catch (e: IOException) {
                // 网络连接异常
                handleError("网络连接异常: ${e.message}")
            } catch (e: retrofit2.HttpException) {
                // HTTP错误
                handleError("服务器错误: ${e.message}")
            } catch (e: Exception) {
                // 其他异常
                handleError("获取新闻失败: ${e.message}")
            }
        }
    }

    /**
     * 处理新闻列表获取结果
     * @param result 分页新闻结果，包含新闻列表和分页信息
     * @param refresh 是否为刷新操作
     * @param loadMore 是否为加载更多操作
     */
    private fun handleNewsListResult(result: PaginatedNewsResult, refresh: Boolean, loadMore: Boolean) {
        Log.d("NewsViewModel", "📊 处理新闻列表结果:")
        Log.d("NewsViewModel", "   获取到新闻数量: ${result.news.size}")
        Log.d("NewsViewModel", "   总数: ${result.total}")
        Log.d("NewsViewModel", "   是否还有更多: ${result.hasMoreData}")
        Log.d("NewsViewModel", "   当前页码: $currentPage")
        Log.d("NewsViewModel", "   是否刷新: $refresh")
        Log.d("NewsViewModel", "   是否加载更多: $loadMore")

        val currentState = _newsState.value
        Log.d("NewsViewModel", "   当前状态: ${currentState::class.simpleName}")

        if (result.news.isEmpty() && currentPage == 1) {
            // 第一页且没有数据，显示空状态
            Log.d("NewsViewModel", "🔄 设置状态为Empty（第一页无数据）")
            _newsState.value = UiState.Empty
        } else {
            if (currentState is UiState.Success && loadMore) {
                // 加载更多操作，需要合并数据
                val existingNews = currentState.data.news
                val combinedNews = existingNews + result.news

                Log.d("NewsViewModel", "🔄 设置状态为Success（加载更多）")
                Log.d("NewsViewModel", "   原有新闻数量: ${existingNews.size}")
                Log.d("NewsViewModel", "   新增新闻数量: ${result.news.size}")
                Log.d("NewsViewModel", "   合并后数量: ${combinedNews.size}")

                _newsState.value = UiState.Success(
                    NewsListState(
                        news = combinedNews,
                        isRefreshing = false,
                        isLoadingMore = false,
                        hasMoreData = result.hasMoreData
                    )
                )
            } else {
                // 刷新操作或首次加载，使用全新数据
                Log.d("NewsViewModel", "🔄 设置状态为Success（刷新或首次加载）")
                Log.d("NewsViewModel", "   新闻数量: ${result.news.size}")
                Log.d("NewsViewModel", "   是否还有更多数据: ${result.hasMoreData}")

                _newsState.value = UiState.Success(
                    NewsListState(
                        news = result.news,
                        isRefreshing = false,
                        isLoadingMore = false,
                        hasMoreData = result.hasMoreData
                    )
                )
            }
        }

        Log.d("NewsViewModel", "✅ 状态更新完成，新状态: ${_newsState.value::class.simpleName}")
    }

    /**
     * 刷新新闻列表
     * 重置分页并重新加载第一页数据
     */
    fun refreshNewsList() {
        getNewsList(refresh = true)
    }

    /**
     * 加载更多新闻
     * 检查当前状态，如果满足条件则加载下一页
     */
    fun loadMoreNews() {
        Log.d("NewsViewModel", "🔄 loadMoreNews() 被调用")

        val currentState = _newsState.value
        Log.d("NewsViewModel", "📊 当前状态类型: ${currentState::class.simpleName}")

        when (currentState) {
            is UiState.Success -> {
                Log.d("NewsViewModel", "✅ 当前状态为Success")
                Log.d("NewsViewModel", "   isLoadingMore: ${currentState.data.isLoadingMore}")
                Log.d("NewsViewModel", "   hasMoreData: ${currentState.data.hasMoreData}")
                Log.d("NewsViewModel", "   当前新闻数量: ${currentState.data.news.size}")

                if (!currentState.data.isLoadingMore && currentState.data.hasMoreData) {
                    Log.d("NewsViewModel", "🚀 满足加载更多条件，开始加载下一页")
                    getNewsList(loadMore = true)
                } else {
                    if (currentState.data.isLoadingMore) {
                        Log.d("NewsViewModel", "⏳ 正在加载中，跳过此次请求")
                    }
                    if (!currentState.data.hasMoreData) {
                        Log.d("NewsViewModel", "🚫 没有更多数据，跳过此次请求")
                    }
                }
            }
            is UiState.Loading -> {
                Log.d("NewsViewModel", "⏳ 当前状态为Loading，跳过加载更多")
            }
            is UiState.Error -> {
                Log.d("NewsViewModel", "❌ 当前状态为Error，跳过加载更多")
            }
            is UiState.Empty -> {
                Log.d("NewsViewModel", "📭 当前状态为Empty，跳过加载更多")
            }
        }
    }

    // === 新闻��情相关方法 ===

    /**
     * 查看新闻详情
     * 不再通过API获取，而是直接使用传入的新闻对象
     * @param news 要查看的新闻对象
     */
    fun viewNewsDetail(news: News) {
        viewModelScope.launch {
            try {
                // 直接使用传入的新闻对象设置详情状态
                _detailState.value = UiState.Loading

                // 检查该新闻是否已被收藏
                val isFavorite = repository.isNewsFavorite(news.id)

                // 设置详情状态为成功
                _detailState.value = UiState.Success(
                    NewsDetailState(
                        news = news,
                        isFavorite = isFavorite
                    )
                )

                // 自动将该新闻保存到历史记录（不标记为收藏）
                repository.saveNewsToLocal(news, isFavorite = false)

            } catch (e: Exception) {
                _detailState.value = UiState.Error("查看新闻详情失败: ${e.message}")
            }
        }
    }

    /**
     * 切换新闻的收藏状态
     * @param newsId 要切换收藏状态的新闻ID
     */
    fun toggleFavorite(newsId: String) {
        viewModelScope.launch {
            try {
                // 调用Repository切换收藏状态
                val newFavoriteState = repository.toggleFavorite(newsId)

                // 更新详情页中的收藏状态
                val currentDetailState = _detailState.value
                if (currentDetailState is UiState.Success) {
                    _detailState.value = UiState.Success(
                        currentDetailState.data.copy(
                            isFavorite = newFavoriteState
                        )
                    )
                }

                // 刷新收藏列表以反映最新状态
                loadFavoriteNews()

            } catch (e: Exception) {
                // 收藏操作失败，这里可以显示错误提示
                // 实际应用中可以通过Snackbar���Toast提示��户
            }
        }
    }

    // === 历史记录和收藏相关方法 ===

    /**
     * 加载历史记录
     * 从本地数据库获取用户浏览过的新闻
     */
    fun loadHistoryNews() {
        viewModelScope.launch {
            _historyState.value = UiState.Loading
            repository.getHistoryNews()
                .catch { e ->
                    // 处理Flow中的异常
                    _historyState.value = UiState.Error("获取历史记录失败: ${e.message}")
                }
                .collect { historyNews ->
                    // 根据历史记录是否为空设置不同的状态
                    if (historyNews.isEmpty()) {
                        _historyState.value = UiState.Empty
                    } else {
                        _historyState.value = UiState.Success(historyNews)
                    }
                }
        }
    }

    /**
     * 加载收藏的新闻
     * 从本地数据库获取用户收藏的新闻
     */
    fun loadFavoriteNews() {
        viewModelScope.launch {
            _favoritesState.value = UiState.Loading
            repository.getFavoriteNews()
                .catch { e ->
                    // 处理Flow中的异常
                    _favoritesState.value = UiState.Error("获取收藏失败: ${e.message}")
                }
                .collect { favoriteNews ->
                    // 根据收藏新闻是否为空设置不同的状态
                    if (favoriteNews.isEmpty()) {
                        _favoritesState.value = UiState.Empty
                    } else {
                        _favoritesState.value = UiState.Success(favoriteNews)
                    }
                }
        }
    }

    /**
     * 清除历史记录
     * 删除所有浏览历史，但保留收藏的新闻
     */
    fun clearHistory() {
        viewModelScope.launch {
            try {
                repository.clearHistory()
                // 清除成功��设置为空状态
                _historyState.value = UiState.Empty
            } catch (e: Exception) {
                _historyState.value = UiState.Error("清除历史记录失败: ${e.message}")
            }
        }
    }

    // === 私有辅助方法 ===

    /**
     * 处理错误情况的统一方���
     * @param errorMessage 错误消息
     */
    private fun handleError(errorMessage: String) {
        val currentState = _newsState.value
        if (currentState is UiState.Success) {
            // 如果已经有数据，保留现有数据
            // 在实际应用中可以通过Snackbar显示错��消息
            // 这样用户仍能看到之前加载的数据
        } else {
            // 如果没有数据，显示错误状态
            _newsState.value = UiState.Error(errorMessage)
        }
    }

    // === ViewModel工厂 ===

    /**
     * ViewModel工厂，用于创建NewsViewModel实例
     * 使用依赖注入模式获取所需的Repository
     */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                // 从Application中获取依赖
                val application = (this[APPLICATION_KEY] as NewsApplication)
                NewsViewModel(repository = application.container.newsRepository)
            }
        }
    }
}