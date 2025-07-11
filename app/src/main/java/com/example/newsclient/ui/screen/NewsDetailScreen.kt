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
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回"
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
                .padding(16.dp)
        ) {
            // 新闻标题
            NewsTitle(title = news.title)

            Spacer(modifier = Modifier.height(12.dp))

            // 发布信息
            PublishInfo(
                publisher = news.publisher,
                publishTime = news.publishTime
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 媒体内容（图片或视频）
            MediaContent(
                imageUrl = news.imageUrl,
                videoUrl = news.videoUrl
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 新闻正文
            NewsContent(content = news.content)

            // 底部间距
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 新闻标题组件
 */
@Composable
private fun NewsTitle(title: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(16.dp),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            lineHeight = 28.sp
        )
    }
}

/**
 * 发布信息组件
 */
@Composable
private fun PublishInfo(
    publisher: String,
    publishTime: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = publisher,
                fontSize = 14.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )

            Text(
                text = formatPublishTime(publishTime),
                fontSize = 14.sp,
                color = Color(0xFF999999)
            )
        }
    }
}

/**
 * 媒体内容组件（图片或视频）
 */
@Composable
private fun MediaContent(
    imageUrl: String,
    videoUrl: String
) {
    // 优先显示视频，如果没有视频则显示图片
    when {
        videoUrl.isNotEmpty() -> {
            VideoPlayer(videoUrl = videoUrl)
        }
        imageUrl.isNotEmpty() -> {
            NewsImage(imageUrl = imageUrl)
        }
    }
}

/**
 * 新闻图片组件
 */
@Composable
private fun NewsImage(imageUrl: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
private fun VideoPlayer(videoUrl: String) {
    val context = LocalContext.current
    var isPlayerReady by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
 * 新闻正文组件
 */
@Composable
private fun NewsContent(content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = content,
            modifier = Modifier.padding(16.dp),
            fontSize = 16.sp,
            color = Color.Black,
            lineHeight = 24.sp,
            textAlign = TextAlign.Justify
        )
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
