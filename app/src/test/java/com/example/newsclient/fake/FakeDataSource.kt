package com.example.newsclient.fake

import com.example.newsclient.data.model.Keyword
import com.example.newsclient.data.model.News
import com.example.newsclient.data.model.NewsResponse

object FakeDataSource {
    // 模拟数据源
    val newsList = listOf(
        News(
            id = "1",
            title = "Fake News 1",
            content = "This is a fake news content 1.",
            category = "科技",
            videoUrl = "",
            imageUrl = "",
            publishTime="1",
            keywords = listOf(
                Keyword("科技", 1.0),
                Keyword("新闻", 0.8)),
            publisher = "QQ"
        ),
        News(
            id = "2",
            title = "Fake News 2",
            content = "This is a fake news content 2.",
            category = "娱乐",
            videoUrl = "",
            imageUrl = "",
            publishTime="2",
            keywords = listOf(
                Keyword("二号", 1.0),
                Keyword("新闻", 0.8)),
            publisher = "微信"
        )
    )
    val newsResponse = NewsResponse(
        total = 2,
        pageSize = 15,
        data = newsList
    )

}