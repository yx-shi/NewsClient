package com.example.newsclient

import android.app.Application
import com.example.newsclient.data.model.AppContainer
import com.example.newsclient.data.model.DefaultAppContainer

/**
 * 将应用容器连接到应用
 */

class NewsApplication: Application() {
    lateinit var container: AppContainer
    override fun onCreate(){
        super.onCreate()
        // 初始化应用容器
        container = DefaultAppContainer()
    }
}