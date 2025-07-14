package com.example.newsclient.ui.screen

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    onCategoryManageClick: () -> Unit = {}, // 新增：分类管理点击回调
    viewModel: NewsViewModel = viewModel(factory = NewsViewModel.Factory)
) {
    // 收集ViewModel状态
    val newsListState by viewModel.newsListState.collectAsState()
    val currentCategory by viewModel.currentCategory.collectAsState()
    val userCategories by viewModel.userCategories.collectAsState()

    // 添加调试日志
    LaunchedEffect(Unit) {
        Log.d("NewsListScreen", "🎯 NewsListScreen 组件初始化")
        Log.d("NewsListScreen", "   onSearchClick 函数: ${onSearchClick}")
    }

    // 监听分类和新闻列表状态的变化
    LaunchedEffect(currentCategory, newsListState.news.size) {
        Log.d("NewsListScreen", "📊 状态变化监听")
        Log.d("NewsListScreen", "   当前分类: ${currentCategory?.value ?: "全部"}")
        Log.d("NewsListScreen", "   新闻列表大小: ${newsListState.news.size}")
        Log.d("NewsListScreen", "   是否正在刷新: ${newsListState.isRefreshing}")
    }

    // 添加一个检测机制，如果分类切换后5秒内新闻列表还是空的，则强制刷新
    LaunchedEffect(currentCategory) {
        val category = currentCategory
        if (category != null) {
            Log.d("NewsListScreen", "⏰ 开始5秒超时检测：${category.value}")
            kotlinx.coroutines.delay(5000) // 等待5秒
            if (newsListState.news.isEmpty() && !newsListState.isRefreshing) {
                Log.w("NewsListScreen", "⚠️ 检测到分类切换超时，强制刷新")
                viewModel.forceRefreshCurrentCategory()
            }
        }
    }

    // 分类列表（包含"全部"选项 + 用户自定义分类）
    val categories = remember(userCategories) {
        listOf(null) + userCategories // null 代表"全部"，然后是用户选择的分类
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // 搜索栏
        SearchBar(
            onSearchClick = {
                Log.d("NewsListScreen", "🔍 SearchBar 回调被触发，当前分类: ${currentCategory?.value ?: "全部"}")
                try {
                    onSearchClick(currentCategory) // 传递当前分类
                    Log.d("NewsListScreen", "✅ SearchBar 调用 onSearchClick 成功")
                } catch (e: Exception) {
                    Log.e("NewsListScreen", "❌ SearchBar 调用 onSearchClick 失败", e)
                }
            }
        )

        // 分类选择栏（包含管理按钮）
        CategorySelector(
            categories = categories,
            currentCategory = currentCategory,
            onCategorySelected = { viewModel.selectCategory(it) },
            onManageClick = onCategoryManageClick
        )

        // 新闻列表
        NewsListContent(
            newsListState = when {
                newsListState.isRefreshing -> UiState.Loading
                newsListState.news.isEmpty() && !newsListState.isRefreshing && !newsListState.isLoadingMore -> UiState.Empty
                else -> UiState.Success(newsListState)
            },
            onNewsClick = { news ->
                // 点击新闻时标记为已读
                viewModel.markNewsAsRead(news)
                onNewsClick(news)
            },
            onLoadMore = { viewModel.loadMoreNews() },
            onRefresh = { viewModel.refreshNews() }
        )
    }
}

/**
 * 搜索栏组件
 */
@Composable
private fun SearchBar(
    onSearchClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    Log.d("SearchBar", "🔍 搜索栏被点击 - 准备导航")
                    onSearchClick()
                }
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "搜索",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "搜索新闻、关键词...",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )

            // 添加搜索快捷提示
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "搜索",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
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
    onCategorySelected: (NewsCategory?) -> Unit,
    onManageClick: () -> Unit // 新增：分类管理点击回调
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

        // 分类管理按钮
        item {
            CategoryManageButton(
                onClick = onManageClick
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

    // 添加调试日志
    LaunchedEffect(isSelected) {
        Log.d("CategoryChip", "🏷️ 分类标签状态更新: $categoryName, 选中: $isSelected")
    }

    // 动画状态
    val animatedElevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 2.dp,
        animationSpec = tween(300)
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(200)
    )

    Card(
        modifier = Modifier
            .clickable {
                Log.d("CategoryChip", "👆 分类标签被点击: $categoryName")
                onClick()
                Log.d("CategoryChip", "✅ 分类标签点击回调执行完成: $categoryName")
            }
            .clip(RoundedCornerShape(20.dp))
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation),
        border = if (isSelected) null else BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 选中状态指示器
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            Color.White,
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = categoryName,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

/**
 * 分类管理按钮
 */
@Composable
private fun CategoryManageButton(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF00796B) // 深绿色
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings, // 使用Material Icons的设置图标
                contentDescription = "管理分类",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "管理",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 新闻列表内容
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun NewsListContent(
    newsListState: UiState<com.example.newsclient.ui.NewsListState>,
    onNewsClick: (News) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit
) {
    val listState = rememberLazyListState()

    // 下拉刷新状态
    val isRefreshing = newsListState is UiState.Loading
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
                Log.d("NewsListContent", "   当前状态: ${newsListState::class.simpleName}")
                Log.d("NewsListContent", "   isNearBottom: $isNearBottom (${scrollInfo.lastVisibleIndex} >= ${scrollInfo.totalItems - 2})")
                Log.d("NewsListContent", "   hasEnoughItems: $hasEnoughItems")

                // 修改条件：只要有数据且接近底部就尝试加载更多
                if (isNearBottom && hasEnoughItems) {
                    // 检查是否为Success状态且满足加载更多条件
                    if (newsListState is UiState.Success) {
                        val canLoadMore = !newsListState.data.isLoadingMore && newsListState.data.hasMoreData
                        Log.d("NewsListContent", "   Success状态 - isLoadingMore: ${newsListState.data.isLoadingMore}")
                        Log.d("NewsListContent", "   Success状态 - hasMoreData: ${newsListState.data.hasMoreData}")
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
                if (newsListState !is UiState.Success) {
                    Log.d("NewsListContent", "❌ 状态不是Success: ${newsListState::class.simpleName}")
                    // 额外调试：如果状态不是Success，输出更多信息
                    when (newsListState) {
                        is UiState.Loading -> {
                            Log.d("NewsListContent", "   状态详情: 正在加载中")
                        }
                        is UiState.Error -> {
                            Log.d("NewsListContent", "   状态详情: 错误 - ${newsListState.message}")
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
        when (newsListState) {
            is UiState.Loading -> {
                if (!isRefreshing) {
                    LoadingContent()
                }
            }
            is UiState.Success -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp), // 增加间距避免遮挡
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp) // 优化边距
                ) {
                    items(
                        items = newsListState.data.news,
                        key = { news -> news.id } // 添加key以优化性能
                    ) { news ->
                        NewsItem(
                            news = news,
                            isRead = newsListState.data.readNewsIds.contains(news.id),
                            onClick = { onNewsClick(news) }
                        )
                    }

                    // 加载更多指示器
                    if (newsListState.data.isLoadingMore) {
                        item {
                            LoadingMoreIndicator()
                        }
                    }

                    // 如果没有更多数据，显示底部提示
                    if (!newsListState.data.hasMoreData && newsListState.data.news.isNotEmpty()) {
                        item {
                            NoMoreDataIndicator()
                        }
                    }
                }
            }
            is UiState.Error -> {
                ErrorContent(
                    message = newsListState.message,
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
    isRead: Boolean = false,
    onClick: () -> Unit
) {
    // 动画效果
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isRead) 0.7f else 1f,
        animationSpec = tween(300)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .graphicsLayer { alpha = animatedAlpha },
        colors = CardDefaults.cardColors(
            containerColor = if (isRead)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 新闻标题 - 使用黑体字体
            Text(
                text = news.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif, // 使用无衬线字体（接近黑体效果）
                color = if (isRead)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                else
                    MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 26.sp, // 稍微增加行高避免遮挡
                modifier = Modifier.fillMaxWidth() // 确保文本占满宽度
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 新闻图片优化
            val processedImageUrl = news.imageUrl.let { url ->
                when {
                    url.isBlank() -> ""
                    url == "[]" -> ""
                    url.startsWith("[") && url.endsWith("]") -> {
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
                var finalImageUrl by remember { mutableStateOf(processedImageUrl) }
                var hasTriedFallback by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(finalImageUrl)
                            .crossfade(true)
                            .listener(
                                onError = { _, result ->
                                    android.util.Log.w("NewsItem", "图片加载失败: $finalImageUrl, 错误: ${result.throwable.message}")
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
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 200.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // 新闻摘要优化
            Text(
                text = news.content,
                fontSize = 15.sp,
                fontFamily = FontFamily.Serif, // 使用衬线字体（宋体效果）
                color = if (isRead)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 22.sp, // 增加行高提升可读性
                modifier = Modifier.fillMaxWidth() // 确保文本占满宽度
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 新闻元信息优化
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 发布者信息
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = news.publisher.take(1),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = news.publisher,
                        fontSize = 13.sp,
                        color = if (isRead)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                // 时间和已读状态
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRead) {
                        Surface(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "已读",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = formatPublishTime(news.publishTime),
                        fontSize = 12.sp,
                        color = if (isRead)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
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
            // 使用更现代的加载动画
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "正在加载新闻...",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "为您精选最新资讯",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 14.sp
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
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // 错误状态图标
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "!",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "加载失败",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "重新加载",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
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
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // 添加一个空状态图标
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "暂无新闻",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "下拉刷新获取最新资讯",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 添加一个提示卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "💡 小提示：可以尝试切换不同的新闻分类或调整网络连接",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
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
