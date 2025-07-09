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
    val isBookmarked: Boolean = false,
    val viewedAt: Long = System.currentTimeMillis()
)

fun NewsEntity.toNews() : News {
    return News(
        id = this.id,
        title = this.title,
        content = this.content,
        videoUrl = this.videoUrl,
        imageUrl = this.imageUrl,
        publishTime = this.publishTime,
        category = this.category,
        keywords = emptyList() // 这里可以根据需要添加关键词
    )
}

// DAO接口
@Dao
interface NewsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(news: NewsEntity)

    @Query("SELECT * FROM news WHERE category = :category ORDER BY viewedAt DESC")
    suspend fun getNewsByCategory(category: String): List<NewsEntity>

    @Query("SELECT * FROM news WHERE isBookmarked = 1 ORDER BY viewedAt DESC")
    suspend fun getBookmarkedNews(): List<NewsEntity>

    @Query("SELECT * FROM news ORDER BY viewedAt DESC LIMIT :pageSize OFFSET :offset")
    suspend fun getNewsByPage(
        pageSize: Int = 10,
        offset: Int = 0
    ): List<NewsEntity>

    @Update
    suspend fun updateBookmarkStatus(news: NewsEntity)
}

// 数据库类
@Database(entities = [NewsEntity::class], version = 1)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun newsDao(): NewsDao

    companion object {
        @Volatile private var instance: NewsDatabase? = null

        fun getInstance(context: Context): NewsDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NewsDatabase::class.java, "news-db"
                ).build().also { instance = it }
            }
        }
    }
}