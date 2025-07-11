package com.example.newsclient.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.newsclient.data.model.News
import com.example.newsclient.data.model.NewsCategory
import com.example.newsclient.ui.NewsViewModel
import com.example.newsclient.ui.UiState

/**
 * 新闻列表主界面
 * 包含搜索栏、分类选择和新闻列表
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun NewsListScreen(
    onNewsClick: (News) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: NewsViewModel = viewModel(factory = NewsViewModel.Factory)
) {
    // 收集ViewModel状态
    val newsState by viewModel.newsState.collectAsState()
    val currentCategory by viewModel.currentCategory.collectAsState()
    val searchKeyword by viewModel.searchKeyword.collectAsState()

    // 分类列表（包含"全部"选项）
    val categories = remember {
        listOf(null) + NewsCategory.entries // null 代表"全部"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // 搜索栏
        SearchBar(
            searchKeyword = searchKeyword,
            onSearchClick = onSearchClick,
            onSearchTextChange = { viewModel.setSearchKeyword(it) }
        )

        // 分类选择栏
        CategorySelector(
            categories = categories,
            currentCategory = currentCategory,
            onCategorySelected = { viewModel.setCategory(it) }
        )

        // 新闻列表
        NewsListContent(
            newsState = newsState,
            onNewsClick = onNewsClick,
            onLoadMore = { viewModel.loadMoreNews() },
            onRefresh = { viewModel.refreshNewsList() }
        )
    }
}

/**
 * 搜索栏组件
 */
@Composable
private fun SearchBar(
    searchKeyword: String?,
    onSearchClick: () -> Unit,
    onSearchTextChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "搜索",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = searchKeyword?.takeIf { it.isNotEmpty() } ?: "搜索新闻...",
                color = if (searchKeyword.isNullOrEmpty()) Color.Gray else Color.Black,
                fontSize = 16.sp,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSearchClick() }
            )
        }
    }
}

/**
 * 分类选择组件
 */
@Composable
private fun CategorySelector(
    categories: List<NewsCategory?>,
    currentCategory: NewsCategory?,
    onCategorySelected: (NewsCategory?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        items(categories) { category ->
            CategoryChip(
                category = category,
                isSelected = currentCategory == category,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

/**
 * 单个分类标签
 */
@Composable
private fun CategoryChip(
    category: NewsCategory?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val categoryName = category?.value ?: "全部"

    Card(
        modifier = Modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = categoryName,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else Color.Black,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * 新闻列表内容
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun NewsListContent(
    newsState: UiState<com.example.newsclient.ui.NewsListState>,
    onNewsClick: (News) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit
) {
    val listState = rememberLazyListState()

    // 下拉刷新状态
    val isRefreshing = newsState is UiState.Loading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = onRefresh
    )

    // 监听滚动状态，实现无限加载
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (newsState is UiState.Success && lastVisibleIndex != null) {
                    val totalItems = newsState.data.news.size
                    if (lastVisibleIndex >= totalItems - 3 &&
                        !newsState.data.isLoadingMore &&
                        newsState.data.hasMoreData) {
                        onLoadMore()
                    }
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        when (newsState) {
            is UiState.Loading -> {
                if (!isRefreshing) {
                    LoadingContent()
                }
            }
            is UiState.Success -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    items(newsState.data.news) { news ->
                        NewsItem(
                            news = news,
                            onClick = { onNewsClick(news) }
                        )
                    }

                    // 加载更多指示器
                    if (newsState.data.isLoadingMore) {
                        item {
                            LoadingMoreIndicator()
                        }
                    }
                }
            }
            is UiState.Error -> {
                ErrorContent(
                    message = newsState.message,
                    onRetry = onRefresh
                )
            }
            is UiState.Empty -> {
                EmptyContent()
            }
        }

        // 下拉刷新指示器
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

/**
 * 单个新闻条目
 */
@Composable
private fun NewsItem(
    news: News,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 新闻图片
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(news.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "新闻图片",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.1f)),
                error = painterResource(id = android.R.drawable.ic_menu_gallery)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 新闻内容
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 新闻标题
                Text(
                    text = news.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 新闻摘要
                Text(
                    text = news.content,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 新闻元信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = news.publisher,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Text(
                        text = formatPublishTime(news.publishTime),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

/**
 * 加载中内容
 */
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "正在加载新闻...",
                color = Color.Gray
            )
        }
    }
}

/**
 * 加载更多指示器
 */
@Composable
private fun LoadingMoreIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "加载更多...",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

/**
 * 错误内容
 */
@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                color = Color.Red,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("重试")
            }
        }
    }
}

/**
 * 空内容
 */
@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "暂无新闻",
                color = Color.Gray,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "下拉刷新试试",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * 格式化发布时间
 */
private fun formatPublishTime(publishTime: String): String {
    // 简单的时间格式化，你可以根据需要优化
    return try {
        // 假设publishTime是"2024-01-01 12:00:00"格式
        val parts = publishTime.split(" ")
        if (parts.size >= 2) {
            val datePart = parts[0]
            val timePart = parts[1].substring(0, 5) // 只取小时和分钟
            "$datePart $timePart"
        } else {
            publishTime
        }
    } catch (e: Exception) {
        publishTime
    }
}
