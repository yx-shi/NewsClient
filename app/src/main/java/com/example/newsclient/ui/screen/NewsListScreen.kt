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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.delay

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
            .background(MaterialTheme.colorScheme.background) // 使用主题背景色
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
                style = MaterialTheme.typography.bodyMedium, // 使用定义的宋体正文样式
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )

            // 添加搜索快捷提示
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "搜索",
                    style = MaterialTheme.typography.labelMedium, // 使用定义的标签样式
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
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
                            MaterialTheme.colorScheme.onPrimary, // 使用主题色替代硬编码白色
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
            containerColor = MaterialTheme.colorScheme.secondaryContainer // 使用主题的secondary容器色
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "管理分类",
                tint = MaterialTheme.colorScheme.onSecondaryContainer, // 使用主题的对应文字色
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "管理",
                color = MaterialTheme.colorScheme.onSecondaryContainer, // 使用主题的对应文字色
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

    // 上拉加载更多逻辑 - 监听滚动到底部
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            ScrollInfo(
                totalItems = totalItems,
                lastVisibleItemIndex = lastVisibleItemIndex,
                isAtBottom = lastVisibleItemIndex >= totalItems - 1
            )
        }.collect { scrollInfo ->
            Log.d("ScrollDebug", "滚动状态: 总数=${scrollInfo.totalItems}, 最后可见=${scrollInfo.lastVisibleItemIndex}, 是否到底=${scrollInfo.isAtBottom}")

            // 当滚动到底部时等待一小段时间，然后检查是否需要自动加载更多
            if (scrollInfo.isAtBottom && scrollInfo.totalItems > 0) {
                delay(200) // 等待200ms，避免过于频繁的触发

                if (newsListState is UiState.Success) {
                    val canLoadMore = !newsListState.data.isLoadingMore && newsListState.data.hasMoreData

                    Log.d("LoadMoreDebug", "=== 自动加载更多检查 ===")
                    Log.d("LoadMoreDebug", "到达底部: ${scrollInfo.isAtBottom}")
                    Log.d("LoadMoreDebug", "总数: ${scrollInfo.totalItems}")
                    Log.d("LoadMoreDebug", "isLoadingMore: ${newsListState.data.isLoadingMore}")
                    Log.d("LoadMoreDebug", "hasMoreData: ${newsListState.data.hasMoreData}")
                    Log.d("LoadMoreDebug", "canLoadMore: $canLoadMore")

                    if (canLoadMore) {
                        Log.d("LoadMoreDebug", "🚀 自动触发加载更多")
                        // 立即触发加载更多，这会更新状态为 isLoadingMore = true
                        // 从而显示加载特效，然后再实际加载数据
                        onLoadMore()
                    } else {
                        Log.d("LoadMoreDebug", "❌ 无法加载更多 - 可能正在加载或没有更多数据")
                    }
                }
            }
        }
    }

    // 计算当前状态以显示底部指示器
    val bottomIndicatorState by remember(newsListState) {
        derivedStateOf {
            Log.d("BottomIndicatorDebug", "=== 底部指示器状态计算开始 ===")
            Log.d("BottomIndicatorDebug", "newsListState类型: ${newsListState::class.simpleName}")

            if (newsListState is UiState.Success) {
                Log.d("BottomIndicatorDebug", "newsListState是Success类型")
                Log.d("BottomIndicatorDebug", "isLoadingMore: ${newsListState.data.isLoadingMore}")
                Log.d("BottomIndicatorDebug", "hasMoreData: ${newsListState.data.hasMoreData}")
                Log.d("BottomIndicatorDebug", "新闻数量: ${newsListState.data.news.size}")

                val state = when {
                    newsListState.data.isLoadingMore -> {
                        Log.d("BottomIndicatorDebug", "✅ 条件匹配: isLoadingMore = true")
                        BottomIndicatorState.Loading
                    }
                    !newsListState.data.hasMoreData && newsListState.data.news.isNotEmpty() -> {
                        Log.d("BottomIndicatorDebug", "✅ 条件匹配: 没有更多数据且新闻不为空")
                        BottomIndicatorState.NoMore
                    }
                    else -> {
                        Log.d("BottomIndicatorDebug", "✅ 条件匹配: 默认隐藏状态")
                        BottomIndicatorState.Hidden
                    }
                }

                Log.d("BottomIndicatorDebug", "计算得到的状态: $state")
                state
            } else {
                Log.d("BottomIndicatorDebug", "newsListState不是Success类型，返回Hidden")
                BottomIndicatorState.Hidden
            }
        }
    }

    // 添加额外的状态监听来确保UI能够响应状态变化
    LaunchedEffect(newsListState) {
        if (newsListState is UiState.Success) {
            Log.d("BottomIndicatorDebug", "👁️ LaunchedEffect 监听到状态变化")
            Log.d("BottomIndicatorDebug", "   isLoadingMore: ${newsListState.data.isLoadingMore}")
            Log.d("BottomIndicatorDebug", "   hasMoreData: ${newsListState.data.hasMoreData}")
            Log.d("BottomIndicatorDebug", "   新闻数量: ${newsListState.data.news.size}")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        when (newsListState) {
            is UiState.Loading -> {
                Log.d("LoadMoreDebug", "显示加载中状态")
                LoadingContent()
            }
            is UiState.Success -> {
                Log.d("LoadMoreDebug", "显示成功状态，新闻数量: ${newsListState.data.news.size}")
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    items(
                        items = newsListState.data.news,
                        key = { news -> news.id }
                    ) { news ->
                        NewsItem(
                            news = news,
                            isRead = newsListState.data.readNewsIds.contains(news.id),
                            onClick = { onNewsClick(news) }
                        )
                    }

                    // 底部状态指示器
                    item {
                        BottomIndicator(state = bottomIndicatorState)
                    }
                }
            }
            is UiState.Error -> {
                Log.d("LoadMoreDebug", "显示错误状态: ${newsListState.message}")
                ErrorContent(
                    message = newsListState.message,
                    onRetry = onRefresh
                )
            }
            is UiState.Empty -> {
                Log.d("LoadMoreDebug", "显示空状态")
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
 * 底部指示器状态
 */
private enum class BottomIndicatorState {
    Hidden,    // 隐藏
    Loading,   // 正在加载
    NoMore     // 没有更多数据
}

/**
 * 滚动信息数据类
 */
private data class ScrollInfo(
    val totalItems: Int,
    val lastVisibleItemIndex: Int,
    val isAtBottom: Boolean
)

/**
 * 底部指示器组件
 */
@Composable
private fun BottomIndicator(state: BottomIndicatorState) {
    Log.d("BottomIndicatorRender", "=== BottomIndicator 渲染 ===")
    Log.d("BottomIndicatorRender", "接收到的状态: $state")

    when (state) {
        BottomIndicatorState.Loading -> {
            Log.d("BottomIndicatorRender", "✅ 渲染 Loading 状态")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "正在加载更多新闻...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
        BottomIndicatorState.NoMore -> {
            Log.d("BottomIndicatorRender", "✅ 渲染 NoMore 状态")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Divider(
                        modifier = Modifier.width(60.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "已经到底了",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
        BottomIndicatorState.Hidden -> {
            Log.d("BottomIndicatorRender", "✅ 渲染 Hidden 状态")
            // 给底部留一些空间
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 单个新闻条目
 */
@Composable
private fun NewsItem(
    news: News,
    isRead: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // 统一使用普通背景色，不区分已读状态
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp) // 增加内边距避免标题被遮挡
        ) {
            // 新闻标题 - 已读时变为灰色，未读时保持正常颜色
            Text(
                text = news.title,
                style = MaterialTheme.typography.titleMedium, // 使用定义的黑体标题样式
                color = if (isRead)
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) // 已读：灰色
                else
                    MaterialTheme.colorScheme.onSurface, // 未读：正常颜色
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp) // 增加底部间距
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
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
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

            // 新闻摘要 - 使用雅黑字体和主题色彩
            Text(
                text = news.content,
                style = MaterialTheme.typography.bodyMedium, // 使用定义的雅黑正文样式
                color = if (isRead)
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) // 已读：稍微变淡
                else
                    MaterialTheme.colorScheme.onSurfaceVariant, // 未读：正常颜色
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 22.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
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
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        color = if (isRead)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) // 已读：淡化图标
                        else
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f), // 未读：正常
                        shape = CircleShape
                    ) {
                        Text(
                            text = news.publisher.take(1),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = news.publisher,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isRead)
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) // 已读：变淡
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant, // 未读：正常
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                // 时间和已读状态
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 只在已读时显示已读标签，且样式更简洁
                    if (isRead) {
                        Text(
                            text = "已读",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                    Text(
                        text = news.publishTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isRead)
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) // 已读：变淡
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) // 未读：正常
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
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary // 使用主题色
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "加载更多...",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant // 使用主题色
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
            color = MaterialTheme.colorScheme.onSurfaceVariant, // 使用主题色
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
 * 底部加载更多组件
 */
@Composable
private fun LoadMoreFooter(
    showLoadMoreButton: Boolean,
    isLoadingMore: Boolean,
    hasMoreData: Boolean,
    hasData: Boolean,
    onLoadMore: () -> Unit
) {
    // 添加调试信息
    Log.d("LoadMoreFooter", "=== LoadMoreFooter 渲染 ===")
    Log.d("LoadMoreFooter", "showLoadMoreButton: $showLoadMoreButton")
    Log.d("LoadMoreFooter", "isLoadingMore: $isLoadingMore")
    Log.d("LoadMoreFooter", "hasMoreData: $hasMoreData")
    Log.d("LoadMoreFooter", "hasData: $hasData")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            // 正在加载更多
            isLoadingMore -> {
                Log.d("LoadMoreFooter", "显示加载中状态")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "正在加载更多新闻...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 显示加载更多按钮
            showLoadMoreButton -> {
                Log.d("LoadMoreFooter", "显示加载更多按钮")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            Log.d("LoadMoreFooter", "🔥 加载更多按钮被点击")
                            onLoadMore()
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "加载更多",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "上拉加载更多",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 添加一个小提示
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "点击或向上滑动加载更多内容",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            // 没有更多数据
            !hasMoreData && hasData -> {
                Log.d("LoadMoreFooter", "显示没有更多数据")
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Divider(
                        modifier = Modifier.width(60.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "已经到底了",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "感谢您的阅读 📰",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            // 默认状态：给底部留一些空间
            else -> {
                Log.d("LoadMoreFooter", "显示默认状态")
                Spacer(modifier = Modifier.height(32.dp))
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
