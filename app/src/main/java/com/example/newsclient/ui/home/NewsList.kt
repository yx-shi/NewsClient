package com.example.newsclient.ui.home

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import com.example.newsclient.ui.theme.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.newsclient.data.model.News
import com.example.newsclient.ui.NewsListState
import com.example.newsclient.ui.NewsViewModel
import com.example.newsclient.ui.UiState


@Composable
fun ShowNewsList(
    viewModel: NewsViewModel= NewsViewModel(), // 默认使用ViewModel实例
    onNewsClick: (String) -> Unit ={}// 新闻点击回调
) {
    val newsState by viewModel.newsState.collectAsState()

    // 处理不同状态
    when (val state = newsState) {
        is UiState.Loading -> {
            Log.d("NewsList","新闻加载中")
        }
        is UiState.Success -> NewsListContent(
            NewsList = state.data.news,
            onLoadMore = { viewModel.loadNewsList() },
            onNewsClick = onNewsClick
        )
        is UiState.Error -> {
            Log.e("NewsList", "加载新闻失败: ${state.message}")
            // 可以在这里显示错误提示UI
        }
        UiState.Empty -> {
            Log.d("NewsList","新闻列表为空")
        }
    }
}

@Composable
fun NewsListContent(
    NewsList: List<News>,
    onLoadMore: () -> Unit,
    onNewsClick: (String) -> Unit
) {
    val listState = rememberLazyListState()

    LazyColumn(state = listState) {
        items(NewsList) { news ->
            NewsItem(
                news = news,
                onClick = { onNewsClick(news.id) }
            )
        }

//        // 分页加载
//        if (!state.endReached && state.news.isNotEmpty()) {
//            item {
//                LoadingNextPageItem()
//                if (listState.isScrolledToEnd()) onLoadMore()
//            }
//        }
    }
}

@Composable
private fun NewsItem(
    news: News,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 新闻项的UI逻辑
    // 例如使用Card或其他组件展示新闻标题、摘要等
    // 点击事件可以通过onClick回调传递给父组件
    Card(
        modifier = modifier,
        onClick = onClick
    ){
        Column{
            Text(
                text = news.title,
                style=Typography.bodyLarge,
                modifier = Modifier.padding(16.dp) // 添加内边距
            )
        }

    }
}