package com.example.newsclient.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.newsclient.data.model.FontSizeLevel
import com.example.newsclient.data.model.Gender
import com.example.newsclient.data.model.UserSettings
import com.example.newsclient.ui.theme.NewsClientTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 个性化设置页面
 * 支持用户自定义主题、字体大小等个性化设置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizationScreen(
    onNavigateBack: () -> Unit,
    userSettings: UserSettings,
    onUserSettingsChange: (UserSettings) -> Unit
) {
    // 使用本地状态管理临时更改，只有点击保存才会应用
    var tempSettings by remember(userSettings) { mutableStateOf(userSettings) }
    var hasChanges by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    // 对话框状态
    var showNicknameDialog by remember { mutableStateOf(false) }
    var showGenderDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showSignatureDialog by remember { mutableStateOf(false) }

    // 监听设置变化
    LaunchedEffect(tempSettings) {
        hasChanges = tempSettings != userSettings
    }

    // 处理返回按钮
    val handleBackClick = {
        if (hasChanges) {
            showUnsavedDialog = true
        } else {
            onNavigateBack()
        }
    }

    // 保存设置
    val saveSettings = {
        onUserSettingsChange(tempSettings)
        hasChanges = false
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部应用栏
        TopAppBar(
            title = {
                Text(
                    text = "个性化设置",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            navigationIcon = {
                IconButton(onClick = handleBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            actions = {
                // 保存按钮
                if (hasChanges) {
                    TextButton(
                        onClick = saveSettings,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "保存",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("保存")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // 设置内容
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 个人信息分组
            item {
                SettingsGroup(title = "个人信息") {
                    // 昵称设置
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = "昵称",
                        value = tempSettings.nickname,
                        onClick = { showNicknameDialog = true }
                    )

                    // 性别设置
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = "性别",
                        value = tempSettings.gender.displayName,
                        onClick = { showGenderDialog = true }
                    )

                    // 生日设置
                    SettingsItem(
                        icon = Icons.Default.DateRange,
                        title = "生日",
                        value = if (tempSettings.birthday.isNotEmpty()) {
                            tempSettings.birthday
                        } else {
                            "未设置"
                        },
                        onClick = { showDatePicker = true }
                    )

                    // 个性签名
                    SettingsItem(
                        icon = Icons.Default.Edit,
                        title = "个性签名",
                        value = if (tempSettings.signature.isNotEmpty()) {
                            tempSettings.signature
                        } else {
                            "未设置"
                        },
                        onClick = { showSignatureDialog = true }
                    )
                }
            }

            // 显示设置分组
            item {
                SettingsGroup(title = "显示设置") {
                    // 主题设置
                    ThemeSettingsItem(
                        isDarkTheme = tempSettings.isDarkTheme,
                        onThemeChange = { isDark ->
                            tempSettings = tempSettings.copy(isDarkTheme = isDark)
                        }
                    )

                    // 字体大小设置
                    FontSizeSettingsItem(
                        fontSizeScale = tempSettings.fontSizeScale,
                        onFontSizeChange = { scale ->
                            tempSettings = tempSettings.copy(fontSizeScale = scale)
                        }
                    )
                }
            }
        }

        // 底部保存按钮
        if (hasChanges) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            tempSettings = userSettings
                            hasChanges = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("重置")
                    }

                    Button(
                        onClick = saveSettings,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("保存更改")
                    }
                }
            }
        }
    }

    // 未保存更改提示对话框
    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = {
                Text("未保存的更改")
            },
            text = {
                Text("您有未保存的更改，是否要保存？")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        saveSettings()
                        showUnsavedDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showUnsavedDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("不保存")
                }
            }
        )
    }

    // 其他对话框保持不变，但使用tempSettings
    if (showNicknameDialog) {
        NicknameDialog(
            currentNickname = tempSettings.nickname,
            onConfirm = { newNickname ->
                tempSettings = tempSettings.copy(nickname = newNickname)
                showNicknameDialog = false
            },
            onDismiss = { showNicknameDialog = false }
        )
    }

    if (showGenderDialog) {
        GenderDialog(
            currentGender = tempSettings.gender,
            onConfirm = { newGender ->
                tempSettings = tempSettings.copy(gender = newGender)
                showGenderDialog = false
            },
            onDismiss = { showGenderDialog = false }
        )
    }

    if (showDatePicker) {
        BirthdayDatePickerDialog(
            currentDate = tempSettings.birthday,
            onConfirm = { newDate ->
                tempSettings = tempSettings.copy(birthday = newDate)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showSignatureDialog) {
        SignatureDialog(
            currentSignature = tempSettings.signature,
            onConfirm = { newSignature ->
                tempSettings = tempSettings.copy(signature = newSignature)
                showSignatureDialog = false
            },
            onDismiss = { showSignatureDialog = false }
        )
    }
}

/**
 * 设置分组组件
 */
@Composable
fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

/**
 * 设置项组件
 */
@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "编辑",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * 主题设置项
 */
@Composable
fun ThemeSettingsItem(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Palette,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "主题模式",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isDarkTheme) "深色模式" else "浅色模式",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = isDarkTheme,
            onCheckedChange = onThemeChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

/**
 * 字体大小设置项
 */
@Composable
fun FontSizeSettingsItem(
    fontSizeScale: Float,
    onFontSizeChange: (Float) -> Unit
) {
    val currentLevel = FontSizeLevel.fromScale(fontSizeScale)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.TextFields,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "字体大小",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = currentLevel.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 字体大小调节器
        FontSizeSlider(
            currentScale = fontSizeScale,
            onScaleChange = onFontSizeChange
        )
    }
}

/**
 * 字体大小滑块组件
 */
@Composable
fun FontSizeSlider(
    currentScale: Float,
    onScaleChange: (Float) -> Unit
) {
    val levels = FontSizeLevel.values()
    val currentIndex = levels.indexOf(FontSizeLevel.fromScale(currentScale))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // 减小按钮
        IconButton(
            onClick = {
                val newIndex = (currentIndex - 1).coerceAtLeast(0)
                onScaleChange(levels[newIndex].scale)
            },
            enabled = currentIndex > 0
        ) {
            Icon(
                imageVector = Icons.Default.TextFields, // 可以换成减号图标
                contentDescription = "减小字体",
                tint = if (currentIndex > 0)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }

        // 滑块
        Slider(
            value = currentIndex.toFloat(),
            onValueChange = { value ->
                val index = value.toInt().coerceIn(0, levels.size - 1)
                onScaleChange(levels[index].scale)
            },
            valueRange = 0f..(levels.size - 1).toFloat(),
            steps = levels.size - 2,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        // 增大按钮
        IconButton(
            onClick = {
                val newIndex = (currentIndex + 1).coerceAtMost(levels.size - 1)
                onScaleChange(levels[newIndex].scale)
            },
            enabled = currentIndex < levels.size - 1
        ) {
            Icon(
                imageVector = Icons.Default.TextFields, // 可以换成加号图标
                contentDescription = "增大字体",
                tint = if (currentIndex < levels.size - 1)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }

    // 字体预览
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "字体预览：这是${FontSizeLevel.fromScale(currentScale).displayName}字体",
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = (14 * currentScale).sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * 昵称编辑对话框
 */
@Composable
fun NicknameDialog(
    currentNickname: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var nickname by remember { mutableStateOf(currentNickname) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "设置昵称",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("昵称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nickname.isNotBlank()) {
                        onConfirm(nickname.trim())
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 性别选择对话框
 */
@Composable
fun GenderDialog(
    currentGender: Gender,
    onConfirm: (Gender) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedGender by remember { mutableStateOf(currentGender) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "选择性别",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                Gender.values().forEach { gender ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedGender = gender }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedGender == gender,
                            onClick = { selectedGender = gender }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = gender.displayName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedGender) }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 生日日期选择对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayDatePickerDialog(
    currentDate: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = if (currentDate.isNotEmpty()) {
            try {
                LocalDate.parse(currentDate).toEpochDay() * 24 * 60 * 60 * 1000
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        } else {
            System.currentTimeMillis()
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        onConfirm(localDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = "选择生日",
                    modifier = Modifier.padding(16.dp)
                )
            }
        )
    }
}

/**
 * 个性签名编辑对话框
 */
@Composable
fun SignatureDialog(
    currentSignature: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var signature by remember { mutableStateOf(currentSignature) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "设置个性签名",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            OutlinedTextField(
                value = signature,
                onValueChange = { signature = it },
                label = { Text("个性签名") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(signature.trim()) }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 个性化设置页面预览
 */
@Composable
@Preview(showBackground = true, name = "个性化设置页面")
fun PersonalizationScreenPreview() {
    NewsClientTheme {
        PersonalizationScreen(
            onNavigateBack = {},
            userSettings = UserSettings(
                nickname = "新闻用户007",
                gender = Gender.MALE,
                birthday = "1990-01-01",
                signature = "热爱生活，关注时事",
                isDarkTheme = false,
                fontSizeScale = 1.0f
            ),
            onUserSettingsChange = {}
        )
    }
}

/**
 * 字体大小滑块预览
 */
@Composable
@Preview(showBackground = true, name = "字体大小滑块")
fun FontSizeSliderPreview() {
    NewsClientTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            FontSizeSlider(
                currentScale = 1.0f,
                onScaleChange = {}
            )
        }
    }
}

/**
 * 设置分组预览
 */
@Composable
@Preview(showBackground = true, name = "设置分组")
fun SettingsGroupPreview() {
    NewsClientTheme {
        SettingsGroup(title = "个人信息") {
            SettingsItem(
                icon = Icons.Default.Person,
                title = "昵称",
                value = "新闻用户007",
                onClick = {}
            )
            SettingsItem(
                icon = Icons.Default.DateRange,
                title = "生日",
                value = "1990-01-01",
                onClick = {}
            )
        }
    }
}
