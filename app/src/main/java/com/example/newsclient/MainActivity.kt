package com.example.newsclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.newsclient.data.model.News
import com.example.newsclient.ui.screen.NewsListScreen
import com.example.newsclient.ui.screen.SearchScreen
import com.example.newsclient.ui.screen.NewsDetailScreen
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
 * 新闻应用主体
 * 包含导航和所有界面
 */
@Composable
fun NewsApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "news_list",
        modifier = Modifier.fillMaxSize()
    ) {
        // 新闻列表界面
        composable("news_list") {
            NewsListScreen(
                onNewsClick = { news ->
                    // 缓存新闻数据并使用新闻ID进行导航
                    NewsDetailViewModel.cacheNews(news)
                    navController.navigate("news_detail/${news.id}")
                },
                onSearchClick = {
                    // 点击搜索时导航到搜索界面
                    navController.navigate("search")
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

        // 搜索界面
        composable("search") {
            SearchScreen(
                onBackClick = {
                    // 返回新闻列表界面
                    navController.popBackStack()
                },
                onNewsClick = { news ->
                    // 缓存新闻数据并使用新闻ID进行导航
                    NewsDetailViewModel.cacheNews(news)
                    navController.navigate("news_detail/${news.id}")
                }
            )
        }

        // 测试界面 - 用于调试API
        composable("test") {
            TestScreen()
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
