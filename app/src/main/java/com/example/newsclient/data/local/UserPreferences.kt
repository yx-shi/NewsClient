package com.example.newsclient.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.newsclient.data.model.NewsCategory
import com.example.newsclient.data.model.News
import com.example.newsclient.data.model.NewsSummary
import com.example.newsclient.data.model.UserSettings
import com.example.newsclient.data.model.Gender
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 历史记录数据类
 */
data class NewsHistory(
    val news: News,
    val readTime: Long = System.currentTimeMillis()
)

/**
 * 收藏新闻数据类
 */
data class NewsFavorite(
    val news: News,
    val favoriteTime: Long = System.currentTimeMillis()
)

/**
 * 用户偏好设置管理类
 * 负责保存和读取用户的分类偏好设置、历史记录、收藏和摘要
 */
class UserPreferences(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "news_client_preferences",
        Context.MODE_PRIVATE
    )

    private val gson = Gson()

    // 用于响应式更新的StateFlow
    private val _userCategories = MutableStateFlow<List<NewsCategory>>(emptyList())
    private val _historyNews = MutableStateFlow<List<NewsHistory>>(emptyList())
    private val _favoriteNews = MutableStateFlow<List<NewsFavorite>>(emptyList())
    private val _newsSummaries = MutableStateFlow<List<NewsSummary>>(emptyList())
    private val _userSettings = MutableStateFlow<UserSettings>(UserSettings())

    companion object {
        private const val KEY_SELECTED_CATEGORIES = "selected_categories"
        private const val KEY_CATEGORY_ORDER = "category_order"
        private const val KEY_HISTORY_NEWS = "history_news"
        private const val KEY_READ_NEWS_IDS = "read_news_ids"
        private const val KEY_FAVORITE_NEWS = "favorite_news"
        private const val KEY_FAVORITE_NEWS_IDS = "favorite_news_ids"
        private const val KEY_NEWS_SUMMARIES = "news_summaries"
        private const val MAX_HISTORY_SIZE = 500 // 最大历史记录数量
        private const val MAX_FAVORITE_SIZE = 1000 // 最大收藏数量
        private const val MAX_SUMMARY_SIZE = 1000 // 最大摘要缓存数量

        // 用户设置相关的键
        private const val KEY_USER_SETTINGS = "user_settings"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_GENDER = "gender"
        private const val KEY_BIRTHDAY = "birthday"
        private const val KEY_SIGNATURE = "signature"
        private const val KEY_IS_DARK_THEME = "is_dark_theme"
        private const val KEY_FONT_SIZE_SCALE = "font_size_scale"
    }

    init {
        // 初始化时加载用户分类、历史记录、收藏和摘要
        _userCategories.value = getSelectedCategories()
        _historyNews.value = getHistoryNews()
        _favoriteNews.value = getFavoriteNews()
        _newsSummaries.value = getNewsSummaries()
        _userSettings.value = getUserSettings()

        // 添加调试日志以确认初始化状态
        android.util.Log.d("UserPreferences", "初始化完成 - 收藏数量: ${_favoriteNews.value.size}, 摘要数量: ${_newsSummaries.value.size}")
    }

    /**
     * 获取用户选择的分类列表
     * 如果没有保存过偏好，返回默认的前5个分类
     */
    fun getSelectedCategories(): List<NewsCategory> {
        val savedJson = sharedPreferences.getString(KEY_SELECTED_CATEGORIES, null)
        return if (savedJson != null) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                val categoryValues: List<String> = gson.fromJson(savedJson, type)
                categoryValues.mapNotNull { value ->
                    NewsCategory.entries.find { it.value == value }
                }
            } catch (e: Exception) {
                getDefaultCategories()
            }
        } else {
            getDefaultCategories()
        }
    }

    /**
     * 获取用户分类的响应式Flow
     */
    fun getUserCategories(): Flow<List<NewsCategory>> {
        return _userCategories.asStateFlow()
    }

    /**
     * 保存用户选择的分类列表
     */
    fun saveSelectedCategories(categories: List<NewsCategory>) {
        val categoryValues = categories.map { it.value }
        val json = gson.toJson(categoryValues)
        sharedPreferences.edit()
            .putString(KEY_SELECTED_CATEGORIES, json)
            .apply()

        // 更新响应式状态
        _userCategories.value = categories
    }

    /**
     * 获取分类显示顺序
     */
    fun getCategoryOrder(): List<NewsCategory> {
        val savedJson = sharedPreferences.getString(KEY_CATEGORY_ORDER, null)
        return if (savedJson != null) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                val categoryValues: List<String> = gson.fromJson(savedJson, type)
                categoryValues.mapNotNull { value ->
                    NewsCategory.entries.find { it.value == value }
                }
            } catch (e: Exception) {
                getSelectedCategories()
            }
        } else {
            getSelectedCategories()
        }
    }

    /**
     * 保存分类显示顺序
     */
    fun saveCategoryOrder(categories: List<NewsCategory>) {
        val categoryValues = categories.map { it.value }
        val json = gson.toJson(categoryValues)
        sharedPreferences.edit()
            .putString(KEY_CATEGORY_ORDER, json)
            .apply()
    }

    /**
     * 添加一个分类到用户偏好
     */
    fun addCategory(category: NewsCategory) {
        val currentCategories = getSelectedCategories().toMutableList()
        if (!currentCategories.contains(category)) {
            currentCategories.add(category)
            saveSelectedCategories(currentCategories)
        }
    }

    /**
     * 从用户偏好中移除一个分类
     */
    fun removeCategory(category: NewsCategory) {
        val currentCategories = getSelectedCategories().toMutableList()
        if (currentCategories.contains(category) && currentCategories.size > 1) {
            currentCategories.remove(category)
            saveSelectedCategories(currentCategories)
        }
    }

    /**
     * 检查分类是否已被用户选择
     */
    fun isCategorySelected(category: NewsCategory): Boolean {
        return getSelectedCategories().contains(category)
    }

    /**
     * 获取可添加的分类列表（未被用户选择的分类）
     */
    fun getAvailableCategories(): List<NewsCategory> {
        val selectedCategories = getSelectedCategories()
        return NewsCategory.entries.filter { !selectedCategories.contains(it) }
    }

    /**
     * 重置为默认分类
     */
    fun resetToDefault() {
        saveSelectedCategories(getDefaultCategories())
    }

    /**
     * 获取默认分类（前5个）
     */
    private fun getDefaultCategories(): List<NewsCategory> {
        return listOf(
            NewsCategory.ENTERTAINMENT,
            NewsCategory.TECHNOLOGY,
            NewsCategory.SPORTS,
            NewsCategory.FINANCE,
            NewsCategory.SOCIETY
        )
    }

    /**
     * 添加新闻到历史记录
     */
    fun addToHistory(news: News) {
        val currentHistory = _historyNews.value.toMutableList()

        // 移除已存在的相同新闻（如果有）
        currentHistory.removeAll { it.news.id == news.id }

        // 添加到列表开头
        currentHistory.add(0, NewsHistory(news))

        // 限制历史记录数量
        if (currentHistory.size > MAX_HISTORY_SIZE) {
            currentHistory.removeAt(currentHistory.size - 1)
        }

        // 保存到SharedPreferences
        val json = gson.toJson(currentHistory)
        sharedPreferences.edit()
            .putString(KEY_HISTORY_NEWS, json)
            .apply()

        // 更新StateFlow
        _historyNews.value = currentHistory

        // 同时更新已读新闻ID集合
        addReadNewsId(news.id)
    }

    /**
     * 获取历史记录
     */
    fun getHistoryNews(): List<NewsHistory> {
        val savedJson = sharedPreferences.getString(KEY_HISTORY_NEWS, null)
        return if (savedJson != null) {
            try {
                val type = object : TypeToken<List<NewsHistory>>() {}.type
                val parsedResult: List<NewsHistory>? = gson.fromJson(savedJson, type)
                parsedResult ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * 获取历史记录的Flow
     */
    fun getHistoryNewsFlow(): Flow<List<NewsHistory>> = _historyNews.asStateFlow()

    /**
     * 清空历史记录
     */
    fun clearHistory() {
        sharedPreferences.edit()
            .remove(KEY_HISTORY_NEWS)
            .remove(KEY_READ_NEWS_IDS)
            .apply()
        _historyNews.value = emptyList()
    }

    /**
     * 删除单条历史记录
     */
    fun removeFromHistory(newsId: String) {
        val currentHistory = _historyNews.value.toMutableList()
        currentHistory.removeAll { it.news.id == newsId }

        val json = gson.toJson(currentHistory)
        sharedPreferences.edit()
            .putString(KEY_HISTORY_NEWS, json)
            .apply()

        _historyNews.value = currentHistory

        // 同时从已读集合中移除
        removeReadNewsId(newsId)
    }

    /**
     * 添加新闻到收藏
     */
    fun addToFavorites(news: News) {
        val currentFavorites = _favoriteNews.value.toMutableList()

        // 移除已存在的相同新闻（如果有）
        currentFavorites.removeAll { it.news.id == news.id }

        // 添加到列表开头
        currentFavorites.add(0, NewsFavorite(news))

        // 限制收藏数量
        if (currentFavorites.size > MAX_FAVORITE_SIZE) {
            currentFavorites.removeAt(currentFavorites.size - 1)
        }

        // 保存到SharedPreferences
        val json = gson.toJson(currentFavorites)
        sharedPreferences.edit()
            .putString(KEY_FAVORITE_NEWS, json)
            .apply()

        // 立即更新StateFlow - 这是关键修复
        _favoriteNews.value = currentFavorites

        // 添加调试日志
        android.util.Log.d("UserPreferences", "添加收藏: ${news.title}")
        android.util.Log.d("UserPreferences", "当前收藏数量: ${currentFavorites.size}")
        android.util.Log.d("UserPreferences", "StateFlow已更新到: ${_favoriteNews.value.size} 条")
    }

    /**
     * 获取收藏的新闻
     */
    fun getFavoriteNews(): List<NewsFavorite> {
        val savedJson = sharedPreferences.getString(KEY_FAVORITE_NEWS, null)
        val result = if (savedJson != null) {
            try {
                val type = object : TypeToken<List<NewsFavorite>>() {}.type
                val parsedResult: List<NewsFavorite>? = gson.fromJson(savedJson, type)
                parsedResult ?: emptyList()
            } catch (e: Exception) {
                android.util.Log.e("UserPreferences", "解析收藏数据失败", e)
                emptyList()
            }
        } else {
            emptyList()
        }

        // 添加调试日志
        android.util.Log.d("UserPreferences", "获取收藏数据: ${result.size} 条")

        // 关键修复：确保StateFlow与实际数据同步
        if (_favoriteNews.value.size != result.size) {
            android.util.Log.d("UserPreferences", "检测到StateFlow不同步，从 ${_favoriteNews.value.size} 更新到 ${result.size}")
            _favoriteNews.value = result
        }

        return result
    }

    /**
     * 获取收藏新闻的Flow
     */
    fun getFavoriteNewsFlow(): Flow<List<NewsFavorite>> = _favoriteNews.asStateFlow()

    /**
     * 清空收藏
     */
    fun clearFavorites() {
        sharedPreferences.edit()
            .remove(KEY_FAVORITE_NEWS)
            .remove(KEY_FAVORITE_NEWS_IDS)
            .apply()
        _favoriteNews.value = emptyList()

        // 添加调试日志
        android.util.Log.d("UserPreferences", "清空收藏")
    }

    /**
     * 删除单条收藏
     */
    fun removeFromFavorites(newsId: String) {
        val currentFavorites = _favoriteNews.value.toMutableList()
        currentFavorites.removeAll { it.news.id == newsId }

        val json = gson.toJson(currentFavorites)
        sharedPreferences.edit()
            .putString(KEY_FAVORITE_NEWS, json)
            .apply()

        _favoriteNews.value = currentFavorites

        // 添加调试日志
        android.util.Log.d("UserPreferences", "删除收藏: $newsId, 剩余收藏数量: ${currentFavorites.size}")
    }

    /**
     * 添加已读新闻ID
     */
    private fun addReadNewsId(newsId: String) {
        val readIds = getReadNewsIds().toMutableSet()
        readIds.add(newsId)

        val json = gson.toJson(readIds.toList())
        sharedPreferences.edit()
            .putString(KEY_READ_NEWS_IDS, json)
            .apply()
    }

    /**
     * 移除已读新闻ID
     */
    private fun removeReadNewsId(newsId: String) {
        val readIds = getReadNewsIds().toMutableSet()
        readIds.remove(newsId)

        val json = gson.toJson(readIds.toList())
        sharedPreferences.edit()
            .putString(KEY_READ_NEWS_IDS, json)
            .apply()
    }

    /**
     * 获取已读新闻ID集合
     */
    fun getReadNewsIds(): Set<String> {
        val savedJson = sharedPreferences.getString(KEY_READ_NEWS_IDS, null)
        return if (savedJson != null) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                val ids: List<String> = gson.fromJson(savedJson, type) ?: emptyList()
                ids.toSet()
            } catch (e: Exception) {
                emptySet()
            }
        } else {
            emptySet()
        }
    }

    /**
     * 检查新闻是否已读
     */
    fun isNewsRead(newsId: String): Boolean {
        return getReadNewsIds().contains(newsId)
    }

    /**
     * 强制刷新收藏数据的StateFlow
     * 用于确保多个实例之间的数据同步
     */
    fun refreshFavoriteFlow() {
        val currentData = getFavoriteNews()
        _favoriteNews.value = currentData
        android.util.Log.d("UserPreferences", "强制刷新收藏Flow: ${currentData.size} 条")
    }

    /**
     * 获取新闻摘要
     */
    fun getNewsSummaries(): List<NewsSummary> {
        val savedJson = sharedPreferences.getString(KEY_NEWS_SUMMARIES, null)
        return if (savedJson != null) {
            try {
                val type = object : TypeToken<List<NewsSummary>>() {}.type
                val parsedResult: List<NewsSummary>? = gson.fromJson(savedJson, type)
                parsedResult ?: emptyList()
            } catch (e: Exception) {
                android.util.Log.e("UserPreferences", "解析摘要数据失败", e)
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * 保存新闻摘要
     */
    fun saveNewsSummaries(summaries: List<NewsSummary>) {
        val summariesToSave = summaries.take(MAX_SUMMARY_SIZE) // 限制保存数量
        val json = gson.toJson(summariesToSave)
        sharedPreferences.edit()
            .putString(KEY_NEWS_SUMMARIES, json)
            .apply()

        // 更新StateFlow
        _newsSummaries.value = summariesToSave
    }

    /**
     * 添加新闻摘要
     */
    fun addNewsSummary(newsId: String, summary: String, apiKey: String) {
        val currentSummaries = _newsSummaries.value.toMutableList()

        // 移除已存在的相同新闻摘要（如果有）
        currentSummaries.removeAll { it.newsId == newsId }

        // 添加新摘要到列表开头
        currentSummaries.add(0, NewsSummary(newsId, summary, System.currentTimeMillis(), apiKey))

        // 限制摘要数量
        if (currentSummaries.size > MAX_SUMMARY_SIZE) {
            currentSummaries.removeAt(currentSummaries.size - 1)
        }

        // 保存到SharedPreferences
        val json = gson.toJson(currentSummaries)
        sharedPreferences.edit()
            .putString(KEY_NEWS_SUMMARIES, json)
            .apply()

        // 更新StateFlow
        _newsSummaries.value = currentSummaries

        android.util.Log.d("UserPreferences", "添加摘要: $newsId, 当前摘要数量: ${currentSummaries.size}")
    }

    /**
     * 根据新闻ID获取摘要
     */
    fun getNewsSummaryById(newsId: String): NewsSummary? {
        return _newsSummaries.value.find { it.newsId == newsId }
    }

    /**
     * 检查新闻是否已有摘要
     */
    fun hasNewsSummary(newsId: String): Boolean {
        return _newsSummaries.value.any { it.newsId == newsId }
    }

    /**
     * 删除指定新闻的摘要
     */
    fun removeNewsSummary(newsId: String) {
        val currentSummaries = _newsSummaries.value.toMutableList()
        currentSummaries.removeAll { it.newsId == newsId }

        val json = gson.toJson(currentSummaries)
        sharedPreferences.edit()
            .putString(KEY_NEWS_SUMMARIES, json)
            .apply()

        _newsSummaries.value = currentSummaries

        android.util.Log.d("UserPreferences", "删除摘要: $newsId, 剩余摘要数量: ${currentSummaries.size}")
    }

    /**
     * 获取摘要Flow（用于响应式更新）
     */
    fun getNewsSummariesFlow(): Flow<List<NewsSummary>> = _newsSummaries.asStateFlow()

    /**
     * 清理过期的摘要（可选功能，比如清理30天前的摘要）
     */
    fun cleanExpiredSummaries(daysToKeep: Int = 30) {
        val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        val currentSummaries = _newsSummaries.value.toMutableList()
        val filteredSummaries = currentSummaries.filter { it.generatedAt > cutoffTime }

        if (filteredSummaries.size != currentSummaries.size) {
            val json = gson.toJson(filteredSummaries)
            sharedPreferences.edit()
                .putString(KEY_NEWS_SUMMARIES, json)
                .apply()

            _newsSummaries.value = filteredSummaries

            android.util.Log.d("UserPreferences", "清理过期摘要: 删除 ${currentSummaries.size - filteredSummaries.size} 条")
        }
    }

    /**
     * 获取用户个性化设置
     */
    fun getUserSettings(): UserSettings {
        val savedJson = sharedPreferences.getString(KEY_USER_SETTINGS, null)
        return if (savedJson != null) {
            try {
                gson.fromJson(savedJson, UserSettings::class.java)
            } catch (e: Exception) {
                android.util.Log.e("UserPreferences", "读取用户设置失败", e)
                UserSettings()
            }
        } else {
            // 如果没有保存的JSON，从旧的分离键值中迁移数据
            UserSettings(
                nickname = sharedPreferences.getString(KEY_NICKNAME, "新闻用户007") ?: "新闻用户007",
                gender = Gender.valueOf(sharedPreferences.getString(KEY_GENDER, "UNSPECIFIED") ?: "UNSPECIFIED"),
                birthday = sharedPreferences.getString(KEY_BIRTHDAY, "") ?: "",
                signature = sharedPreferences.getString(KEY_SIGNATURE, "") ?: "",
                isDarkTheme = sharedPreferences.getBoolean(KEY_IS_DARK_THEME, false),
                fontSizeScale = sharedPreferences.getFloat(KEY_FONT_SIZE_SCALE, 1.0f)
            )
        }
    }

    /**
     * 获取用户设置的响应式Flow
     */
    fun getUserSettingsFlow(): Flow<UserSettings> {
        return _userSettings.asStateFlow()
    }

    /**
     * 保存用户个性化设置
     */
    fun saveUserSettings(settings: UserSettings) {
        val json = gson.toJson(settings)
        sharedPreferences.edit()
            .putString(KEY_USER_SETTINGS, json)
            .apply()

        // 更新响应式状态
        _userSettings.value = settings

        android.util.Log.d("UserPreferences", "用户设置已保存: ${settings.nickname}, 主题: ${settings.isDarkTheme}")
    }

    /**
     * 更新用户昵称
     */
    fun updateNickname(nickname: String) {
        val currentSettings = getUserSettings()
        saveUserSettings(currentSettings.copy(nickname = nickname))
    }

    /**
     * 更新用户性别
     */
    fun updateGender(gender: Gender) {
        val currentSettings = getUserSettings()
        saveUserSettings(currentSettings.copy(gender = gender))
    }

    /**
     * 更新用户生日
     */
    fun updateBirthday(birthday: String) {
        val currentSettings = getUserSettings()
        saveUserSettings(currentSettings.copy(birthday = birthday))
    }

    /**
     * 更新用户签名
     */
    fun updateSignature(signature: String) {
        val currentSettings = getUserSettings()
        saveUserSettings(currentSettings.copy(signature = signature))
    }

    /**
     * 更新主题模式
     */
    fun updateTheme(isDarkTheme: Boolean) {
        val currentSettings = getUserSettings()
        saveUserSettings(currentSettings.copy(isDarkTheme = isDarkTheme))
    }

    /**
     * 更新字体大小
     */
    fun updateFontSize(scale: Float) {
        val currentSettings = getUserSettings()
        saveUserSettings(currentSettings.copy(fontSizeScale = scale))
    }
}
