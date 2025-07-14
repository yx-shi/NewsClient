package com.example.newsclient.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newsclient.NewsApplication
import com.example.newsclient.data.local.UserPreferences
import com.example.newsclient.ui.UiState
import com.example.newsclient.ui.viewmodel.HistoryViewModel

/**
 * "我的"页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onHistoryClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onPersonalizationClick: () -> Unit = {}, // 新增个性化设置点击回调
    historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
) {
    // 添加调试日志
    android.util.Log.d("ProfileScreen", "ProfileScreen 被创建")

    val context = LocalContext.current
    // 使用应用级别的UserPreferences实例，确保数据一致性
    val application = context.applicationContext as NewsApplication
    val userPreferences = application.userPreferences
    val historyState by historyViewModel.uiState.collectAsState()

    // 获取收藏数量 - 使用Flow来实时监听，并添加强制刷新机制
    val favoriteNews by userPreferences.getFavoriteNewsFlow().collectAsState(initial = emptyList())
    val favoriteCount = favoriteNews.size

    // 添加调试日志
    android.util.Log.d("ProfileScreen", "当前收藏数量: $favoriteCount")

    // 添加更详细的调试信息
    LaunchedEffect(favoriteNews) {
        android.util.Log.d("ProfileScreen", "收藏数据更新: ${favoriteNews.size} 条")
        favoriteNews.forEach { favorite ->
            android.util.Log.d("ProfileScreen", "收藏项: ${favorite.news.title}")
        }
    }

    // 页面显示时强制刷新一次收藏数据
    LaunchedEffect(Unit) {
        userPreferences.refreshFavoriteFlow()
        android.util.Log.d("ProfileScreen", "ProfileScreen初始化，强制刷新收藏数据")
    }

    // 使用DisposableEffect来监听页面的生命周期
    DisposableEffect(Unit) {
        android.util.Log.d("ProfileScreen", "ProfileScreen进入前台")
        // 页面进入前台时刷新数据
        userPreferences.refreshFavoriteFlow()

        onDispose {
            android.util.Log.d("ProfileScreen", "ProfileScreen离开前台")
        }
    }

    // 计算历史记录数量
    val historyCount = when (val currentState = historyState) {
        is UiState.Success -> {
            android.util.Log.d("ProfileScreen", "历史记录数量: ${currentState.data.size}")
            currentState.data.size
        }
        else -> {
            android.util.Log.d("ProfileScreen", "历史记录状态: $currentState")
            0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 顶部用户信息区域
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 用户头像
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "用户头像",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 用户信息
                Column {
                    Text(
                        text = "新闻阅读者",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "已阅读 $historyCount 篇新闻 · 收藏 $favoriteCount 篇",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 功能菜单区域
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                // 历史记录
                ProfileMenuItem(
                    icon = Icons.Default.DateRange,
                    title = "历史记录",
                    subtitle = "$historyCount 篇新闻",
                    onClick = onHistoryClick
                )

                Divider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // 我的收藏
                ProfileMenuItem(
                    icon = Icons.Default.Favorite,
                    title = "我的收藏",
                    subtitle = "$favoriteCount 篇新闻",
                    onClick = onFavoriteClick
                )

                Divider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // 设置（预留功能）
                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    title = "个性化设置",
                    subtitle = "主题、字体等个性化设置",
                    onClick = onPersonalizationClick
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 应用信息
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "新闻客户端",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "版本 1.0.0",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "获取最新资讯，了解世界动态",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * 个人资料菜单项
 */
@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图标
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // 标题和副标题
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        // 右箭头
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "进入",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}
