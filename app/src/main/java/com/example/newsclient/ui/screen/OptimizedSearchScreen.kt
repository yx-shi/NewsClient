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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import kotlinx.coroutines.delay

/**
 * 优化后的搜索界面
 * 解决点击反应不灵敏的问题
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptimizedSearchScreen(
    onBackClick: () -> Unit,
    onNewsClick: (News) -> Unit,
    currentCategory: NewsCategory? = null,
    viewModel: NewsViewModel = viewModel(factory = NewsViewModel.Factory)
) {
    // 使用稳定的状态管理
    var searchText by remember { mutableStateOf("") }
    var isSearchCompleted by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // 防抖处理 - 避免频繁重组
    var isProcessing by remember { mutableStateOf(false) }

    // 收集搜索结果状态
    val searchResultState by viewModel.searchResultState.collectAsState()

    // 监听搜索状态变化
    LaunchedEffect(searchResultState) {
        when (searchResultState) {
            is UiState.Success -> {
                isSearchCompleted = true
                isProcessing = false
                Log.d("OptimizedSearchScreen", "✅ 搜索完成")
            }
            is UiState.Error -> {
                isSearchCompleted = true
                isProcessing = false
                Log.d("OptimizedSearchScreen", "❌ 搜索错误")
            }
            is UiState.Loading -> {
                isSearchCompleted = false
                isProcessing = true
                Log.d("OptimizedSearchScreen", "🔄 搜索中...")
            }
            is UiState.Empty -> {
                isSearchCompleted = false
                isProcessing = false
                Log.d("OptimizedSearchScreen", "🔍 空状态")
            }
        }
    }

    // 初始化时聚焦搜索框
    LaunchedEffect(Unit) {
        delay(100) // 等待界面渲染完成
        try {
            focusRequester.requestFocus()
            Log.d("OptimizedSearchScreen", "✅ 搜索框聚焦成功")
        } catch (e: Exception) {
            Log.e("OptimizedSearchScreen", "❌ 搜索框聚焦失败", e)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 优化的搜索栏
        OptimizedSearchBar(
            searchText = searchText,
            onSearchTextChange = { newText ->
                searchText = newText
                if (newText.isEmpty()) {
                    // 清空搜索时重置搜索结果
                    viewModel.clearSearchResults()
                    isSearchCompleted = false
                }
            },
            onBackClick = {
                Log.d("OptimizedSearchScreen", "🔙 返回按钮被点击")
                try {
                    keyboardController?.hide()
                    onBackClick()
                } catch (e: Exception) {
                    Log.e("OptimizedSearchScreen", "❌ 返回失败", e)
                }
            },
            onSearchSubmit = {
                Log.d("OptimizedSearchScreen", "🔍 搜索提交: '$searchText'")
                if (searchText.isNotBlank()) {
                    try {
                        viewModel.searchNews(searchText.trim(), currentCategory)
                        keyboardController?.hide()
                    } catch (e: Exception) {
                        Log.e("OptimizedSearchScreen", "❌ 搜索失败", e)
                    }
                }
            },
            onClearSearch = {
                Log.d("OptimizedSearchScreen", "🧹 清空搜索")
                try {
                    searchText = ""
                    viewModel.clearSearchResults()
                    isSearchCompleted = false
                    // 清空后重新聚焦
                    focusRequester.requestFocus()
                } catch (e: Exception) {
                    Log.e("OptimizedSearchScreen", "❌ 清空失败", e)
                }
            },
            onSearchBarClick = {
                Log.d("OptimizedSearchScreen", "📱 搜索栏被点击")
                try {
                    if (isSearchCompleted) {
                        // 如果已经搜索完成，点击时重新聚焦并可选择性清空
                        if (searchText.isNotEmpty()) {
                            // 保留搜索文字，重新聚焦
                            focusRequester.requestFocus()
                        } else {
                            // 如果没有搜索文字，直接聚焦
                            focusRequester.requestFocus()
                        }
                    } else {
                        // 如果没有搜索完成，正常聚焦
                        focusRequester.requestFocus()
                    }
                } catch (e: Exception) {
                    Log.e("OptimizedSearchScreen", "❌ 搜索栏点击处理失败", e)
                }
            },
            focusRequester = focusRequester,
            isSearchCompleted = isSearchCompleted,
            isProcessing = isProcessing && searchResultState is UiState.Loading
        )

        // 分类显示
        if (currentCategory != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = "在「${currentCategory.value}」分类中搜索",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // 搜索结果
        SearchResultContent(
            searchResultState = searchResultState,
            onNewsClick = onNewsClick
        )
    }
}

/**
 * 优化的搜索栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptimizedSearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSearchSubmit: () -> Unit,
    onClearSearch: () -> Unit,
    onSearchBarClick: () -> Unit,
    focusRequester: FocusRequester,
    isSearchCompleted: Boolean,
    isProcessing: Boolean
) {
    // 使用Surface替代Card，减少重组开销
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 返回按钮 - 现在任何时候都可以使用
            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // 搜索输入框容器 - 添加点击处理
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        Log.d("OptimizedSearchBar", "📱 搜索框区域被点击")
                        onSearchBarClick()
                    }
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text("搜索新闻...", color = Color.Gray)
                    },
                    trailingIcon = {
                        Row {
                            // 清除按钮 - 现在任何时候都可以使用
                            if (searchText.isNotEmpty()) {
                                IconButton(
                                    onClick = onClearSearch
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "清除",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            // 搜索按钮
                            IconButton(
                                onClick = onSearchSubmit,
                                enabled = !isProcessing && searchText.isNotBlank()
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "搜索",
                                        tint = if (searchText.isNotBlank()) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            Color.Gray
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
                        unfocusedBorderColor = Color.Gray,
                        disabledBorderColor = Color.LightGray
                    )
                )
            }
        }
    }
}

/**
 * 搜索结果内容（复用原有实现）
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
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "正在搜索...",
                        fontSize = 16.sp,
                        color = Color.Gray
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
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "尝试使用其他关键词搜索",
                            fontSize = 14.sp,
                            color = Color.Gray
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
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // 搜索结果列表
                    items(
                        items = searchResults,
                        key = { news -> news.id }
                    ) { news ->
                        OptimizedSearchResultItem(
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
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = searchResultState.message,
                        fontSize = 14.sp,
                        color = Color.Gray
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
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * 优化的搜索结果项
 */
@Composable
private fun OptimizedSearchResultItem(
    news: News,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        tonalElevation = 2.dp,
        color = Color.White
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

            // 新闻摘要
            Text(
                text = news.content,
                fontSize = 14.sp,
                color = Color.Gray,
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
