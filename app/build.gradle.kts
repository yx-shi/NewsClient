plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.example.newsclient"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.newsclient"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // 方案1：使用debug keystore作为临时解决方案
            storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"

            // 方案2：使用自定义keystore（推荐生产环境）
            // 取消注释下面的代码并替换为您的keystore信息
            // storeFile = file("release-key.keystore")
            // storePassword = "your_store_password"
            // keyAlias = "your_key_alias"
            // keyPassword = "your_key_password"
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            // debug版本使用默认的debug签名
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 使用自定义签名配置
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.media3.test.utils)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // ===== MVVM 架构组件 =====
    // ViewModel 和 LiveData 支持
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    // ViewModel 与 Compose 集成
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    // StateFlow 和 Flow 支持
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // ===== 网络请求组件 =====
    // Retrofit 及相关依赖
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // OkHttp 用于网络请求拦截和日志
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ===== 本地数据库 Room =====
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1") // Kotlin 扩展和协程支持
    kapt("androidx.room:room-compiler:2.6.1")

    // ===== 协程支持 =====
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // ===== 图片加载 =====
    // Coil 用于 Compose 中的图片加载（推荐用于 Compose 项目）
    implementation("io.coil-kt:coil-compose:2.5.0")
    // 如果你仍想使用 Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // ===== 视频播放 =====
    // ExoPlayer 用于视频播放
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")
    implementation("androidx.media3:media3-common:1.2.1")
    // ExoPlayer 与 Compose 集成
    implementation("androidx.media3:media3-session:1.2.1")

    // ===== 导航组件 =====
    // Navigation Compose 用于页面导航
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // ===== 下拉刷新和上拉加载 =====
    // Compose 下拉刷新组件
    implementation("androidx.compose.material:material:1.5.4")
    // 分页组件
    implementation("androidx.paging:paging-runtime:3.2.1")
    implementation("androidx.paging:paging-compose:3.2.1")

    // ===== JSON 解析 =====
    // Gson 用于 JSON 解析
    implementation("com.google.code.gson:gson:2.10.1")

    // ===== 权限处理 =====
    // 运行时权限处理
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // ===== Material Icons 扩展 =====
    // Material Icons 扩展包，包含更多图标如 Pause, VolumeOff, VolumeUp 等
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    // ===== 测试相关 =====
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // UI 测试
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.6")

}