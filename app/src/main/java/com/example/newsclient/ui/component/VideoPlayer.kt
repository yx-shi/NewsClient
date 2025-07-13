package com.example.newsclient.ui.component

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

/**
 * 视频播放器组件
 * 基于ExoPlayer实现，支持基本的播放控制功能和进度条
 */
@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 视频播放器状态
    var isPlaying by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    // 进度条相关状态
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableStateOf(0L) }

    // 创建ExoPlayer实例
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                // 设置媒体项
                setMediaItem(MediaItem.fromUri(videoUrl))
                // 准备播放器
                prepare()

                // 添加播放器监听器
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        isPlaying = playing
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        isLoading = playbackState == Player.STATE_BUFFERING
                        hasError = playbackState == Player.STATE_ENDED && !isPlaying

                        // 更新视频时长
                        if (playbackState == Player.STATE_READY) {
                            duration = this@apply.duration
                        }
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        hasError = true
                        isLoading = false
                    }
                })
            }
    }

    // 进度更新协程
    LaunchedEffect(isPlaying, isDragging) {
        if (isPlaying && !isDragging) {
            while (isPlaying && !isDragging) {
                currentPosition = exoPlayer.currentPosition
                delay(100) // 每100ms更新一次进度
            }
        }
    }

    // 生命周期管理
    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    // 不自动恢复播放，让用户手动控制
                }
                Lifecycle.Event.ON_DESTROY -> {
                    exoPlayer.release()
                }
                else -> {}
            }
        }
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    // 控制显示/隐藏计时器
    LaunchedEffect(showControls) {
        if (showControls && isPlaying) {
            kotlinx.coroutines.delay(3000) // 3秒后隐藏控制栏
            showControls = false
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
        ) {
            // ExoPlayer视图
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        player = exoPlayer
                        useController = false // 使用自定义控制器
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )

            // 加载指示器
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // 错误状态
            if (hasError) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "视频加载失败",
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                hasError = false
                                isLoading = true
                                exoPlayer.seekTo(0)
                                exoPlayer.prepare()
                            }
                        ) {
                            Text(
                                text = "重试",
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 点击区域 - 显示/隐藏控制栏
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .clickable {
                        showControls = !showControls
                    }
            )

            // 自定义控制栏
            if (showControls && !isLoading && !hasError) {
                VideoControls(
                    isPlaying = isPlaying,
                    isMuted = isMuted,
                    currentPosition = currentPosition,
                    duration = duration,
                    onPlayPauseClick = {
                        if (isPlaying) {
                            exoPlayer.pause()
                        } else {
                            exoPlayer.play()
                        }
                    },
                    onMuteClick = {
                        isMuted = !isMuted
                        exoPlayer.volume = if (isMuted) 0f else 1f
                    },
                    onSeekTo = { position ->
                        exoPlayer.seekTo(position)
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

/**
 * 视频控制栏组件 - 优化布局，减少遮挡面积
 */
@Composable
private fun VideoControls(
    isPlaying: Boolean,
    isMuted: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPauseClick: () -> Unit,
    onMuteClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var tempSliderValue by remember { mutableStateOf((currentPosition / 1000).toFloat()) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp) // 减少垂直padding
    ) {
        // 使用单行紧凑布局
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp) // 元素间距
        ) {
            // 播放/暂停按钮 - 缩小尺寸
            IconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier.size(32.dp) // 从40dp减小到32dp
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "暂停" else "播放",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp) // 从32dp减小到20dp
                )
            }

            // 当前时间 - 缩小字体和宽度
            Text(
                text = formatTime(currentPosition),
                color = Color.White,
                fontSize = 10.sp, // 从12sp减小到10sp
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(35.dp) // 从50dp减小到35dp
            )

            // 进度条 - 占据大部分空间
            Slider(
                value = tempSliderValue,
                onValueChange = { value ->
                    isDragging = true
                    tempSliderValue = value
                },
                onValueChangeFinished = {
                    isDragging = false
                    onSeekTo((tempSliderValue * 1000).toLong())
                },
                valueRange = 0f..(if (duration > 0) duration / 1000 else 0).toFloat(),
                modifier = Modifier.weight(1f)
            )

            // 总时长 - 缩小字体和宽度
            Text(
                text = formatTime(duration),
                color = Color.White,
                fontSize = 10.sp, // 从12sp减小到10sp
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(35.dp) // 从50dp减小到35dp
            )

            // 音量控制按钮 - 缩小尺寸
            IconButton(
                onClick = onMuteClick,
                modifier = Modifier.size(32.dp) // 从40dp减小到32dp
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeDown,
                    contentDescription = if (isMuted) "取消静音" else "静音",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp) // 从24dp减小到16dp
                )
            }
        }
    }

    // 更新临时滑块值，但只在非拖拽状态下
    LaunchedEffect(currentPosition) {
        if (!isDragging) {
            tempSliderValue = (currentPosition / 1000).toFloat()
        }
    }
}

/**
 * 格式化时间显示
 */
private fun formatTime(timeMs: Long): String {
    val totalSeconds = (timeMs / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
