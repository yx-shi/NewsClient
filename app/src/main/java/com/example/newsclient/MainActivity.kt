package com.example.newsclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.newsclient.data.model.News
import com.example.newsclient.data.model.NewsCategory
import com.example.newsclient.ui.screen.NewsListScreen
import com.example.newsclient.ui.screen.SimplifiedSearchScreen
import com.example.newsclient.ui.screen.NewsDetailScreen
import com.example.newsclient.ui.screen.CategoryManagementScreen
import com.example.newsclient.ui.screen.ProfileScreen
import com.example.newsclient.ui.screen.HistoryScreen
import com.example.newsclient.ui.screen.FavoriteScreen
import com.example.newsclient.ui.screen.TestScreen
import com.example.newsclient.ui.theme.NewsClientTheme
import com.example.newsclient.ui.viewmodel.NewsDetailViewModel

/**
 * 主Activity
 * 负责应用的主要导航和界面管理
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewsClientTheme {
                NewsApp()
            }
        }
    }
}

/**
 * 底部导航项
 */
sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Home : BottomNavItem("home", Icons.Default.Home, "首页")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "我的")
}

/**
 * 新闻应用主体
 * 包含底部导航和所有界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsApp() {
    val navController = rememberNavController()
    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            BottomNavigation(
                navController = navController,
                items = bottomNavItems
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            // 首页（新闻列表）
            composable(BottomNavItem.Home.route) {
                NewsListScreen(
                    onNewsClick = { news ->
                        // 缓存新闻数据并使用新闻ID进行导航
                        NewsDetailViewModel.cacheNews(news)
                        navController.navigate("news_detail/${news.id}")
                    },
                    onSearchClick = { currentCategory ->
                        // 传递当前分类信息到搜索界面
                        android.util.Log.d("MainActivity", "🔍 搜索栏被点击，当前分类: ${currentCategory?.value ?: "全部"}")
                        try {
                            val categoryParam = currentCategory?.value?.let {
                                java.net.URLEncoder.encode(it, "UTF-8")
                            } ?: "all"
                            navController.navigate("search/$categoryParam")
                            android.util.Log.d("MainActivity", "✅ 导航到搜索界面成功，分类: $categoryParam")
                        } catch (e: Exception) {
                            android.util.Log.e("MainActivity", "❌ 导航到搜索界面失败", e)
                        }
                    },
                    onCategoryManageClick = {
                        // 导航到分类管理界面
                        android.util.Log.d("MainActivity", "🛠️ 分类管理按钮被点击")
                        navController.navigate("category_management")
                    }
                )
            }

            // 我的页面
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onHistoryClick = {
                        navController.navigate("history")
                    },
                    onFavoriteClick = {
                        navController.navigate("favorite")
                    },
                    onSettingsClick = {
                        // 预留设置功能
                    }
                )
            }

            // 历史记录页面
            composable("history") {
                HistoryScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onNewsClick = { news ->
                        NewsDetailViewModel.cacheNews(news)
                        navController.navigate("news_detail/${news.id}")
                    }
                )
            }

            // 收藏页面
            composable("favorite") {
                FavoriteScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onNewsClick = { news ->
                        NewsDetailViewModel.cacheNews(news)
                        navController.navigate("news_detail/${news.id}")
                    }
                )
            }

            // 新闻详情界面
            composable(
                route = "news_detail/{newsId}",
                arguments = listOf(navArgument("newsId") { type = NavType.StringType })
            ) { backStackEntry ->
                val newsId = backStackEntry.arguments?.getString("newsId")

                // 使用新闻ID获取详情
                NewsDetailWrapper(
                    newsId = newsId,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            // 搜索界面 - 使用简化版
            composable(
                route = "search/{category}",
                arguments = listOf(navArgument("category") { type = NavType.StringType })
            ) { backStackEntry ->
                val categoryParam = backStackEntry.arguments?.getString("category")
                val currentCategory = when {
                    categoryParam == "all" -> null
                    categoryParam != null -> {
                        try {
                            val decodedCategory = java.net.URLDecoder.decode(categoryParam, "UTF-8")
                            NewsCategory.entries.find { it.value == decodedCategory }
                        } catch (e: Exception) {
                            null
                        }
                    }
                    else -> null
                }

                SimplifiedSearchScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onNewsClick = { news ->
                        NewsDetailViewModel.cacheNews(news)
                        navController.navigate("news_detail/${news.id}")
                    },
                    currentCategory = currentCategory
                )
            }

            // 分类管理界面
            composable("category_management") {
                CategoryManagementScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            // 测试界面 - 用于调试API
            composable("test") {
                TestScreen()
            }
        }
    }
}

/**
 * 底部导航栏
 */
@Composable
private fun BottomNavigation(
    navController: NavHostController,
    items: List<BottomNavItem>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(text = item.label)
                },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // 避免多次点击同一个tab时重复创建实例
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}

/**
 * 新闻详情包装组件
 * 负责处理新闻详情的解析和错误处理
 */
@Composable
fun NewsDetailWrapper(
    newsId: String?,
    onBackClick: () -> Unit
) {
    // 在组件内部处理解析逻辑，避免在composable调用周围使用try-catch
    val newsState = remember(newsId) {
        if (newsId != null) {
            // 从缓存中获取新闻数据
            val news = NewsDetailViewModel.getCachedNews(newsId)
            if (news != null) {
                NewsState.Success(news)
            } else {
                NewsState.Error("未找到新闻数据")
            }
        } else {
            NewsState.Error("新闻ID为空")
        }
    }

    when (newsState) {
        is NewsState.Success -> {
            NewsDetailScreen(
                news = newsState.news,
                onBackClick = onBackClick
            )
        }
        is NewsState.Error -> {
            // 显示错误信息或直接返回
            LaunchedEffect(Unit) {
                onBackClick()
            }
        }
    }
}

/**
 * 新闻状态密封类
 */
private sealed class NewsState {
    data class Success(val news: News) : NewsState()
    data class Error(val message: String) : NewsState()
}
