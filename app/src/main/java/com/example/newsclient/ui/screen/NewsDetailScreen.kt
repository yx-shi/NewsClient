package com.example.newsclient.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.newsclient.data.model.News

/**
 * 新闻详情界面
 * 显示新闻的完整内容，包括标题、图片/视频、正文等
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    news: News,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // 在进入详情页时标记为已读
    LaunchedEffect(news.id) {
        val userPreferences = com.example.newsclient.data.local.UserPreferences(context)
        userPreferences.addToHistory(news)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // 顶部导航栏
        TopAppBar(
            title = {
                Text(
                    text = "新闻详情",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.Black
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        // 新闻内容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 新闻标题
            Text(
                text = news.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                lineHeight = 32.sp
            )

            // 新闻元信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "来源：${news.publisher}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "时间：${news.publishTime}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                // 分类标签
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = news.category,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // 新闻媒体内容（图片或视频）
            NewsMediaContent(
                news = news,
                modifier = Modifier.fillMaxWidth()
            )

            // 新闻正文
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "正文",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = news.content,
                        fontSize = 16.sp,
                        color = Color.Black,
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Justify
                    )
                }
            }

            // 关键词标签
            if (news.keywords.isNotEmpty()) {
                KeywordTagsSection(
                    keywords = news.keywords,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 收藏按钮
            FavoriteButton(
                news = news,
                modifier = Modifier.fillMaxWidth()
            )

            // 底部间距
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * 新闻媒体内容组件（图片或视频）
 */
@Composable
private fun NewsMediaContent(
    news: News,
    modifier: Modifier = Modifier
) {
    // 优先显示视频，如果没有视频则显示图片
    when {
        news.videoUrl.isNotEmpty() -> {
            VideoPlayer(videoUrl = news.videoUrl, modifier = modifier)
        }
        news.imageUrl.isNotEmpty() -> {
            NewsImage(imageUrl = news.imageUrl, modifier = modifier)
        }
    }
}

/**
 * 新闻图片组件
 */
@Composable
private fun NewsImage(imageUrl: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(parseImageUrl(imageUrl))
                .crossfade(true)
                .build(),
            contentDescription = "新闻图片",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 300.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Gray.copy(alpha = 0.1f)),
            error = painterResource(id = android.R.drawable.ic_menu_gallery)
        )
    }
}

/**
 * 视频播放器组件
 */
@Composable
private fun VideoPlayer(videoUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isPlayerReady by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    // 点击播放视频
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                    context.startActivity(intent)
                }
        ) {
            // 视频缩略图或加载中的占位符
            NewsImage(imageUrl = videoUrl)

            // 播放按钮覆盖层
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "播放",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }

    // 监听播放器状态
    LaunchedEffect(isPlayerReady) {
        if (isPlayerReady) {
            // TODO: 这里可以添加视频准备好的逻辑
        }
    }
}

/**
 * 关键词标签区域
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KeywordTagsSection(
    keywords: List<com.example.newsclient.data.model.Keyword>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "关键词",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 使用FlowRow显示关键词标签
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                keywords.forEach { keyword ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = keyword.word,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 收藏按钮组件
 */
@Composable
private fun FavoriteButton(
    news: News,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userPreferences = remember { com.example.newsclient.data.local.UserPreferences(context) }

    // 检查是否已收藏 - 使用Flow来监听收藏状态变化
    val favoriteNews by userPreferences.getFavoriteNewsFlow().collectAsState(initial = emptyList())
    val isFavorite = favoriteNews.any { it.news.id == news.id }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (isFavorite) {
                        userPreferences.removeFromFavorites(news.id)
                        android.util.Log.d("NewsDetailScreen", "取消收藏: ${news.title}")
                    } else {
                        userPreferences.addToFavorites(news)
                        android.util.Log.d("NewsDetailScreen", "添加收藏: ${news.title}")
                    }
                }
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isFavorite) "取消收藏" else "收藏",
                tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (isFavorite) "已收藏" else "收藏",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isFavorite) Color.Red else MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * 解析图片URL
 * 处理可能的数组格式图片URL
 */
private fun parseImageUrl(imageUrl: String): String {
    return if (imageUrl.startsWith("[") && imageUrl.endsWith("]")) {
        // 如果是数组格式，取第一个URL
        val urls = imageUrl.removeSurrounding("[", "]")
            .split(",")
            .map { it.trim() }
        urls.firstOrNull()?.takeIf { it.isNotEmpty() } ?: ""
    } else {
        imageUrl
    }
}

/**
 * 格式化发布时间
 */
private fun formatPublishTime(publishTime: String): String {
    return try {
        // 简单的时间格式化
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
