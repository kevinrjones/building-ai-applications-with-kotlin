package com.pluralsight.kai.ai

import com.pluralsight.kai.config.AppConfig
import com.pluralsight.kai.model.ChatCompletionRequestDto
import com.pluralsight.kai.model.ChatCompletionResponseDto
import com.pluralsight.kai.model.MessageDto
import com.pluralsight.kai.model.MessageRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class OpenAiCompatibleClient(
    private val config: AppConfig,
    private val httpClient: OkHttpClient  = OkHttpClient(),
    private val json: Json = Json { ignoreUnknownKeys = true }
    ) : AiClient {

    override suspend fun chat(systemPrompt: String, userPrompt: String): String = withContext(Dispatchers.IO){
        val requestDto = ChatCompletionRequestDto(
            model = config.chatModel,
            messages = listOf(
                MessageDto(role = MessageRole.SYSTEM, content = systemPrompt),
                MessageDto(role = MessageRole.USER, content = userPrompt),
            )
        )

        val payload = json.encodeToString(requestDto)

        val request = Request.Builder()
            .url("${config.baseUrl}/chat/completions")
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .addHeader("Content-Type", "application/json")
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()

        httpClient.newCall(request).execute().use { response ->
            val body: String = response.body.string().orEmpty()
            require(response.isSuccessful) { "LLM request failed: ${response.code} $body" }
            val parsed = json.decodeFromString<ChatCompletionResponseDto>(body)
            parsed.choices.firstOrNull()?.message?.content ?: "No assistant message returned"
        }

    }
}