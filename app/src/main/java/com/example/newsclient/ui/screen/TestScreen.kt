package com.example.newsclient.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newsclient.ui.NewsViewModel
import kotlinx.coroutines.launch

/**
 * 测试界面
 * 用于直接测试API调用和调试网络问题
 */
@Composable
fun TestScreen(
    viewModel: NewsViewModel = viewModel(factory = NewsViewModel.Factory)
) {
    val scope = rememberCoroutineScope()
    var testResult by remember { mutableStateOf("点击按钮开始测试...") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "API 测试界面",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                scope.launch {
                    testResult = "正在测试API..."
                    try {
                        // 直接调用ViewModel的刷新方法
                        viewModel.refreshNews()
                        testResult = "API调用已触发，请查看Logcat日志"
                    } catch (e: Exception) {
                        testResult = "测试失败: ${e.message}"
                    }
                }
            }
        ) {
            Text("测试获取新闻API")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = testResult,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "请查看Android Studio的Logcat，" +
                    "过滤标签：NetworkNewsRepository 和 OkHttp",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
