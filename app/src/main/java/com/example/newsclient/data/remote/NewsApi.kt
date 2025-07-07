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

interface NewsApi {
    @GET("svc/news/queryNewsList")
    suspend fun getNewsList(
        @Query("page") page: Int,
        @Query("size") size: Int = 15,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("words") keyword: String? = null,
        @Query("categories") category: String? = null
    ): NewsResponse

    companion object {
        fun create(): NewsApi {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(
                    GsonConverterFactory.create(
                    GsonBuilder()
                        .registerTypeAdapter(News::class.java, NewsDeserializer())
                        .create()
                ))
                .build()
            return retrofit.create(NewsApi::class.java)
        }
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