package com.example.newsclient.data.model

import android.content.Context
import com.example.newsclient.data.remote.NewsApiService
import com.example.newsclient.data.remote.NewsDeserializer
import com.example.newsclient.data.repository.NetworkNewsRepository
import com.example.newsclient.data.repository.NewsRepository
import com.example.newsclient.data.repository.NewsLocalDataSource
import com.example.newsclient.data.repository.NewsRoomDatabase
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

/**
 * 应用程序容器接口
 * 定义了应用程序所需的依赖项
 */
interface AppContainer {
    val newsRepository: NewsRepository
}

/**
 * 默认应用程序容器实现
 * 负责创建和管理应用程序的依赖项
 */
class DefaultAppContainer(private val context: Context) : AppContainer {

    /**
     * API的基础URL
     * 如果主域名无法访问，可以尝试备用地址
     */
    private val base_url = "https://api2.newsminer.net/"
    // 备用URL（如果需要的话）
    // private val backup_url = "http://api2.newsminer.net/" // HTTP版本

    /**
     * HTTP日志拦截器
     * 用于调试网络请求和响应
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * OkHttp客户端
     * 添加日志拦截器和超时配置用于调试
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)    // 连接超时30秒
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)       // 读取超���30秒
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)      // 写入超时30秒
        .build()

    /**
     * Retrofit实例，用于网络请求
     * 使用lazy初始化，只有在第一次访问时才创建
     */
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(base_url)
            .client(okHttpClient)  // 添加OkHttp客户端
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        // 注册自定义的News反序列化器
                        .registerTypeAdapter(News::class.java, NewsDeserializer())
                        .create()
                )
            )
            .build()
    }

    /**
     * 新闻API服务
     * 对外暴露的Service访问点，使用属性委托
     */
    private val newsApiService: NewsApiService by lazy {
        retrofit.create(NewsApiService::class.java)
    }

    /**
     * 本地数据库实例 - 更新为使用NewsRoomDatabase
     * 用于缓存新闻数据和管理历史记录、收藏
     */
    private val newsRoomDatabase by lazy {
        NewsRoomDatabase.getDatabase(context)
    }

    /**
     * 数据库操作封装类 - 更新为使用NewsLocalDataSource
     * 提供对数据库的高级操作接口
     */
    private val newsLocalDataSource by lazy {
        NewsLocalDataSource(newsRoomDatabase.newsDao())
    }

    /**
     * 新���数据仓库
     * 实现依赖项注入，而不是由具体的ViewModel或其他类直接创建实例
     * 结合网络API和本地数据库，提供统一的数据访问接口
     */
    override val newsRepository: NewsRepository by lazy {
        // 临时使用模拟数据来测试界面功能，直到网络问题解决
        // 如果网络正常，可以切换回 NetworkNewsRepository
//        com.example.newsclient.data.repository.MockNewsRepository()

//         当网络问题解决后，取消注释下面这行，注释上面那行
         NetworkNewsRepository(
             newsApiService = newsApiService,
             newsLocalDataSource = newsLocalDataSource,
             glmApiService = glmApiService  // 添加GLM API服务
         )
    }

    /**
     * GLM API的基础URL
     */
    private val glm_base_url = "https://open.bigmodel.cn/api/paas/v4/"

    /**
     * GLM Retrofit实例，用于调用智谱AI大模型
     */
    private val glmRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(glm_base_url)
            .client(okHttpClient)  // 复用现有的OkHttp客户端
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * GLM API服务
     */
    private val glmApiService: com.example.newsclient.data.remote.GLMApiService by lazy {
        glmRetrofit.create(com.example.newsclient.data.remote.GLMApiService::class.java)
    }
}
