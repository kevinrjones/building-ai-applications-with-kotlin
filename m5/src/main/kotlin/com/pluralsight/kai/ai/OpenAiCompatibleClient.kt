package com.pluralsight.kai.ai

import com.pluralsight.kai.config.AppConfig
import com.pluralsight.kai.model.ChatCompletionRequestDto
import com.pluralsight.kai.model.ChatCompletionResponseDto
import com.pluralsight.kai.model.MessageDto
import com.pluralsight.kai.model.MessageRole
import com.pluralsight.kai.model.ResponseFormatDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class OpenAiCompatibleClient(
    private val config: AppConfig,
    private val http: OkHttpClient = OkHttpClient(),
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }
) : AiClient {


    override suspend fun chat(systemPrompt: String, userPrompt: String): String {

        val parsed = chatEx(
            messages = listOf(
                MessageDto(role = MessageRole.SYSTEM, content = systemPrompt),
                MessageDto(role = MessageRole.USER, content = userPrompt)
            )
        )
        return parsed.choices.firstOrNull()?.message?.content
            ?: throw IllegalStateException("No assistant message returned")

    }

    override suspend fun chatEx(
        messages: List<MessageDto>,
        responseFormat: ResponseFormatDto?
    ): ChatCompletionResponseDto = withContext(Dispatchers.IO) {
        val requestDto = ChatCompletionRequestDto(
            model = config.chatModel,
            messages = messages,
            responseFormat = responseFormat
        )

        val payload = json.encodeToString(requestDto)
        val request = Request.Builder()
            .url("${config.baseUrl}/chat/completions")
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .addHeader("Content-Type", "application/json")
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()

        http.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            require(response.isSuccessful) { "LLM request failed: ${response.code} $body" }

            json.decodeFromString<ChatCompletionResponseDto>(body)
        }
    }
}