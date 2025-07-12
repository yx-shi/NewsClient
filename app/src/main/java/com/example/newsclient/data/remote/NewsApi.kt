package com.example.newsclient.data.remote

import com.example.newsclient.data.model.Keyword
import com.example.newsclient.data.model.News
import com.example.newsclient.data.model.NewsResponse
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import retrofit2.http.GET
import retrofit2.http.Query
import java.lang.reflect.Type

/**
 * @author Shi
 * @version 1.0
 * This interface defines the API endpoints for fetching news articles.
 */

// 根据接口文档，修改日期格式为服务器要求的格式
const val START_DATE = "2024-06-15 00:00:00"  // 修改为正确的日期格式（包含时间）
const val END_DATE = "2025-07-01 23:59:59"    // 修改为正确的日期格式（包含时间）

/**
 * 定义retrofit的API接口
 */
interface NewsApiService {
    @GET("svc/news/queryNewsList")
    suspend fun getNewsList(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 15,
        @Query("startDate") startDate: String = START_DATE,
        @Query("endDate") endDate: String = END_DATE,
        @Query("words") keyword: String = "",        // 改为必填，默认空字符串
        @Query("categories") categories: String = ""  // 改为必填，默认空字符串
    ): NewsResponse

    @GET("svc/news/queryNewsList")
    suspend fun searchNews(
        @Query("words") keyword: String,
        @Query("categories") categories: String = "",
        @Query("startDate") startDate: String = START_DATE,
        @Query("endDate") endDate: String = END_DATE,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 100  // 搜索时获取更多结果用于排序
    ): NewsResponse

    @GET("svc/news/queryNewsList")
    suspend fun searchNewsByDate(
        @Query("startDate") dateQuery: String,
        @Query("endDate") endDate: String = dateQuery, // 默认结束日期与开始日期相同
        @Query("categories") categories: String = "",
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 100  // 获取更多结果
    ): NewsResponse

    @GET("svc/news/queryNewsList")
    suspend fun searchNewsCombined(
        @Query("words") keyword: String,
        @Query("startDate") dateQuery: String,
        @Query("endDate") endDate: String = dateQuery, // 默认结束日期与开始日期相同
        @Query("categories") categories: String = "",
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 100  // 获取更多结果用于排序
    ): NewsResponse
}

/**
 * 重写Gson的反序列化器，用于处理News模型的JSON转换。
 * 这个反序列化器主要处理image字段可能为数组或空的情况。
 */
class NewsDeserializer : JsonDeserializer<News> {
    // This deserializer handles the conversion of JSON to the News model.
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): News {
        val jsonObject = json.asJsonObject

        // 处理image字段可能为数组字符串或空的情况
        val imageElement = jsonObject.get("image")
        val imageUrl = when {
            imageElement.isJsonPrimitive && !imageElement.isJsonNull -> {
                val imageStr = imageElement.asString?.trim() ?: ""
                when {
                    imageStr.isEmpty() || imageStr == "[]" -> ""
                    imageStr.startsWith("[") && imageStr.endsWith("]") -> {
                        // 处理数组格式的字符串，如 "[http://..., http://...]"
                        val urlsString = imageStr.substring(1, imageStr.length - 1)
                        urlsString.split(",")
                            .asSequence()
                            .map { it.trim().removePrefix("\"").removeSuffix("\"") }
                            .filter { it.isNotEmpty() && it.startsWith("http") }
                            .firstOrNull() ?: ""
                    }
                    imageStr.startsWith("http") -> imageStr
                    else -> ""
                }
            }
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
            ) ?: emptyList(),
            publisher = jsonObject.get("publisher").asString
        )
    }
}