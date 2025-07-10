package com.example.newsclient.fake

import com.example.newsclient.data.model.NewsResponse
import com.example.newsclient.data.remote.NewsApiService
import retrofit2.http.Query

class FakeNewsApiService : NewsApiService {
    override suspend fun getNewsList(
        @Query(value = "page") page: Int,
        @Query(value = "size") size: Int,
        @Query(value = "startDate") startDate: String?,
        @Query(value = "endDate") endDate: String?,
        @Query(value = "words") keyword: String?,
        @Query(value = "categories") categories: String?
    ): NewsResponse {
        // 模拟返回数据
        return FakeDataSource.newsResponse
    }
}