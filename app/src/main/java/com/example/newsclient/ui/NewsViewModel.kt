package com.example.newsclient.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.newsclient.NewsApplication
import com.example.newsclient.data.model.NewsCategory
import com.example.newsclient.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// ui/home/NewsViewModel.kt

//TODO：优化ViewModel，测试ViewModel
class NewsViewModel (
    private val repository: NewsRepository // 默认使用NewsRepository实例
) : ViewModel() {

    // 当前选择的分类
    private val _currentCategory = MutableStateFlow(NewsCategory.TECHNOLOGY)
    val currentCategory: StateFlow<NewsCategory> = _currentCategory.asStateFlow()

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
     * 获取新闻列表
     * @param category 新闻分类（默认使用当前分类）
     * @param keyword 搜索关键词（可选）
     * @param refresh 是否刷新（重置分页）
     */
    fun getNewsList(
        category: NewsCategory = _currentCategory.value,
        keyword: String? = null,
        refresh: Boolean = false
    ) {
        if (refresh) {
            currentPage = 1
        }
        viewModelScope.launch {
            _newsState.value = UiState.Loading
            try {
                val newsList = repository.getNews(
                    category = category,
                    keyword = keyword
                )
                if (newsList.isEmpty()) {
                    _newsState.value = UiState.Empty
                } else {
                    _newsState.value = UiState.Success(
                        NewsListState(news = newsList)
                    )
                }
            } catch (e: IOException) {
                _newsState.value = UiState.Error("加载新闻失败: ${e.message}")
            }
            catch(e:HttpException){
                _newsState.value = UiState.Error("加载新闻失败: ${e.message}")
            }
        }
    }

    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as NewsApplication)
                NewsViewModel(repository = application.container.newsRepository)
            }
        }
    }
}