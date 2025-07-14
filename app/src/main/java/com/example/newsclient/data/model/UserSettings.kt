package com.example.newsclient.data.model

import androidx.compose.runtime.Immutable

/**
 * 用户个性化设置数据模型
 */
@Immutable
data class UserSettings(
    val nickname: String = "新闻用户007",
    val gender: Gender = Gender.UNSPECIFIED,
    val birthday: String = "", // 格式：YYYY-MM-DD
    val signature: String = "",
    val isDarkTheme: Boolean = false,
    val fontSizeScale: Float = 1.0f // 字体缩放比例，1.0为默认大小
)

/**
 * 性别枚举
 */
enum class Gender(val displayName: String) {
    MALE("男"),
    FEMALE("女"),
    UNSPECIFIED("未设置")
}

/**
 * 字体大小级别
 */
enum class FontSizeLevel(val displayName: String, val scale: Float) {
    VERY_SMALL("极小", 0.8f),
    SMALL("小", 0.9f),
    NORMAL("标准", 1.0f),
    LARGE("大", 1.1f),
    VERY_LARGE("极大", 1.2f),
    EXTRA_LARGE("超大", 1.3f);

    companion object {
        fun fromScale(scale: Float): FontSizeLevel {
            return values().minByOrNull { kotlin.math.abs(it.scale - scale) } ?: NORMAL
        }
    }
}
