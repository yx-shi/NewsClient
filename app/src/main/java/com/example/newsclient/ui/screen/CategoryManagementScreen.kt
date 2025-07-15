package com.example.newsclient.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newsclient.data.model.NewsCategory
import com.example.newsclient.ui.viewmodel.CategoryManagementViewModel
import kotlinx.coroutines.delay

/**
 * 分类管理界面
 * 允许用户添加、删除和重新排序新闻分类
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    onBackClick: () -> Unit,
    viewModel: CategoryManagementViewModel = viewModel(factory = CategoryManagementViewModel.Factory)
) {
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    // 用于追踪新添加的分类，以便显示动画
    var newlyAddedCategory by remember { mutableStateOf<NewsCategory?>(null) }

    // 用于追踪即将删除的分类，以便显示动画
    var categoryToDelete by remember { mutableStateOf<NewsCategory?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 顶部导航栏
        TopAppBar(
            title = {
                Text(
                    text = "分类管理",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            actions = {
                // 添加分类按钮
                IconButton(
                    onClick = { showAddDialog = true },
                    enabled = availableCategories.isNotEmpty()
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "添加分类",
                        tint = if (availableCategories.isNotEmpty())
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // 说明文本
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "分类管理",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• 点击右上角「+」添加新分类\n• 点击分类右侧的删除按钮移除分类\n• 至少保留一个分类",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 当前分类列表
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "当前分类",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${selectedCategories.size}/10",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedCategories.isEmpty()) {
                    Text(
                        text = "请添加至少一个分类",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = selectedCategories,
                            key = { it.value }
                        ) { category ->
                            CategoryItemWithAnimation(
                                category = category,
                                onDeleteClick = {
                                    categoryToDelete = category
                                },
                                showDeleteButton = selectedCategories.size > 1,
                                isNewlyAdded = category == newlyAddedCategory,
                                isBeingDeleted = category == categoryToDelete
                            )
                        }
                    }
                }
            }
        }

        // 底部按钮 - 添加系统导航栏适配
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.navigationBars), // 添加系统导航栏适配
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 重置按钮
            OutlinedButton(
                onClick = {
                    newlyAddedCategory = null
                    categoryToDelete = null
                    viewModel.resetToDefault()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "重置默认",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // 完成按钮
            Button(
                onClick = onBackClick,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "完成",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }

    // 处理删除动画
    LaunchedEffect(categoryToDelete) {
        if (categoryToDelete != null) {
            delay(300) // 等待删除动画完成
            viewModel.removeCategory(categoryToDelete!!)
            categoryToDelete = null
        }
    }

    // 添加分类对话框
    if (showAddDialog) {
        AddCategoryDialog(
            availableCategories = availableCategories,
            onCategorySelected = { category ->
                newlyAddedCategory = category
                viewModel.addCategory(category)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // 处理新添加分类的动画重置
    LaunchedEffect(newlyAddedCategory) {
        if (newlyAddedCategory != null) {
            delay(800) // 等待添加动画完成
            newlyAddedCategory = null
        }
    }
}

/**
 * 带动画效果的分类项
 */
@Composable
private fun CategoryItemWithAnimation(
    category: NewsCategory,
    onDeleteClick: () -> Unit,
    showDeleteButton: Boolean,
    isNewlyAdded: Boolean,
    isBeingDeleted: Boolean
) {
    // 新添加动画：弹性缩放效果
    val addScale by animateFloatAsState(
        targetValue = if (isNewlyAdded) 1f else 1f,
        animationSpec = if (isNewlyAdded) {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        } else {
            tween(0)
        },
        label = "addScale"
    )

    // 新添加动画：透明度渐变
    val addAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = if (isNewlyAdded) {
            tween(durationMillis = 500, easing = EaseInOut)
        } else {
            tween(0)
        },
        label = "addAlpha"
    )

    // 删除动画：水平滑出效果
    val deleteOffsetX by animateFloatAsState(
        targetValue = if (isBeingDeleted) 300f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = EaseInOut
        ),
        label = "deleteOffsetX"
    )

    // 删除动画：透明度渐变
    val deleteAlpha by animateFloatAsState(
        targetValue = if (isBeingDeleted) 0f else 1f,
        animationSpec = tween(
            durationMillis = 300,
            easing = EaseInOut
        ),
        label = "deleteAlpha"
    )

    // 组合所有动画效果
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = deleteOffsetX.dp)
            .scale(addScale)
            .graphicsLayer(alpha = deleteAlpha * addAlpha),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (showDeleteButton) {
                // 删除按钮带轻微的抖动效果
                var isPressed by remember { mutableStateOf(false) }
                val buttonScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.9f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    ),
                    label = "buttonScale"
                )

                IconButton(
                    onClick = {
                        isPressed = true
                        onDeleteClick()
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .scale(buttonScale)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除分类",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 重置按压状态
                LaunchedEffect(isPressed) {
                    if (isPressed) {
                        delay(100)
                        isPressed = false
                    }
                }
            }
        }
    }
}

/**
 * 添加分类对话框
 */
@Composable
private fun AddCategoryDialog(
    availableCategories: List<NewsCategory>,
    onCategorySelected: (NewsCategory) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "添加分类",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (availableCategories.isEmpty()) {
                    Text(
                        text = "所有分类都已添加",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(availableCategories) { category ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCategorySelected(category) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text(
                                    text = category.value,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
