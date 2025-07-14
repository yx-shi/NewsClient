package com.example.newsclient.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.newsclient.NewsApplication
import com.example.newsclient.data.local.UserPreferences
import com.example.newsclient.data.model.UserSettings
import kotlinx.coroutines.flow.Flow

/**
 * 个性化设置页面的ViewModel
 * 负责管理用户个性化设置的状态和逻辑
 */
class PersonalizationViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    /**
     * 获取用户设置的Flow
     */
    fun getUserSettingsFlow(): Flow<UserSettings> {
        return userPreferences.getUserSettingsFlow()
    }

    /**
     * 保存用户设置
     */
    fun saveUserSettings(settings: UserSettings) {
        userPreferences.saveUserSettings(settings)
    }

    /**
     * 获取当前用户设置
     */
    fun getCurrentUserSettings(): UserSettings {
        return userPreferences.getUserSettings()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NewsApplication)
                PersonalizationViewModel(
                    userPreferences = application.userPreferences
                )
            }
        }
    }
}
