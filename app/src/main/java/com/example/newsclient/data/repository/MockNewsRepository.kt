package com.example.newsclient.data.repository
//
//import android.util.Log
//import com.example.newsclient.data.model.News
//import com.example.newsclient.data.model.NewsCategory
//import com.example.newsclient.data.model.Keyword
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.flowOf
//import kotlinx.coroutines.delay
//
///**
// * 模拟新闻数据仓库
// * 用于在API服务器问题解决前测试界面功能
// */
//class MockNewsRepository : NewsRepository {
//
//    private val mockNews = listOf(
//        News(
//            id = "1",
//            title = "科技巨头发布最新AI产品",
//            content = "今日，某科技公司发布了其最新的人工智能产品，该产品在多个领域展现出强大的能力...",
//            videoUrl = "https://example.com/video1.mp4",
//            imageUrl = "https://picsum.photos/400/300?random=1",
//            publishTime = "2024-07-11 10:30:00",
//            category = "科技",
//            keywords = listOf(Keyword("AI", 0.9), Keyword("科技", 0.8)),
//            publisher = "科技日报"
//        ),
//        News(
//            id = "2",
//            title = "体育赛事精彩回顾",
//            content = "昨晚的体育比赛异常精彩，双方球员都展现出了极高的竞技水平...",
//            videoUrl = "https://example.com/video2.mp4",
//            imageUrl = "https://picsum.photos/400/300?random=2",
//            publishTime = "2024-07-11 09:15:00",
//            category = "体育",
//            keywords = listOf(Keyword("体育", 0.9), Keyword("比赛", 0.7)),
//            publisher = "体育周刊"
//        ),
//        News(
//            id = "3",
//            title = "娱乐圈最新动态",
//            content = "知名演员今日宣布参演新电影，该电影预计将在明年上映...",
//            videoUrl = "https://example.com/video3.mp4",
//            imageUrl = "https://picsum.photos/400/300?random=3",
//            publishTime = "2024-07-11 08:45:00",
//            category = "娱乐",
//            keywords = listOf(Keyword("娱乐", 0.8), Keyword("电影", 0.9)),
//            publisher = "娱乐新闻"
//        ),
//        News(
//            id = "4",
//            title = "经济形势分析报告",
//            content = "专家分析当前经济形势，指出未来发展趋势和机遇...",
//            videoUrl = "https://example.com/video4.mp4",
//            imageUrl = "https://picsum.photos/400/300?random=4",
//            publishTime = "2024-07-11 07:20:00",
//            category = "财经",
//            keywords = listOf(Keyword("经济", 0.9), Keyword("分析", 0.7)),
//            publisher = "财经时报"
//        ),
//        News(
//            id = "5",
//            title = "健康生活新理念",
//            content = "营养专家分享健康饮食的重要性，建议大家养成良好的生活习惯...",
//            videoUrl = "https://example.com/video5.mp4",
//            imageUrl = "https://picsum.photos/400/300?random=5",
//            publishTime = "2024-07-11 06:30:00",
//            category = "健康",
//            keywords = listOf(Keyword("健康", 0.9), Keyword("饮食", 0.8)),
//            publisher = "健康生活"
//        ),
//        News(
//            id = "6",
//            title = "教育改革新举措",
//            content = "教育部门推出新的教育改革措施，旨在提升教学质量...",
//            videoUrl = "https://example.com/video6.mp4",
//            imageUrl = "https://picsum.photos/400/300?random=6",
//            publishTime = "2024-07-11 05:45:00",
//            category = "教育",
//            keywords = listOf(Keyword("教育", 0.9), Keyword("改革", 0.8)),
//            publisher = "教育新闻"
//        )
//    )
//
//    override suspend fun getNews(
//        category: NewsCategory?,
//        keyword: String?,
//        page: Int,
//        pageSize: Int
//    ):  List<News> {
//        Log.d("MockNewsRepository", "使用模拟数据获取新闻")
//
//        // 模拟网络延迟
//        delay(1000)
//
//        var filteredNews = mockNews
//
//        // 根据分类过滤
//        if (category != null) {
//            filteredNews = filteredNews.filter { it.category == category.value }
//        }
//
//        // 根据关键词过滤
//        if (!keyword.isNullOrEmpty()) {
//            filteredNews = filteredNews.filter {
//                it.title.contains(keyword, ignoreCase = true) ||
//                it.content.contains(keyword, ignoreCase = true)
//            }
//        }
//
//        // 分页处理
//        val startIndex = (page - 1) * pageSize
//        val endIndex = minOf(startIndex + pageSize, filteredNews.size)
//
//        return if (startIndex < filteredNews.size) {
//            filteredNews.subList(startIndex, endIndex)
//        } else {
//            emptyList()
//        }
//    }
//
//    override suspend fun getNewsFromLocal(newsId: String): News? {
//        return mockNews.find { it.id == newsId }
//    }
//
//    override suspend fun saveNewsToLocal(news: News, isFavorite: Boolean) {
//        Log.d("MockNewsRepository", "模拟保存新闻到本地: ${news.title}")
//    }
//
//    override fun getHistoryNews(): Flow<List<News>> {
//        return flowOf(mockNews.take(3))
//    }
//
//    override fun getFavoriteNews(): Flow<List<News>> {
//        return flowOf(mockNews.take(2))
//    }
//
//    override suspend fun isNewsFavorite(newsId: String): Boolean {
//        return newsId == "1" || newsId == "2" // 模拟前两条新闻被收藏
//    }
//
//    override suspend fun toggleFavorite(newsId: String): Boolean {
//        Log.d("MockNewsRepository", "模拟切换收藏状态: $newsId")
//        return true
//    }
//
//    override suspend fun clearHistory() {
//        Log.d("MockNewsRepository", "模拟清除历史记录")
//    }
//}
