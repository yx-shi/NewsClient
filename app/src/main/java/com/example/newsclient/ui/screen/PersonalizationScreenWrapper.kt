package com.example.newsclient.ui.screen

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newsclient.NewsApplication
import com.example.newsclient.data.model.UserSettings
import com.example.newsclient.ui.viewmodel.PersonalizationViewModel

/**
 * 个性化设置页面包装器
 * 负责处理数据绑定和状态管理
 */
@Composable
fun PersonalizationScreenWrapper(
    onBackClick: () -> Unit,
    viewModel: PersonalizationViewModel = viewModel(factory = PersonalizationViewModel.Factory)
) {
    val context = LocalContext.current
    val application = context.applicationContext as NewsApplication
    val userPreferences = application.userPreferences

    // 监听用户设置的变化 - 使用remember获取初始值
    val initialSettings = remember { userPreferences.getUserSettings() }
    val userSettings by userPreferences.getUserSettingsFlow().collectAsState(initial = initialSettings)

    // 确保初始状态正确
    LaunchedEffect(Unit) {
        // 强制刷新一次用户设置，确保数据同步
        android.util.Log.d("PersonalizationWrapper", "初始化用户设置: ${userSettings}")
    }

    PersonalizationScreen(
        onNavigateBack = onBackClick,
        userSettings = userSettings,
        onUserSettingsChange = { newSettings ->
            // 保存用户设置到本地存储
            android.util.Log.d("PersonalizationWrapper", "保存用户设置: ${newSettings}")
            userPreferences.saveUserSettings(newSettings)
        }
    )
}
