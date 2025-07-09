package com.example.newsclient.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsclient.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ui/home/NewsViewModel.kt

class NewsViewModel (
    private val repository: NewsRepository
) : ViewModel() {

    // 当前选择的分类
    private val _currentCategory = MutableStateFlow("全部")
    val currentCategory: StateFlow<String> = _currentCategory.asStateFlow()

    // 新闻列表状态
    private val _newsState = MutableStateFlow<UiState<NewsListState>>(UiState.Loading)
    val newsState: StateFlow<UiState<NewsListState>> = _newsState.asStateFlow()

    // 新闻详情状态
    private val _detailState = MutableStateFlow<UiState<NewsDetailState>>(UiState.Loading)
    val detailState: StateFlow<UiState<NewsDetailState>> = _detailState.asStateFlow()

    // 分页控制
    private var currentPage = 1
    private val pageSize = 15

    /**
     * 加载新闻列表
     * @param category 新闻分类（默认使用当前分类）
     * @param keyword 搜索关键词（可选）
     * @param refresh 是否刷新（重置分页）
     */
    fun loadNewsList(
        category: String = _currentCategory.value,
        keyword: String? = null,
        refresh: Boolean = false
    ) {
        viewModelScope.launch {
            // 重置状态
            if (refresh) {
                currentPage = 1
                _newsState.value = UiState.Loading
            }

            try {
                // 从仓库获取数据
                val news = repository.getNews(
                    page = currentPage,
                    size = pageSize,
                    category = if (category == "全部") null else category,
                    keyword = keyword
                )

                // 处理结果
                _newsState.update { currentState ->
                    val currentList = if (refresh) emptyList() else
                        (currentState as? UiState.Success)?.data?.news ?: emptyList()

                    val newList = currentList + news

                    UiState.Success(
                        NewsListState(
                            news = newList,
                            isLoading = false,
                            endReached = news.size < pageSize
                        )
                    )
                }

                // 更新页码
                currentPage++
            } catch (e: Exception) {
                _newsState.value = UiState.Error("加载失败: ${e.message}")
            }
        }
    }

    /**
     * 切换新闻分类
     * @param category 新分类
     */
    fun changeCategory(category: String) {
        _currentCategory.value = category
        loadNewsList(refresh = true)
    }

    /**
     * 加载新闻详情
     * @param newsId 新闻ID
     */
    fun loadNewsDetail(newsId: String) {
        viewModelScope.launch {
            _detailState.value = UiState.Loading
            try {
                // 优先尝试从本地获取（可能已缓存）
                //TODO:完善repository方法getNewsById
                val news = repository.getNewsById(newsId) ?:
                throw Exception("新闻未找到")

                _detailState.value = UiState.Success(
                    NewsDetailState(news = news)
                )
            } catch (e: Exception) {
                _detailState.value = UiState.Error("加载详情失败: ${e.message}")
            }
        }
    }

    /**
     * 搜索新闻
     * @param query 搜索关键词
     */
    fun searchNews(query: String) {
        viewModelScope.launch {
            _newsState.value = UiState.Loading
            currentPage = 1

            try {
                //TODO:添加搜索方法searchNews
                val news = repository.searchNews(
                    query = query,
                    page = currentPage,
                    size = pageSize
                )

                _newsState.value = UiState.Success(
                    NewsListState(
                        news = news,
                        endReached = news.size < pageSize
                    )
                )
                currentPage++
            } catch (e: Exception) {
                _newsState.value = UiState.Error("搜索失败: ${e.message}")
            }
        }
    }

    // 获取所有可用分类
    val categories: List<String>
        get() = repository.getAllCategories()
}