package com.example.newsclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.newsclient.ui.screen.NewsListScreen
import com.example.newsclient.ui.screen.SearchScreen
import com.example.newsclient.ui.screen.TestScreen
import com.example.newsclient.ui.theme.NewsClientTheme

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
                    // 点击新闻时的处理逻辑
                    // 这里可以导航到新闻详情页
                    // 暂时先打印日志
                    println("点击了新闻: ${news.title}")
                },
                onSearchClick = {
                    // 点击搜索时导航到搜索界面
                    navController.navigate("search")
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
                    // 点击搜索结果中的新闻
                    // 这里可以导航到新闻详情页
                    println("点击了搜索结果: ${news.title}")
                }
            )
        }

        // 测试界面 - 用于调试API
        composable("test") {
            TestScreen()
        }
    }
}