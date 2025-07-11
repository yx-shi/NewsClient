package com.example.newsclient.data.repository

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.Query
import com.example.newsclient.data.model.News
import com.example.newsclient.data.model.Keyword
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 数据库实体
@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val videoUrl: String,
    val imageUrl: String,
    val publishTime: String,
    val category: String,
    val publisher: String,
    val isFavorite: Boolean = false,
    val viewedAt: Long = System.currentTimeMillis()
)

// 将数据库实体转换为业务模型
fun NewsEntity.toNews() : News {
    return News(
        id = this.id,
        title = this.title,
        content = this.content,
        videoUrl = this.videoUrl,
        imageUrl = this.imageUrl,
        publishTime = this.publishTime,
        category = this.category,
        keywords = emptyList(), // 关键词可以在需要时添加
        publisher = this.publisher
    )
}

// 将业务模型转换为数据库实体
fun News.toEntity(isFavorite: Boolean = false, viewedAt: Long = System.currentTimeMillis()) : NewsEntity {
    return NewsEntity(
        id = this.id,
        title = this.title,
        content = this.content,
        videoUrl = this.videoUrl,
        imageUrl = this.imageUrl,
        publishTime = this.publishTime,
        category = this.category,
        publisher = this.publisher,
        isFavorite = isFavorite,
        viewedAt = viewedAt
    )
}

// DAO接口
@Dao
interface NewsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(news: NewsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(news: List<NewsEntity>)

    @Query("SELECT * FROM news WHERE category = :category ORDER BY viewedAt DESC")
    fun getNewsByCategory(category: String): Flow<List<NewsEntity>>

    @Query("SELECT * FROM news WHERE id = :newsId LIMIT 1")
    suspend fun getNewsById(newsId: String): NewsEntity?

    @Query("SELECT * FROM news WHERE title LIKE '%' || :keyword || '%' ORDER BY viewedAt DESC")
    fun searchNews(keyword: String?): Flow<List<NewsEntity>>

    @Query("SELECT * FROM news WHERE category = :category AND title LIKE '%' || :keyword || '%' ORDER BY viewedAt DESC")
    fun searchNewsByCategory(category: String, keyword: String?): Flow<List<NewsEntity>>

    @Query("SELECT * FROM news ORDER BY viewedAt DESC LIMIT 100")
    fun getAllHistoryNews(): Flow<List<NewsEntity>>

    @Query("SELECT * FROM news WHERE isFavorite = 1 ORDER BY viewedAt DESC")
    fun getFavoriteNews(): Flow<List<NewsEntity>>

    @Query("UPDATE news SET isFavorite = :isFavorite WHERE id = :newsId")
    suspend fun updateFavorite(newsId: String, isFavorite: Boolean)

    @Query("SELECT isFavorite FROM news WHERE id = :newsId LIMIT 1")
    suspend fun isNewsFavorite(newsId: String): Boolean?

    @Query("DELETE FROM news WHERE isFavorite = 0")
    suspend fun clearHistory()

    @Query("SELECT * FROM news WHERE category = :category OR :category IS NULL ORDER BY viewedAt DESC LIMIT 100")
    suspend fun getCachedNews(category: String?): List<NewsEntity>

    @Query("SELECT * FROM news WHERE (category = :category OR :category IS NULL) AND (title LIKE '%' || :keyword || '%' OR :keyword IS NULL) ORDER BY viewedAt DESC LIMIT 100")
    suspend fun getCachedNewsByKeyword(category: String?, keyword: String?): List<NewsEntity>
}

// 数据库定义 - 重命名为NewsRoomDatabase避免冲突
@Database(entities = [NewsEntity::class], version = 1, exportSchema = false)
abstract class NewsRoomDatabase : RoomDatabase() {
    abstract fun newsDao(): NewsDao

    companion object {
        @Volatile
        private var INSTANCE: NewsRoomDatabase? = null

        fun getDatabase(context: Context): NewsRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NewsRoomDatabase::class.java,
                    "news_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// 数据库操作封装类 - 重命名为NewsLocalDataSource避免冲突
class NewsLocalDataSource(private val newsDao: NewsDao) {
    // 缓存新闻到本地数据库
    suspend fun cacheNews(newsList: List<News>) {
        newsDao.insertAll(newsList.map { it.toEntity() })
    }

    // 保存新闻（作为历史或收藏）
    suspend fun saveNews(news: News, isFavorite: Boolean) {
        newsDao.insert(news.toEntity(isFavorite = isFavorite))
    }

    // 获取历史记录
    fun getHistoryNews(): Flow<List<News>> {
        return newsDao.getAllHistoryNews().map { entities ->
            entities.map { it.toNews() }
        }
    }

    // 获取收藏的新闻
    fun getFavoriteNews(): Flow<List<News>> {
        return newsDao.getFavoriteNews().map { entities ->
            entities.map { it.toNews() }
        }
    }

    // 根据ID获取新闻
    suspend fun getNewsById(newsId: String): News? {
        return newsDao.getNewsById(newsId)?.toNews()
    }

    // 检查新闻是否被收藏
    suspend fun isNewsFavorite(newsId: String): Boolean {
        return newsDao.isNewsFavorite(newsId) ?: false
    }

    // 切换收藏状态
    suspend fun toggleFavorite(newsId: String): Boolean {
        val currentState = isNewsFavorite(newsId)
        newsDao.updateFavorite(newsId, !currentState)
        return !currentState
    }

    // 清除历史记录（保留收藏）
    suspend fun clearHistory() {
        newsDao.clearHistory()
    }

    // 从缓存获取新闻
    suspend fun getCachedNews(category: String?, keyword: String?): List<News> {
        return if (keyword.isNullOrEmpty()) {
            newsDao.getCachedNews(category).map { it.toNews() }
        } else {
            newsDao.getCachedNewsByKeyword(category, keyword).map { it.toNews() }
        }
    }
}
