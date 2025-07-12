package com.example.newsclient.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newsclient.data.model.News
import com.example.newsclient.data.model.NewsCategory
import com.example.newsclient.ui.NewsViewModel
import com.example.newsclient.ui.UiState
import android.util.Log
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.window.Dialog
import com.example.newsclient.ui.components.DatePicker
import com.example.newsclient.ui.components.formatSelectedDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 增强版搜索界面
 * 完全重构解决响应迟钝问题
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedSearchScreen(
    onBackClick: () -> Unit,
    onNewsClick: (News) -> Unit,
    currentCategory: NewsCategory? = null,
    viewModel: NewsViewModel = viewModel(factory = NewsViewModel.Factory)
) {
    // 简化的状态管理
    var searchText by remember { mutableStateOf("") }
    var isInputFocused by remember { mutableStateOf(false) }

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

    // 计算派生状态，避免手动状态管理
    val hasSearchResults = remember(searchResultState) {
        searchResultState is UiState.Success && (searchResultState as? UiState.Success)?.data?.isNotEmpty() == true
    }
    val isLoading = searchResultState is UiState.Loading

    // 初始化聚焦 - 使用rememberCoroutineScope避免协程泄露
    LaunchedEffect(Unit) {
        delay(150)
        try {
            focusRequester.requestFocus()
            Log.d("EnhancedSearchScreen", "✅ 初始聚焦成功")
        } catch (e: Exception) {
            Log.e("EnhancedSearchScreen", "❌ 初始聚焦失败", e)
        }
    }

    // 统一的交互处理函数
    val handleBackClick: () -> Unit = remember {
        {
            Log.d("EnhancedSearchScreen", "🔙 返回按钮点击")
            keyboardController?.hide()
            onBackClick()
        }
    }

    // 搜索提交处理函数（包含日期选择）
    val handleSearchSubmit: () -> Unit = remember(searchText, selectedDateString) {
        {
            if (searchText.isNotBlank() || selectedDateString != null) {
                Log.d("EnhancedSearchScreen", "🔍 执行搜索: 关键词='$searchText', 日期='${selectedDateString ?: "不限"}'")

                // 执行搜索，同时传入关键词和日期
                viewModel.searchNews(
                    keyword = searchText.trim(),
                    category = currentCategory,
                    dateQuery = selectedDateString
                )

                keyboardController?.hide()
            }
        }
    }

    val handleClearSearch: () -> Unit = remember {
        {
            Log.d("EnhancedSearchScreen", "🧹 清空搜索")
            searchText = ""

            // 同时清空日期选择
            selectedYear = null
            selectedMonth = null
            selectedDay = null

            viewModel.clearSearchResults()
            // 使用rememberCoroutineScope管理协程
            coroutineScope.launch {
                delay(50)
                try {
                    focusRequester.requestFocus()
                } catch (e: Exception) {
                    Log.e("EnhancedSearchScreen", "❌ 清空后聚焦失败", e)
                }
            }
        }
    }

    val handleSearchBarClick: () -> Unit = remember {
        {
            Log.d("EnhancedSearchScreen", "📱 搜索栏点击")
            try {
                if (hasSearchResults && searchText.isNotEmpty()) {
                    // 有搜索结果且有文本时，清空并聚焦
                    searchText = ""
                    viewModel.clearSearchResults()
                }
                focusRequester.requestFocus()
            } catch (e: Exception) {
                Log.e("EnhancedSearchScreen", "❌ 搜索栏点击失败", e)
            }
        }
    }

    // 日期选择按钮点击处理
    val handleDatePickerClick: () -> Unit = remember {
        {
            showDatePicker = true
            keyboardController?.hide()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 重构的搜索栏
        AdvancedSearchBar(
            searchText = searchText,
            onSearchTextChange = { newText ->
                searchText = newText
                if (newText.isEmpty() && selectedDateString == null) {
                    viewModel.clearSearchResults()
                }
            },
            onBackClick = handleBackClick,
            onSearchSubmit = handleSearchSubmit,
            onClearSearch = handleClearSearch,
            onSearchBarClick = handleSearchBarClick,
            onDatePickerClick = handleDatePickerClick,
            focusRequester = focusRequester,
            onFocusChanged = { focused ->
                isInputFocused = focused
                Log.d("EnhancedSearchScreen", "🎯 焦点状态: $focused")
            },
            isInputFocused = isInputFocused,
            hasSearchResults = hasSearchResults,
            isLoading = isLoading,
            hasDateFilter = selectedDateString != null,
            selectedDateString = selectedDateString
        )

        // 分类信息
        if (currentCategory != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "在「${currentCategory.value}」分类中搜索",
                    modifier = Modifier.padding(12.dp),
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
                    .padding(horizontal = 12.dp, vertical = 4.dp),
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

        // 搜索结果
        SearchResultContent(
            searchResultState = searchResultState,
            onNewsClick = onNewsClick
        )
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

                        // 选择日期后立即执行搜索
                        if (searchText.isNotBlank() || year != null) {
                            // 延迟一点执行搜索，让对话框先关闭
                            coroutineScope.launch {
                                delay(100)
                                handleSearchSubmit()
                            }
                        }
                    },
                    onDismiss = { showDatePicker = false }
                )
            }
        }
    }
}

/**
 * 高级搜索栏 - 包含日期选择功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdvancedSearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSearchSubmit: () -> Unit,
    onClearSearch: () -> Unit,
    onSearchBarClick: () -> Unit,
    onDatePickerClick: () -> Unit,
    focusRequester: FocusRequester,
    onFocusChanged: (Boolean) -> Unit,
    isInputFocused: Boolean,
    hasSearchResults: Boolean,
    isLoading: Boolean,
    hasDateFilter: Boolean,
    selectedDateString: String?
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // 搜索栏主要部分
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 返回按钮
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 搜索输入框
                OutlinedTextField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            onFocusChanged(focusState.isFocused)
                        },
                    placeholder = {
                        Text(
                            text = if (hasSearchResults) "点击清空并重新搜索" else "搜索新闻...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        Row {
                            // 日期选择按钮
                            IconButton(
                                onClick = onDatePickerClick,
                                modifier = Modifier.size(40.dp)
                            ) {
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

                            // 清除按钮
                            if (searchText.isNotEmpty()) {
                                IconButton(
                                    onClick = onClearSearch,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "清除",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // 搜索按钮
                            IconButton(
                                onClick = onSearchSubmit,
                                enabled = !isLoading && (searchText.isNotBlank() || hasDateFilter),
                                modifier = Modifier.size(40.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
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
                        imeAction = ImeAction.Search,
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Text
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { onSearchSubmit() }
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                    ),
                    // 整体点击处理
                    interactionSource = remember { MutableInteractionSource() }.also { interactionSource ->
                        LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect { interaction ->
                                when (interaction) {
                                    is PressInteraction.Press -> {
                                        onSearchBarClick()
                                    }
                                }
                            }
                        }
                    }
                )
            }

            // 当前搜索说明（可选）
            if (hasDateFilter && selectedDateString != null) {
                val dateDisplayText = when {
                    selectedDateString.count { it == '-' } == 2 -> // YYYY-MM-DD
                        "精确日期搜索"
                    selectedDateString.count { it == '-' } == 1 -> // YYYY-MM
                        "按月份搜索"
                    else -> // YYYY
                        "按年份搜索"
                }

                Text(
                    text = dateDisplayText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * 搜索结果内容
 */
@Composable
private fun SearchResultContent(
    searchResultState: UiState<List<News>>,
    onNewsClick: (News) -> Unit
) {
    when (searchResultState) {
        is UiState.Loading -> {
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

        is UiState.Success -> {
            val searchResults = searchResultState.data
            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "未找到相关新闻",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "尝试使用其他关键词搜索",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 搜索结果统计
                    item {
                        Text(
                            text = "找到 ${searchResults.size} 条相关新闻",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // 搜索结果列表
                    items(
                        items = searchResults,
                        key = { news -> news.id }
                    ) { news ->
                        SearchResultItem(
                            news = news,
                            onClick = { onNewsClick(news) }
                        )
                    }
                }
            }
        }

        is UiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "搜索失败",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = searchResultState.message,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        is UiState.Empty -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "输入关键词开始搜索",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 搜索结果项
 */
@Composable
private fun SearchResultItem(
    news: News,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 新闻摘要
            Text(
                text = news.content,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 新闻信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = news.publisher,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = formatPublishTime(news.publishTime),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 格式化发布时间
 */
private fun formatPublishTime(publishTime: String): String {
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
