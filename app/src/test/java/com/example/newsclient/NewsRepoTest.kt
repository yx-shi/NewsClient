package com.example.newsclient

import com.example.newsclient.data.repository.NetworkNewsRepository
import com.example.newsclient.fake.FakeDataSource
import com.example.newsclient.fake.FakeNewsApiService
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 测试网络新闻仓库
 * 测试通过，说明repo成功把API接口转化为了自己的repo.getNews()接口，返回List<News>类型数据
 */
class NewsRepoTest {
    @Test
    fun networkNewsRepository_getNews_verify()=
        runTest {
            val repo=NetworkNewsRepository(
                newsApiService = FakeNewsApiService()
            )
            assertEquals(FakeDataSource.newsResponse.data,repo.getNews())
        }
}