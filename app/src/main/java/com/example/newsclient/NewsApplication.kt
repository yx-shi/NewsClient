package com.example.newsclient

import android.app.Application
import com.example.newsclient.data.model.AppContainer
import com.example.newsclient.data.model.DefaultAppContainer

/**
 * 新闻应用程序类
 * 负责管理应用程序的全局状态和依赖项容器
 * 继承自Application，在应用程序启动时被创建
 */
class NewsApplication: Application() {

    /**
     * 应用程序的依赖项容器
     * 用于管理和提供应用程序所需的各种依赖项
     * 如Repository、数据库、网络服务等
     */
    lateinit var container: AppContainer

    /**
     * 应用程序创建时的回调方法
     * 在这里初始化应用程序的全局组件和依赖项
     */
    override fun onCreate(){
        super.onCreate()
        // 初始化应用容器，传递应用程序的Context
        // 这样容器就可以创建需要Context的组件，如数据库
        container = DefaultAppContainer(applicationContext)
    }
}