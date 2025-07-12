package com.example.newsclient.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import android.util.Log
import kotlinx.coroutines.delay

/**
 * 简化的搜索界面 - 重构版本
 * 分离搜索输入状态和结果显示状态，提高响应速度
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimplifiedSearchScreen(
    onBackClick: () -> Unit,
    onNewsClick: (News) -> Unit,
    currentCategory: NewsCategory? = null,
    viewModel: NewsViewModel = viewModel(factory = NewsViewModel.Factory)
) {
    // 简化状态管理
    var searchText by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var selectedMonth by remember { mutableStateOf<Int?>(null) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isInSearchMode by remember { mutableStateOf(true) } // 核心状态：是否在搜索模式
    var hasSearched by remember { mutableStateOf(false) } // 记录是否已经搜索过

    val searchResultState by viewModel.searchResultState.collectAsState()

    // 搜索函数
    fun performSearch() {
        if (searchText.isBlank() && selectedYear == null) return

        val dateQuery = buildDateQuery(selectedYear, selectedMonth, selectedDay)

        Log.d("SimplifiedSearchScreen", "搜索 - 关键词: '$searchText', 日期: $dateQuery")

        when {
            searchText.isNotBlank() && dateQuery != null -> {
                viewModel.searchNews(searchText.trim(), currentCategory, dateQuery)
            }
            searchText.isNotBlank() -> {
                viewModel.searchNews(searchText.trim(), currentCategory)
            }
            dateQuery != null -> {
                viewModel.searchNewsByDate(dateQuery, currentCategory)
            }
        }

        hasSearched = true
        isInSearchMode = false // 切换到结果显示模式
    }

    // 根据当前状态显示不同的界面
    if (isInSearchMode) {
        SearchInputScreen(
            searchText = searchText,
            onSearchTextChange = { searchText = it },
            selectedYear = selectedYear,
            selectedMonth = selectedMonth,
            selectedDay = selectedDay,
            showDatePicker = showDatePicker,
            onDatePickerToggle = { showDatePicker = it },
            onDateSelected = { year, month, day ->
                selectedYear = year
                selectedMonth = month
                selectedDay = day
                showDatePicker = false
            },
            onClearDate = {
                selectedYear = null
                selectedMonth = null
                selectedDay = null
            },
            onBackClick = onBackClick,
            onSearch = { performSearch() },
            currentCategory = currentCategory
        )
    } else {
        SearchResultScreen(
            searchResultState = searchResultState,
            onNewsClick = onNewsClick,
            onBackToSearch = {
                isInSearchMode = true
                Log.d("SimplifiedSearchScreen", "点击搜索栏，切换到搜索模式")
            },
            searchText = searchText,
            selectedYear = selectedYear,
            selectedMonth = selectedMonth,
            selectedDay = selectedDay,
            hasSearched = hasSearched
        )
    }
}

/**
 * 搜索输入界面
 */
@Composable
private fun SearchInputScreen(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    selectedYear: Int?,
    selectedMonth: Int?,
    selectedDay: Int?,
    showDatePicker: Boolean,
    onDatePickerToggle: (Boolean) -> Unit,
    onDateSelected: (Int?, Int?, Int?) -> Unit,
    onClearDate: () -> Unit,
    onBackClick: () -> Unit,
    onSearch: () -> Unit,
    currentCategory: NewsCategory?
) {
    val focusRequester = remember { FocusRequester() }

    // 自动聚焦
    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 搜索栏
        SearchBar(
            searchText = searchText,
            onSearchTextChange = onSearchTextChange,
            onBackClick = onBackClick,
            onSearch = onSearch,
            onDatePickerClick = { onDatePickerToggle(true) },
            onClearText = { onSearchTextChange("") },
            focusRequester = focusRequester,
            hasDateFilter = selectedYear != null
        )

        // 日期筛选显示
        if (selectedYear != null) {
            DateFilterCard(
                selectedYear = selectedYear,
                selectedMonth = selectedMonth,
                selectedDay = selectedDay,
                onClearDate = onClearDate
            )
        }

        // 分类信息
        currentCategory?.let { category ->
            CategoryCard(category = category)
        }

        // 欢迎界面
        WelcomeContent()
    }

    // 日期选择器
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = onDateSelected,
            onDismiss = { onDatePickerToggle(false) }
        )
    }
}

/**
 * 搜索结果界面
 */
@Composable
private fun SearchResultScreen(
    searchResultState: UiState<List<News>>,
    onNewsClick: (News) -> Unit,
    onBackToSearch: () -> Unit,
    searchText: String,
    selectedYear: Int?,
    selectedMonth: Int?,
    selectedDay: Int?,
    hasSearched: Boolean
) {
    // 添加重组监控
    val recompositionCount = remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        recompositionCount.value++
        Log.d("SearchResultScreen", "重组次数: ${recompositionCount.value}")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 结果页面的搜索栏 - 点击快速回到搜索模式
        ResultSearchBar(
            searchText = searchText,
            selectedYear = selectedYear,
            selectedMonth = selectedMonth,
            selectedDay = selectedDay,
            onBackToSearch = onBackToSearch
        )

        // 搜索结果内容
        when (searchResultState) {
            is UiState.Loading -> LoadingContent()
            is UiState.Success -> {
                if (searchResultState.data.isEmpty()) {
                    EmptyResultContent()
                } else {
                    SearchResultList(
                        searchResults = searchResultState.data,
                        onNewsClick = onNewsClick
                    )
                }
            }
            is UiState.Error -> ErrorContent(searchResultState.message)
            else -> EmptyResultContent()
        }
    }
}

/**
 * 搜索栏组件
 */
@Composable
private fun SearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSearch: () -> Unit,
    onDatePickerClick: () -> Unit,
    onClearText: () -> Unit,
    focusRequester: FocusRequester,
    hasDateFilter: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回"
                )
            }

            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder = { Text("搜索新闻...") },
                trailingIcon = {
                    Row {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = onClearText) {
                                Icon(Icons.Default.Clear, contentDescription = "清除")
                            }
                        }

                        IconButton(onClick = onDatePickerClick) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "选择日期",
                                tint = if (hasDateFilter) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }

                        IconButton(
                            onClick = onSearch,
                            enabled = searchText.isNotBlank() || hasDateFilter
                        ) {
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
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                singleLine = true
            )
        }
    }
}

/**
 * 结果页面的搜索栏 - 点击快速回到搜索模式
 * 使用最简单可靠的点击处理方式
 */
@Composable
private fun ResultSearchBar(
    searchText: String,
    selectedYear: Int?,
    selectedMonth: Int?,
    selectedDay: Int?,
    onBackToSearch: () -> Unit
) {
    // 添加调试状态
    var isPressed by remember { mutableStateOf(false) }

    // 使用最简单的 Box + clickable 组合，避免复杂的组件嵌套
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable {
                Log.d("ResultSearchBar", "===== 点击事件触发 =====")
                isPressed = true
                onBackToSearch()
            }
            .padding(16.dp), // 内边距放在 clickable 之后
        contentAlignment = Alignment.CenterStart
    ) {
        // 背景
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = if (isPressed) 6.dp else 4.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (searchText.isNotEmpty()) searchText else "点击重新搜索",
                        fontSize = 16.sp,
                        color = if (searchText.isNotEmpty()) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = if (searchText.isEmpty()) FontWeight.Medium else FontWeight.Normal
                    )

                    if (selectedYear != null) {
                        Text(
                            text = formatDateDisplay(selectedYear, selectedMonth, selectedDay),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 点击指示器
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "点击重新搜索",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    // 重置按压状态
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

/**
 * 日期筛选卡片
 */
@Composable
private fun DateFilterCard(
    selectedYear: Int?,
    selectedMonth: Int?,
    selectedDay: Int?,
    onClearDate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatDateDisplay(selectedYear, selectedMonth, selectedDay),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            IconButton(onClick = onClearDate) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "清除日期",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * 分类卡片
 */
@Composable
private fun CategoryCard(category: NewsCategory) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Text(
            text = "在「${category.value}」分类中搜索",
            modifier = Modifier.padding(12.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
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
            Text(text = "🔍", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "输入关键词或选择日期进行搜索",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 搜索结果列表
 */
@Composable
private fun SearchResultList(
    searchResults: List<News>,
    onNewsClick: (News) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "找到 ${searchResults.size} 条相关新闻",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(searchResults) { news ->
            NewsCard(news = news, onClick = { onNewsClick(news) })
        }
    }
}

/**
 * 加载内容
 */
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "正在搜索...", fontSize = 16.sp)
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "😔", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "没有找到相关新闻",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "❌", fontSize = 48.sp)
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
 * 日期选择器对话框
 */
@Composable
private fun DatePickerDialog(
    onDateSelected: (Int?, Int?, Int?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            DatePicker(
                onDateSelected = onDateSelected,
                onDismiss = onDismiss
            )
        }
    }
}

/**
 * 新闻卡片组件
 */
@Composable
private fun NewsCard(
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
            // 标题
            Text(
                text = news.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 图片
            val imageUrl = processImageUrl(news.imageUrl)
            if (imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "新闻图片",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 内容
            Text(
                text = news.content,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 发布信息
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

// 辅助函数
private fun buildDateQuery(year: Int?, month: Int?, day: Int?): String? {
    return if (year != null) {
        when {
            day != null -> {
                // 精确到天：2025-01-15
                String.format("%04d-%02d-%02d", year, month ?: 1, day)
            }
            month != null -> {
                // 精确到月：2025-01-01,2025-01-31
                val startDate = String.format("%04d-%02d-01", year, month)
                val endDate = String.format("%04d-%02d-%02d", year, month, getDaysInMonth(year, month))
                "$startDate,$endDate"
            }
            else -> {
                // 精确到年：2025-01-01,2025-12-31
                "$year-01-01,$year-12-31"
            }
        }
    } else null
}

private fun formatDateDisplay(year: Int?, month: Int?, day: Int?): String {
    return when {
        day != null -> "${year}年${month}月${day}日"
        month != null -> "${year}年${month}月"
        year != null -> "${year}年"
        else -> "日期筛选"
    }
}

private fun processImageUrl(imageUrl: String): String {
    return when {
        imageUrl.isBlank() -> ""
        imageUrl == "[]" -> ""
        imageUrl.startsWith("[") && imageUrl.endsWith("]") -> {
            imageUrl.substring(1, imageUrl.length - 1)
                .split(",")
                .map { it.trim().removePrefix("\"").removeSuffix("\"") }
                .firstOrNull { it.startsWith("http") } ?: ""
        }
        imageUrl.startsWith("http") -> imageUrl
        else -> ""
    }
}

private fun formatTime(publishTime: String): String {
    return try {
        // 简单的时间格式化，可以根据需要调整
        if (publishTime.length >= 10) {
            publishTime.substring(0, 10)
        } else {
            publishTime
        }
    } catch (e: Exception) {
        publishTime
    }
}

private fun getDaysInMonth(year: Int, month: Int): Int {
    return when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 31
    }
}

private fun isLeapYear(year: Int): Boolean {
    return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
}
