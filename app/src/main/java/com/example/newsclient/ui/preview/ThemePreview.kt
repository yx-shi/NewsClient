package com.example.newsclient.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.newsclient.data.model.News
import com.example.newsclient.data.model.NewsCategory
import com.example.newsclient.data.model.Keyword
import com.example.newsclient.ui.theme.NewsClientTheme

// 硬编码的主题颜色 - 对应你的Color.kt中定义的颜色
object ThemeColors {
    val primaryLight = Color(0xFF1F6A4E)
    val onPrimaryLight = Color(0xFFFFFFFF)
    val primaryContainerLight = Color(0xFFA8F2CE)
    val onPrimaryContainerLight = Color(0xFF005138)
    val secondaryLight = Color(0xFF4D6357)
    val onSecondaryLight = Color(0xFFFFFFFF)
    val secondaryContainerLight = Color(0xFFCFE9D9)
    val onSecondaryContainerLight = Color(0xFF354B40)
    val backgroundLight = Color(0xFFF5FBF5)
    val onBackgroundLight = Color(0xFF171D1A)
    val surfaceLight = Color(0xFFF5FBF5)
    val onSurfaceLight = Color(0xFF171D1A)
    val surfaceVariantLight = Color(0xFFDBE5DD)
    val onSurfaceVariantLight = Color(0xFF404943)
    val outlineLight = Color(0xFF707973)
}

/**
 * 完整的新闻列表界面预览 - 使用硬编码颜色
 */
@Composable
@Preview(showBackground = true, name = "新闻列表预览（硬编码颜色）")
fun NewsListScreenPreviewHardcoded() {
    // 创建示例数据 - 修复字段类型和缺失参数
    val sampleNews = News(
        id = "1", // 修改为String类型
        title = "示例新闻标题：这是一个很长的新闻标题，用来测试文本换行和字体效果",
        content = "这是新闻正文内容，用宋体显示。这段文字用来测试正文字体的显示效果和可读性。包含一些中文内容来验证字体渲染是否正确。",
        publisher = "示例发布者",
        publishTime = "2024-01-15 10:30",
        imageUrl = "",
        videoUrl = "", // 添加缺失的videoUrl参数
        category = "科技", // 添加缺失的category参数
        keywords = listOf( // 添加缺失的keywords参数
            Keyword("示例", 0.8),
            Keyword("新闻", 0.9)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeColors.backgroundLight) // 硬编码背景色
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题展示
        Text(
            text = "主题配色预览（硬编码）",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ThemeColors.onBackgroundLight
        )

        // 搜索栏预览 - 硬编码颜色
        SearchBarPreviewHardcoded()

        // 分类标签预览 - 硬编码颜色
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChipPreviewHardcoded(
                text = "全部",
                isSelected = true
            )
            CategoryChipPreviewHardcoded(
                text = "科技",
                isSelected = false
            )
            CategoryManageButtonPreviewHardcoded()
        }

        // 新闻条目预览 - 硬编码颜色
        NewsItemPreviewHardcoded(
            news = sampleNews,
            isRead = false
        )

        // 已读新闻条目预览 - 硬编码颜色
        NewsItemPreviewHardcoded(
            news = sampleNews.copy(title = "已读新闻标题示例"),
            isRead = true
        )

        // 颜色面板预览
        ColorPalettePreviewHardcoded()
    }
}

/**
 * 使用真实主题的预览
 */
@Composable
@Preview(showBackground = true, name = "新闻列表预览（真实主题）")
fun NewsListScreenPreviewWithTheme() {
    NewsClientTheme {
        // 创建示例数据 - 修复字段类型和缺失参数
        val sampleNews = News(
            id = "2", // 修改为String类型
            title = "示例新闻标题：这是一个很长的新闻标题，用来测试文本换行和字体效果",
            content = "这是新闻正文内容，用宋体显示。这段文字用来测试正文字体的显示效果和可读性。包含一些中文内容来验证字体渲染是否正确。",
            publisher = "示例发布者",
            publishTime = "2024-01-15 10:30",
            imageUrl = "",
            videoUrl = "", // 添加缺失的videoUrl参数
            category = "科技", // 添加缺失的category参数
            keywords = listOf( // 添加缺失的keywords参数
                Keyword("示例", 0.8),
                Keyword("新闻", 0.9)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题展示
            Text(
                text = "主题配色预览（真实主题）",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            // 搜索栏预览
            SearchBarPreviewWithTheme()

            // 分类标签预览
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryChipPreviewWithTheme(
                    text = "全部",
                    isSelected = true
                )
                CategoryChipPreviewWithTheme(
                    text = "科技",
                    isSelected = false
                )
                CategoryManageButtonPreviewWithTheme()
            }

            // 新闻条目预览
            NewsItemPreviewWithTheme(
                news = sampleNews,
                isRead = false
            )

            // 已读新闻条目预览
            NewsItemPreviewWithTheme(
                news = sampleNews.copy(title = "已读新闻标题示例"),
                isRead = true
            )

            // 颜色面板预览
            ColorPalettePreviewWithTheme()
        }
    }
}

/**
 * 搜索栏预览 - 硬编码颜色
 */
@Composable
fun SearchBarPreviewHardcoded() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeColors.surfaceLight),
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "搜索",
                tint = ThemeColors.onSurfaceVariantLight,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "搜索新闻、关键词...",
                fontSize = 16.sp,
                color = ThemeColors.onSurfaceVariantLight,
                modifier = Modifier.weight(1f)
            )

            // 添加搜索快捷提示
            Surface(
                color = ThemeColors.primaryContainerLight.copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "搜索",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeColors.onPrimaryContainerLight,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * 搜索栏预览 - 使用真实主题
 */
@Composable
fun SearchBarPreviewWithTheme() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "搜索",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "搜索新闻、关键词...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "搜索",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * 分类标签预览 - 硬编码颜色
 */
@Composable
fun CategoryChipPreviewHardcoded(
    text: String,
    isSelected: Boolean
) {
    Card(
        modifier = Modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                ThemeColors.primaryLight
            else
                ThemeColors.surfaceLight
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(ThemeColors.onPrimaryLight, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = text,
                color = if (isSelected)
                    ThemeColors.onPrimaryLight
                else
                    ThemeColors.onSurfaceLight,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

/**
 * 分类标签预览 - 使用真实主题
 */
@Composable
fun CategoryChipPreviewWithTheme(
    text: String,
    isSelected: Boolean
) {
    Card(
        modifier = Modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.onPrimary, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = text,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

/**
 * 分类管理按钮预览 - 硬编码颜色
 */
@Composable
fun CategoryManageButtonPreviewHardcoded() {
    Card(
        modifier = Modifier,
        colors = CardDefaults.cardColors(
            containerColor = ThemeColors.secondaryContainerLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "管理分类",
                tint = ThemeColors.onSecondaryContainerLight,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "管理",
                color = ThemeColors.onSecondaryContainerLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 分类管理按钮预览 - 使用真实主题
 */
@Composable
fun CategoryManageButtonPreviewWithTheme() {
    Card(
        modifier = Modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "管理分类",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "管理",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 新闻条目预览 - 硬编码颜色
 */
@Composable
fun NewsItemPreviewHardcoded(
    news: News,
    isRead: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ThemeColors.surfaceLight // 统一使用普通背景色
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 新闻标题 - 已读时变为灰色
            Text(
                text = news.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold, // 黑体效果
                color = if (isRead)
                    ThemeColors.onSurfaceVariantLight.copy(alpha = 0.7f) // 已读：灰色
                else
                    ThemeColors.onSurfaceLight, // 未读：正常颜色
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 新闻摘要 - 硬编码雅黑效果
            Text(
                text = news.content,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal, // 雅黑效果
                color = if (isRead)
                    ThemeColors.onSurfaceVariantLight.copy(alpha = 0.6f) // 已读：稍微变淡
                else
                    ThemeColors.onSurfaceVariantLight, // 未读：正常颜色
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 22.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 新闻元信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 发布者信息
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        color = if (isRead)
                            ThemeColors.primaryContainerLight.copy(alpha = 0.5f) // 已读：淡化
                        else
                            ThemeColors.primaryContainerLight.copy(alpha = 0.8f), // 未读：正常
                        shape = CircleShape
                    ) {
                        Text(
                            text = news.publisher.take(1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = ThemeColors.onPrimaryContainerLight,
                            modifier = Modifier.padding(6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = news.publisher,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isRead)
                            ThemeColors.onSurfaceVariantLight.copy(alpha = 0.5f) // 已读：变淡
                        else
                            ThemeColors.onSurfaceVariantLight, // 未读：正常
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 时间和已读状态
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 简洁的已读标签
                    if (isRead) {
                        Text(
                            text = "已读",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = ThemeColors.onSurfaceVariantLight.copy(alpha = 0.6f),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                    Text(
                        text = news.publishTime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isRead)
                            ThemeColors.onSurfaceVariantLight.copy(alpha = 0.5f) // 已读：变淡
                        else
                            ThemeColors.onSurfaceVariantLight.copy(alpha = 0.7f) // 未读：正常
                    )
                }
            }
        }
    }
}

/**
 * 新闻条目预览 - 使用真实主题
 */
@Composable
fun NewsItemPreviewWithTheme(
    news: News,
    isRead: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isRead)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 新闻标题 - 使用真实主题
            Text(
                text = news.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isRead)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                else
                    MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 新闻摘要 - 使用真实主题
            Text(
                text = news.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isRead)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 22.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 新闻元信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = news.publisher.take(1),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = news.publisher,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isRead)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRead) {
                        Surface(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "已读",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = news.publishTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * 颜色面板预览 - 硬编码颜色
 */
@Composable
fun ColorPalettePreviewHardcoded() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ThemeColors.surfaceVariantLight
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "主题颜色面板（硬编码）",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = ThemeColors.onSurfaceVariantLight
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ColorBlockHardcoded(
                        color = ThemeColors.primaryLight,
                        name = "Primary"
                    )
                }
                item {
                    ColorBlockHardcoded(
                        color = ThemeColors.primaryContainerLight,
                        name = "Primary Container"
                    )
                }
                item {
                    ColorBlockHardcoded(
                        color = ThemeColors.secondaryLight,
                        name = "Secondary"
                    )
                }
                item {
                    ColorBlockHardcoded(
                        color = ThemeColors.secondaryContainerLight,
                        name = "Secondary Container"
                    )
                }
                item {
                    ColorBlockHardcoded(
                        color = ThemeColors.surfaceLight,
                        name = "Surface"
                    )
                }
                item {
                    ColorBlockHardcoded(
                        color = ThemeColors.backgroundLight,
                        name = "Background"
                    )
                }
            }
        }
    }
}

/**
 * 颜色面板预览 - 使用真实主题
 */
@Composable
fun ColorPalettePreviewWithTheme() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "主题颜色面板（真实主题）",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ColorBlockWithTheme(
                        color = MaterialTheme.colorScheme.primary,
                        name = "Primary"
                    )
                }
                item {
                    ColorBlockWithTheme(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        name = "Primary Container"
                    )
                }
                item {
                    ColorBlockWithTheme(
                        color = MaterialTheme.colorScheme.secondary,
                        name = "Secondary"
                    )
                }
                item {
                    ColorBlockWithTheme(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        name = "Secondary Container"
                    )
                }
                item {
                    ColorBlockWithTheme(
                        color = MaterialTheme.colorScheme.surface,
                        name = "Surface"
                    )
                }
                item {
                    ColorBlockWithTheme(
                        color = MaterialTheme.colorScheme.background,
                        name = "Background"
                    )
                }
            }
        }
    }
}

/**
 * 颜色块预览 - 硬编码颜色
 */
@Composable
fun ColorBlockHardcoded(color: Color, name: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = ThemeColors.onSurfaceVariantLight,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 颜色块预览 - 使用真实主题
 */
@Composable
fun ColorBlockWithTheme(color: Color, name: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 主题对比预览
 */
@Composable
@Preview(showBackground = true, name = "主题对比预览")
fun ThemeComparisonPreview() {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 硬编码颜色预览
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ThemeColors.backgroundLight
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "硬编码主题",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = ThemeColors.onBackgroundLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = ThemeColors.primaryLight
                        )
                    ) {
                        Text(
                            text = "主色调",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = ThemeColors.onPrimaryLight,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        // 真实主题预览
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            NewsClientTheme {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "真实主题",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "主色调",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 主题调试预览 - 用于验证动态颜色修复效果
 */
@Composable
@Preview(showBackground = true, name = "主题调试预览")
fun ThemeDebugPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "主题修复验证",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        // 修复后的主题（动态颜色关闭）
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "修复后 - NewsClientTheme (dynamicColor = false)",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(8.dp))

                NewsClientTheme(dynamicColor = false) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        ) {}
                        Column {
                            Text(
                                text = "Primary: ${MaterialTheme.colorScheme.primary}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "应该显示: #1F6A4E (深绿色)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // 修复前的主题（动态颜色开启）
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "修复前 - NewsClientTheme (dynamicColor = true)",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
                Spacer(modifier = Modifier.height(8.dp))

                NewsClientTheme(dynamicColor = true) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        ) {}
                        Column {
                            Text(
                                text = "Primary: ${MaterialTheme.colorScheme.primary}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "可能显示: 系统动态颜色",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // 硬编码参考颜色
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "参考 - 硬编码颜色",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7B1FA2)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xFF1F6A4E),
                        modifier = Modifier.size(40.dp)
                    ) {}
                    Column {
                        Text(
                            text = "Primary: #1F6A4E",
                            fontSize = 14.sp
                        )
                        Text(
                            text = "期望的绿色主题色",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

