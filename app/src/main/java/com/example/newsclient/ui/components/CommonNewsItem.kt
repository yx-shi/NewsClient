package com.example.newsclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.newsclient.data.model.News

/**
 * 通用新闻条目组件
 * 用于新闻列表、历史记录、收藏等页面
 */
@Composable
fun CommonNewsItem(
    news: News,
    isRead: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isRead) Color(0xFFF5F5F5) else Color.White
        ),
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
                color = if (isRead) Color.Gray else Color.Black,
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
                                android.util.Log.w("CommonNewsItem", "图片加载失败: $finalImageUrl, 错误: ${result.throwable.message}")

                                // 如果是HTTPS失败且还没尝试过HTTP回退，则尝试HTTP
                                if (!hasTriedFallback && finalImageUrl.startsWith("https://")) {
                                    val httpUrl = finalImageUrl.replaceFirst("https://", "http://")
                                    android.util.Log.i("CommonNewsItem", "尝试HTTP回退: $httpUrl")
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
                        .let { modifier ->
                            if (isRead) {
                                modifier.background(Color.Gray.copy(alpha = 0.1f))
                            } else {
                                modifier
                            }
                        }
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // 新闻摘要
            Text(
                text = news.content,
                fontSize = 14.sp,
                color = if (isRead) Color.Gray.copy(alpha = 0.8f) else Color.Gray,
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
                    color = if (isRead) Color.Gray.copy(alpha = 0.7f) else Color.Gray
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRead) {
                        Text(
                            text = "已读",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier
                                .background(
                                    Color.Gray.copy(alpha = 0.2f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = formatPublishTime(news.publishTime),
                        fontSize = 12.sp,
                        color = if (isRead) Color.Gray.copy(alpha = 0.7f) else Color.Gray
                    )
                }
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
            val timePart = parts[1].substring(0, 5) // 只取小时和分钟
            "$datePart $timePart"
        } else {
            publishTime
        }
    } catch (e: Exception) {
        publishTime
    }
}
