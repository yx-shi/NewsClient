package com.example.newsclient

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.newsclient.data.model.NewsResponse
import com.example.newsclient.data.remote.NewsApi
import com.example.newsclient.ui.theme.NewsClientTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewsClientTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        // 添加协程作用域
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val testResponse = NewsApi.service.getNewsList(
                    page = 1,
                    startDate = "2023-01-01",
                    endDate = "2023-01-02",
                    categories = "科技"
                )
                Log.d("NetworkTest", "请求成功: ${testResponse.data.size} 条数据")
            } catch (e: Exception) {
                Log.e("NetworkTest", "请求失败", e)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NewsClientTheme {
        Greeting("Android")
    }
}