package com.example.newsclient.data.model

import com.google.gson.annotations.SerializedName

/**
 * @author Shi
 * @version 1.0
 * the basic data model for the news，which is used to represent a news article.
 * the news coming from the server with format of JSON will be parsed into this model.
 */

enum class NewsCategory(val value: String) {
    ENTERTAINMENT("娱乐"),
    MILITARY("军事"),
    EDUCATION("教育"),
    CULTURE("文化"),
    HEALTH("健康"),
    FINANCE("财经"),
    SPORTS("体育"),
    AUTO("汽车"),
    TECHNOLOGY("科技"),
    SOCIETY("社会");

    companion object {
        val ALL_CATEGORIES = entries.map { it.value }
    }
}

//单条新闻数据
data class News(
    @SerializedName(value="newsID") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("video") val videoUrl: String,
    @SerializedName("image") val imageUrl: String,
    @SerializedName("publishTime") val publishTime: String,
    @SerializedName("category") val category: String,
    @SerializedName("keywords") val keywords: List<Keyword>,
    @SerializedName("publisher") val publisher: String
)

data class Keyword(
    @SerializedName("word") val word: String,
    @SerializedName("score") val score: Double
)

// API响应包装类,这个类的作用是将API响应的数据进行封装，
data class NewsResponse(
    @SerializedName("total") val total: Int,
    @SerializedName("data") val data: List<News>,
    @SerializedName("PageSize") val pageSize: Int
)