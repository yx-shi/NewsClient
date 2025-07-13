package com.example.newsclient.data.model

import com.google.gson.annotations.SerializedName

/**
 * GLM API 请求数据模型
 */
data class GLMRequest(
    @SerializedName("model") val model: String = "glm-4-plus",
    @SerializedName("messages") val messages: List<GLMMessage>,
    @SerializedName("max_tokens") val maxTokens: Int? = 1000,
    @SerializedName("request_id") val requestId: String? = null
)

/**
 * GLM 消息格式
 */
data class GLMMessage(
    @SerializedName("role") val role: String, // "system", "user", "assistant"
    @SerializedName("content") val content: String
)

/**
 * GLM API 响应数据模型
 */
data class GLMResponse(
    @SerializedName("id") val id: String,
    @SerializedName("created") val created: Long,
    @SerializedName("model") val model: String,
    @SerializedName("request_id") val requestId: String,
    @SerializedName("choices") val choices: List<GLMChoice>,
    @SerializedName("usage") val usage: GLMUsage
)

/**
 * GLM 选择项
 */
data class GLMChoice(
    @SerializedName("index") val index: Int,
    @SerializedName("message") val message: GLMMessage,
    @SerializedName("finish_reason") val finishReason: String
)

/**
 * GLM 使用统计
 */
data class GLMUsage(
    @SerializedName("prompt_tokens") val promptTokens: Int,
    @SerializedName("completion_tokens") val completionTokens: Int,
    @SerializedName("total_tokens") val totalTokens: Int
)

/**
 * 新闻摘要结果
 */
data class NewsSummary(
    val newsId: String,
    val summary: String,
    val generatedAt: Long = System.currentTimeMillis()
)
