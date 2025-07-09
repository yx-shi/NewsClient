package com.example.newsclient.data.remote

import com.example.newsclient.data.model.Keyword
import com.example.newsclient.data.model.News
import com.example.newsclient.data.model.NewsResponse
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.lang.reflect.Type

/**
 * @author Shi
 * @version 1.0
 * This interface defines the API endpoints for fetching news articles.
 */
private const val BASE_URL = "https://api2.newsminer.net/"

/**
 * 定义retrofit的API接口
 */
interface NewsApiService {
    @GET("svc/news/queryNewsList")
    suspend fun getNewsList(
        @Query("page") page: Int=1,
        @Query("size") size: Int = 15,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("words") keyword: String? = "",
        @Query("categories") categories: String? = null
    ): NewsResponse
}

/**
 * 公开的单例对象，用于初始化Retrofit并提供API服务访问点。
 */
// 单例对象负责Retrofit初始化
object NewsApi {
    private const val BASE_URL = "https://api2.newsminer.net/"

    // 使用懒加载初始化Retrofit
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
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
}

class NewsDeserializer : JsonDeserializer<News> {
    // This deserializer handles the conversion of JSON to the News model.
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): News {
        val jsonObject = json.asJsonObject

        // 处理image字段可能为数组或空的情况
        val imageElement = jsonObject.get("image")
        val imageUrl = when {
            imageElement.isJsonArray -> {
                val array = imageElement.asJsonArray
                if (array.size() > 0) array[0].asString else ""
            }
            imageElement.isJsonPrimitive -> imageElement.asString
            else -> ""
        }

        return News(
            id = jsonObject.get("newsID").asString,
            title = jsonObject.get("title").asString,
            content = jsonObject.get("content").asString,
            videoUrl = jsonObject.get("video").asString,
            imageUrl = imageUrl,
            publishTime = jsonObject.get("publishTime").asString,
            category = jsonObject.get("category").asString,
            keywords = context?.deserialize<List<Keyword>>(
                jsonObject.get("keywords"),
                object : TypeToken<List<Keyword>>() {}.type
            ) ?: emptyList()
        )
    }
}