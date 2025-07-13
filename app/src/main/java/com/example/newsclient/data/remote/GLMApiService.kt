package com.example.newsclient.data.remote

import com.example.newsclient.data.model.GLMRequest
import com.example.newsclient.data.model.GLMResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * GLM API接口
 * 用于调用智谱AI的GLM大模型生成新闻摘要
 */
interface GLMApiService {

    @POST("chat/completions")
    suspend fun generateSummary(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: GLMRequest
    ): GLMResponse

    companion object {
        const val BASE_URL = "https://open.bigmodel.cn/api/paas/v4/"

        /**
         * 创建授权头
         * @param apiKey GLM API密钥
         */
        fun createAuthHeader(apiKey: String): String {
            return "Bearer $apiKey"
        }

        /**
         * 创建新闻摘要请求
         * @param newsTitle 新闻标题
         * @param newsContent 新闻内容
         */
        fun createSummaryRequest(newsTitle: String, newsContent: String): GLMRequest {
            val systemMessage = com.example.newsclient.data.model.GLMMessage(
                role = "system",
                content = "你是一个专业的新闻摘要助手。请为用户提供的新闻生成简洁、准确的摘要，要求：1. 摘要长度控制在100-150字 2. 突出新闻的核心要点 3. 语言简洁明了 4. 保持客观中立"
            )

            val userMessage = com.example.newsclient.data.model.GLMMessage(
                role = "user",
                content = "请为以下新闻生成摘要：\n\n标题：$newsTitle\n\n内容：$newsContent"
            )

            return GLMRequest(
                model = "glm-4-plus",
                messages = listOf(systemMessage, userMessage),
                maxTokens = 200
            )
        }
    }
}
