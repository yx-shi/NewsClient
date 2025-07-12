package com.example.newsclient.ui.screen

import android.util.Log
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
    onSearchClick: (NewsCategory?) -> Unit, // 修改为接收分类参数
    viewModel: NewsViewModel = viewModel(factory = NewsViewModel.Factory)
) {
    // 收集ViewModel状态
    val newsState by viewModel.newsState.collectAsState()
    val currentCategory by viewModel.currentCategory.collectAsState()
    val searchKeyword by viewModel.searchKeyword.collectAsState()

    // 添加调试日志
    LaunchedEffect(Unit) {
        Log.d("NewsListScreen", "🎯 NewsListScreen 组件初始化")
        Log.d("NewsListScreen", "   onSearchClick 函数: ${onSearchClick}")
    }

    // 分类列表（包含"全部"选项）
    val categories = remember {
        listOf(null) + NewsCategory.entries // null 代表"全部"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // 临时测试按钮 - 用于验证导航
        Button(
            onClick = {
                Log.d("TestButton", "🧪 测试按钮被点击，当前分类: ${currentCategory?.value ?: "全部"}")
                try {
                    onSearchClick(currentCategory)
                    Log.d("TestButton", "✅ 测试按钮调用 onSearchClick 成功")
                } catch (e: Exception) {
                    Log.e("TestButton", "❌ 测试按钮调用 onSearchClick 失败", e)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("测试搜索导航 - 当前分类: ${currentCategory?.value ?: "全部"}")
        }

        // 搜索栏
        SearchBar(
            searchKeyword = searchKeyword,
            onSearchClick = {
                Log.d("NewsListScreen", "🔍 SearchBar 回调被触发，当前分类: ${currentCategory?.value ?: "全部"}")
                try {
                    onSearchClick(currentCategory) // 传递当前分类
                    Log.d("NewsListScreen", "✅ SearchBar 调用 onSearchClick 成功")
                } catch (e: Exception) {
                    Log.e("NewsListScreen", "❌ SearchBar 调用 onSearchClick 失败", e)
                }
            },
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
                .clickable {
                    Log.d("SearchBar", "🔍 搜索栏被点击 - 准备导航")
                    onSearchClick()
                }
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
                modifier = Modifier.weight(1f)
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
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            // 返回一个包含必要信息的数据类
            ScrollInfo(
                lastVisibleIndex = lastVisibleItemIndex,
                totalItems = totalItems
            )
        }.collect { scrollInfo ->
            Log.d("NewsListContent", "📱 滚动状态更新:")
            Log.d("NewsListContent", "   当前可见最后一个item索引: ${scrollInfo.lastVisibleIndex}")
            Log.d("NewsListContent", "   总item数量: ${scrollInfo.totalItems}")

            // 当滚动到倒数第2个item时触发加载更多（提前触发）
            // 修改逻辑：如果有数据显示，就认为状态正常，不完全依赖newsState
            if (scrollInfo.totalItems > 0) {
                val isNearBottom = scrollInfo.lastVisibleIndex >= scrollInfo.totalItems - 2
                val hasEnoughItems = scrollInfo.totalItems >= 5

                Log.d("NewsListContent", "🔍 检查加载更多条件:")
                Log.d("NewsListContent", "   当前状态: ${newsState::class.simpleName}")
                Log.d("NewsListContent", "   isNearBottom: $isNearBottom (${scrollInfo.lastVisibleIndex} >= ${scrollInfo.totalItems - 2})")
                Log.d("NewsListContent", "   hasEnoughItems: $hasEnoughItems")

                // 修改条件：只要有数据且接近底部就尝试加载更多
                if (isNearBottom && hasEnoughItems) {
                    // 检查是否为Success状态且满足加载更多条件
                    if (newsState is UiState.Success) {
                        val canLoadMore = !newsState.data.isLoadingMore && newsState.data.hasMoreData
                        Log.d("NewsListContent", "   Success状态 - isLoadingMore: ${newsState.data.isLoadingMore}")
                        Log.d("NewsListContent", "   Success状态 - hasMoreData: ${newsState.data.hasMoreData}")
                        Log.d("NewsListContent", "   Success状态 - canLoadMore: $canLoadMore")

                        if (canLoadMore) {
                            Log.d("NewsListContent", "🚀 Success状态满足条件，触发加载更多")
                            onLoadMore()
                        } else {
                            Log.d("NewsListContent", "❌ Success状态但不满足加载更多条件")
                        }
                    } else {
                        // 即使状态不是Success，但如果有数据且满足其他条件，也尝试触发
                        Log.d("NewsListContent", "⚠️ 状态不是Success但有数据，尝试触发加载更多")
                        onLoadMore()
                    }
                } else {
                    if (!isNearBottom) {
                        Log.d("NewsListContent", "❌ 未接近底部，不触发加载")
                    }
                    if (!hasEnoughItems) {
                        Log.d("NewsListContent", "❌ 数据量不足5个，不触发")
                    }
                }
            } else {
                Log.d("NewsListContent", "❌ 总数量为0，不检查加载更多")
                if (newsState !is UiState.Success) {
                    Log.d("NewsListContent", "❌ 状态不是Success: ${newsState::class.simpleName}")
                    // 额外调试：如果状态不是Success，输出更多信息
                    when (newsState) {
                        is UiState.Loading -> {
                            Log.d("NewsListContent", "   状态详情: 正在加载中")
                        }
                        is UiState.Error -> {
                            Log.d("NewsListContent", "   状态详情: 错误 - ${newsState.message}")
                        }
                        is UiState.Empty -> {
                            Log.d("NewsListContent", "   状态详情: 空数据")
                        }
                        else -> {
                            Log.d("NewsListContent", "   状态详情: 未知状态")
                        }
                    }
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
                    items(
                        items = newsState.data.news,
                        key = { news -> news.id } // 添加key以优化性能
                    ) { news ->
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

                    // 如果没有更多数据，显示底部提示
                    if (!newsState.data.hasMoreData && newsState.data.news.isNotEmpty()) {
                        item {
                            NoMoreDataIndicator()
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
 * 滚动信息数据类
 * 用于传递滚动状态信息
 */
private data class ScrollInfo(
    val lastVisibleIndex: Int,
    val totalItems: Int
)

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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
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

            Spacer(modifier = Modifier.height(8.dp))

            // 新闻图片（只有在有有效图片URL时才显示）
            val processedImageUrl = news.imageUrl.let { url ->
                when {
                    url.isBlank() -> ""
                    url == "[]" -> ""
                    url.startsWith("[") && url.endsWith("]") -> {
                        // 处理可能的数组格式，提取第一张有效图片
                        url.substring(1, url.length - 1)
                            .split(",")
                            .asSequence()
                            .map { it.trim().removePrefix("\"").removeSuffix("\"") }
                            .filter { it.isNotEmpty() && (it.startsWith("http://") || it.startsWith("https://")) }
                            .firstOrNull() ?: ""
                    }
                    url.startsWith("http://") || url.startsWith("https://") -> url
                    else -> ""
                }
            }

            if (processedImageUrl.isNotEmpty()) {
                // 智能图片加载：先尝试HTTPS，失败后自动回退到HTTP
                var finalImageUrl by remember { mutableStateOf(processedImageUrl) }
                var hasTriedFallback by remember { mutableStateOf(false) }

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(finalImageUrl)
                        .crossfade(true)
                        .listener(
                            onError = { _, result ->
                                android.util.Log.w("NewsItem", "图片加载失败: $finalImageUrl, 错误: ${result.throwable.message}")

                                // 如果是HTTPS失败且还没尝试过HTTP回退，则尝试HTTP
                                if (!hasTriedFallback && finalImageUrl.startsWith("https://")) {
                                    val httpUrl = finalImageUrl.replaceFirst("https://", "http://")
                                    android.util.Log.i("NewsItem", "尝试HTTP回退: $httpUrl")
                                    finalImageUrl = httpUrl
                                    hasTriedFallback = true
                                }
                            }
                        )
                        .build(),
                    contentDescription = "新闻图片",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

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
 * 没有更多数据的指示器
 */
@Composable
private fun NoMoreDataIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "— 已经到底了 —",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp)
        )
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
