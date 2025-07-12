package com.example.newsclient.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.newsclient.data.model.News
import com.example.newsclient.data.model.NewsCategory
import com.example.newsclient.ui.NewsViewModel
import com.example.newsclient.ui.UiState
import com.example.newsclient.ui.components.DatePicker
import com.example.newsclient.ui.components.formatSelectedDate
import com.example.newsclient.ui.components.formatDateRange
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 优化的搜索界面
 * 1. 简化状态管理：点击搜索栏直接回到输入状态
 * 2. 统一新闻显示：使用与主页面相同的新闻卡片组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimplifiedSearchScreen(
    onBackClick: () -> Unit,
    onNewsClick: (News) -> Unit,
    currentCategory: NewsCategory? = null,
    viewModel: NewsViewModel = viewModel(factory = NewsViewModel.Factory)
) {
    var searchText by remember { mutableStateOf("") }
    var hasExecutedSearch by remember { mutableStateOf(false) }
    var searchMode by remember { mutableStateOf(SearchMode.KEYWORD) }

    // 日期选择相关状态
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var selectedMonth by remember { mutableStateOf<Int?>(null) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }
    val selectedDateString = remember(selectedYear, selectedMonth, selectedDay) {
        formatSelectedDate(selectedYear, selectedMonth, selectedDay)
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    // 搜索结果状态
    val searchResultState by viewModel.searchResultState.collectAsState()

    // 简化的状态：只关心是否有搜索文本或日期筛选
    val isInSearchMode = searchText.isNotEmpty() || selectedDateString != null

    // 检测搜索内容是否为时间格式
    LaunchedEffect(searchText) {
        if (searchText.isNotEmpty()) {
            val newMode = detectSearchMode(searchText)
            if (newMode != searchMode) {
                searchMode = newMode
            }
        }
    }

    // 初始聚焦
    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 增强的搜索栏，支持时间搜索
        AdvancedSearchBar(
            searchText = searchText,
            searchMode = searchMode,
            hasDateFilter = selectedDateString != null,
            onSearchTextChange = { newText ->
                searchText = newText
                // 文本为空且没有日期筛选时，清空搜索结果和执行状态
                if (newText.isEmpty() && selectedDateString == null) {
                    viewModel.clearSearchResults()
                    hasExecutedSearch = false
                    searchMode = SearchMode.KEYWORD
                }
            },
            onBackClick = {
                keyboardController?.hide()
                onBackClick()
            },
            onSearchSubmit = {
                if (searchText.isNotBlank() || selectedDateString != null) {
                    Log.d("SimplifiedSearchScreen", "搜索: 关键词='$searchText', 日期='${selectedDateString ?: "不限"}', 模式: $searchMode")
                    hasExecutedSearch = true

                    // 根据是否有日期筛选决定搜索方式
                    if (selectedDateString != null) {
                        // 将模糊日期转换为日期范围
                        val dateRange = formatDateRange(selectedYear, selectedMonth, selectedDay)

                        if (dateRange != null) {
                            val (startDate, endDate) = dateRange
                            Log.d("SimplifiedSearchScreen", "日期范围: $startDate 到 $endDate")

                            // 构建日期范围字符串，让服务器知道这是一个日期范围
                            val dateRangeQuery = if (startDate == endDate) {
                                // 如果开始和结束日期相同，就是精确日期
                                startDate
                            } else {
                                // 如果不同，构建范围查询字符串
                                "$startDate,$endDate"
                            }

                            if (searchText.isBlank()) {
                                // 纯日期搜索
                                viewModel.searchNewsByDate(dateRangeQuery, currentCategory)
                            } else {
                                // 关键词+日期组合搜索
                                viewModel.searchNews(searchText.trim(), currentCategory, dateRangeQuery)
                            }
                        } else {
                            // 如果日期范围为空，执行普通关键词搜索
                            if (searchText.isNotBlank()) {
                                viewModel.searchNews(searchText.trim(), currentCategory)
                            }
                        }
                    } else if (searchMode == SearchMode.DATE) {
                        // 从文本中识别出的日期搜索
                        val parsedQuery = parseSearchQuery(searchText.trim())
                        viewModel.searchNewsByDate(parsedQuery.dateQuery ?: "", currentCategory)
                    } else if (searchMode == SearchMode.COMBINED) {
                        // 从文本中识别出的关键词+日期组合搜索
                        val parsedQuery = parseSearchQuery(searchText.trim())
                        viewModel.searchNews(
                            parsedQuery.keyword ?: "",
                            currentCategory,
                            parsedQuery.dateQuery
                        )
                    } else {
                        // 普通关键词搜索
                        viewModel.searchNews(searchText.trim(), currentCategory)
                    }

                    keyboardController?.hide()
                }
            },
            onClearSearch = {
                searchText = ""
                selectedYear = null
                selectedMonth = null
                selectedDay = null
                hasExecutedSearch = false
                searchMode = SearchMode.KEYWORD
                viewModel.clearSearchResults()
                focusRequester.requestFocus()
            },
            onSearchBarClick = {
                // 简化点击处理，只负责获取焦点，不再清空搜索文本
                focusRequester.requestFocus()
            },
            onDatePickerClick = {
                showDatePicker = true
                keyboardController?.hide()
            },
            focusRequester = focusRequester,
            isLoading = searchResultState is UiState.Loading,
            selectedYear = selectedYear,
            selectedMonth = selectedMonth,
            selectedDay = selectedDay
        )

        // 分类信息
        if (currentCategory != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "在「${currentCategory.value}」分类中搜索",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // 日期筛选信息
        if (selectedDateString != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                selectedYear != null && selectedMonth != null && selectedDay != null ->
                                    "按日期: ${selectedYear}年${selectedMonth}月${selectedDay}日"
                                selectedYear != null && selectedMonth != null ->
                                    "按月份: ${selectedYear}年${selectedMonth}月"
                                selectedYear != null ->
                                    "按年份: ${selectedYear}年"
                                else -> "日期筛选"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    IconButton(
                        onClick = {
                            selectedYear = null
                            selectedMonth = null
                            selectedDay = null

                            // 如果搜索框也为空，清空搜索结果
                            if (searchText.isEmpty()) {
                                viewModel.clearSearchResults()
                                hasExecutedSearch = false
                            } else {
                                // 否则执行纯关键词搜索
                                coroutineScope.launch {
                                    delay(100)
                                    viewModel.searchNews(searchText.trim(), currentCategory)
                                }
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清除日期筛选",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 内容区域
        when {
            !isInSearchMode -> {
                // 输入提示状态
                WelcomeContent()
            }
            !hasExecutedSearch -> {
                // 有搜索文本但还没执行搜索时，显示输入提示
                WelcomeContent()
            }
            else -> {
                // 已执行搜索时显示结果
                when (val state = searchResultState) {
                    is UiState.Loading -> {
                        LoadingContent()
                    }
                    is UiState.Success -> {
                        if (state.data.isEmpty()) {
                            EmptyResultContent()
                        } else {
                            UnifiedSearchResultList(
                                searchResults = state.data,
                                onNewsClick = onNewsClick
                            )
                        }
                    }
                    is UiState.Error -> {
                        ErrorContent(message = state.message)
                    }
                    is UiState.Empty -> {
                        EmptyResultContent()
                    }
                }
            }
        }
    }

    // 日期选择器对话框
    if (showDatePicker) {
        Dialog(onDismissRequest = { showDatePicker = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                DatePicker(
                    onDateSelected = { year, month, day ->
                        selectedYear = year
                        selectedMonth = month
                        selectedDay = day

                        // 选择日期后不自动执行搜索，让用户手动触发
                        // 移除自动搜索逻辑，用户需要手动点击搜索按钮
                    },
                    onDismiss = { showDatePicker = false }
                )
            }
        }
    }
}

/**
 * 优化的搜索栏组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdvancedSearchBar(
    searchText: String,
    searchMode: SearchMode,
    hasDateFilter: Boolean,
    onSearchTextChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSearchSubmit: () -> Unit,
    onClearSearch: () -> Unit,
    onSearchBarClick: () -> Unit,
    onDatePickerClick: () -> Unit,
    focusRequester: FocusRequester,
    isLoading: Boolean,
    selectedYear: Int? = null,
    selectedMonth: Int? = null,
    selectedDay: Int? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 返回按钮
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 搜索输入框
                OutlinedTextField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(
                            text = "搜索新闻...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        Row {
                            // 清除按钮
                            if (searchText.isNotEmpty()) {
                                IconButton(onClick = onClearSearch) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "清除",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // 日期选择按钮
                            IconButton(
                                onClick = onDatePickerClick,
                                enabled = !isLoading
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "选择日期",
                                    tint = if (hasDateFilter || searchMode == SearchMode.DATE) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }

                            // 搜索按钮
                            IconButton(
                                onClick = onSearchSubmit,
                                enabled = !isLoading && (searchText.isNotBlank() || hasDateFilter)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "搜索",
                                        tint = if (searchText.isNotBlank() || hasDateFilter) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { onSearchSubmit() }
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }

        // 显示选中的日期标签
        AnimatedVisibility(
            visible = hasDateFilter,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = formatDateForDisplay(selectedYear, selectedMonth, selectedDay),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "点击图标更改",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * 统一的搜索结果列表 - 使用与主页面相同的样式
 */
@Composable
private fun UnifiedSearchResultList(
    searchResults: List<News>,
    onNewsClick: (News) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 结果统计
        item {
            Text(
                text = "找到 ${searchResults.size} 条相关新闻",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // 搜索结果 - 使用统一的新闻项组件
        items(
            items = searchResults,
            key = { news -> news.id }
        ) { news ->
            UnifiedNewsItem(
                news = news,
                onClick = { onNewsClick(news) }
            )
        }
    }
}

/**
 * 统一的新闻项组件 - 与主页面保持一致的样式和图片处理
 */
@Composable
private fun UnifiedNewsItem(
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

            // 新闻图片（与主页面完全相同的处理逻辑）
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
                                Log.w("UnifiedNewsItem", "图片加载失败: $finalImageUrl, 错误: ${result.throwable.message}")

                                // 如果是HTTPS失败且还没尝试过HTTP回退，则尝试HTTP
                                if (!hasTriedFallback && finalImageUrl.startsWith("https://")) {
                                    val httpUrl = finalImageUrl.replaceFirst("https://", "http://")
                                    Log.i("UnifiedNewsItem", "尝试HTTP回退: $httpUrl")
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
                    text = formatTime(news.publishTime),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * 加载状态内容
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
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "正在搜索...",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 空结果内容
 */
@Composable
private fun EmptyResultContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "😔",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "很抱歉，没有找到相关新闻",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "请尝试使用其他关键词搜索",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 错误内容
 */
@Composable
private fun ErrorContent(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "❌",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "搜索失败",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 欢迎内容
 */
@Composable
private fun WelcomeContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "🔍",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "智能搜索支持多种模式",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 搜索提示卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "💡 搜索提示",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• 关键词搜索：科技、体育、政治等",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• 时间搜索：2024-01-15 或 15/01/2024",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• 组合搜索：科技 2024-01-15",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * 格式化时间
 */
private fun formatTime(publishTime: String): String {
    return try {
        val parts = publishTime.split(" ")
        if (parts.size >= 2) {
            val datePart = parts[0]
            val timePart = parts[1].substring(0, 5)
            "$datePart $timePart"
        } else {
            publishTime
        }
    } catch (e: Exception) {
        publishTime
    }
}

/**
 * 搜索模式
 */
enum class SearchMode {
    KEYWORD,    // 纯关键词搜索
    DATE,       // 纯日期搜索
    COMBINED    // 关键词+日期组合搜索
}

/**
 * 搜索查询解析结果
 */
data class ParsedSearchQuery(
    val keyword: String? = null,
    val dateQuery: String? = null,
    val mode: SearchMode
)

/**
 * 检测并解析搜索查询
 */
private fun parseSearchQuery(query: String): ParsedSearchQuery {
    val trimmedQuery = query.trim()

    // 日期正则表达式
    val dateRegex1 = Regex("""\d{4}[-/]\d{1,2}[-/]\d{1,2}""") // YYYY-MM-DD
    val dateRegex2 = Regex("""\d{1,2}/\d{1,2}/\d{4}""")       // DD/MM/YYYY

    // 查找所有日期匹配
    val dateMatches = (dateRegex1.findAll(trimmedQuery) + dateRegex2.findAll(trimmedQuery)).toList()

    return when {
        dateMatches.isEmpty() -> {
            // 没有日期，纯关键词搜索
            ParsedSearchQuery(
                keyword = trimmedQuery,
                mode = SearchMode.KEYWORD
            )
        }
        dateMatches.size == 1 -> {
            val dateMatch = dateMatches.first()
            val dateString = dateMatch.value
            val remainingText = trimmedQuery.replace(dateString, "").trim()

            if (remainingText.isBlank()) {
                // 只有日期，纯日期搜索
                ParsedSearchQuery(
                    dateQuery = dateString,
                    mode = SearchMode.DATE
                )
            } else {
                // 有关键词和日期，组合搜索
                ParsedSearchQuery(
                    keyword = remainingText,
                    dateQuery = dateString,
                    mode = SearchMode.COMBINED
                )
            }
        }
        else -> {
            // 多个日期匹配，使用第一个日期，剩余作为关键词
            val firstDate = dateMatches.first().value
            val remainingText = trimmedQuery.replace(firstDate, "").trim()

            ParsedSearchQuery(
                keyword = if (remainingText.isNotBlank()) remainingText else null,
                dateQuery = firstDate,
                mode = if (remainingText.isNotBlank()) SearchMode.COMBINED else SearchMode.DATE
            )
        }
    }
}

/**
 * 检测搜索模式（保持向后兼容）
 */
private fun detectSearchMode(query: String): SearchMode {
    return parseSearchQuery(query).mode
}

/**
 * 格式化日期用于显示
 */
private fun formatDateForDisplay(year: Int?, month: Int?, day: Int?): String {
    return buildString {
        year?.let { append("${it}年") }
        month?.let { append("${it}月") }
        day?.let { append("${it}日") }
    }.ifEmpty { "未选择日期" }
}
