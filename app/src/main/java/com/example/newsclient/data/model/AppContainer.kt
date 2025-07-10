package com.example.newsclient.data.model

import com.example.newsclient.data.remote.NewsApiService
import com.example.newsclient.data.remote.NewsDeserializer
import com.example.newsclient.data.repository.NetworkNewsRepository
import com.example.newsclient.data.repository.NewsRepository
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type

interface AppContainer {
    val newsRepository: NewsRepository
}

class DefaultAppContainer : AppContainer {
    private val start_date = "2023-01-01"
    private val end_date = "2023-01-02"
    private val base_url = "https://api2.newsminer.net/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(base_url)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .registerTypeAdapter(News::class.java, NewsDeserializer())
                        .create()
                )
            )
            .build()
    }

    // 对外暴露的Service访问点，属性委托
    val service: NewsApiService by lazy {
        retrofit.create(NewsApiService::class.java)
    }

    override val newsRepository: NewsRepository by lazy {
        // 使用网络仓库作为默认实现
        NetworkNewsRepository(service)
        //TODO:修改仓库的实现
    }
}
