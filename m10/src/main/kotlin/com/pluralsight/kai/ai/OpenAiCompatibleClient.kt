package com.pluralsight.kai.ai

import com.pluralsight.kai.config.AppConfig
import com.pluralsight.kai.model.ChatCompletionChunkDto
import com.pluralsight.kai.model.ChatCompletionRequestDto
import com.pluralsight.kai.model.ChatCompletionResponseDto
import com.pluralsight.kai.model.EmbeddingRequestDto
import com.pluralsight.kai.model.EmbeddingResponseDto
import com.pluralsight.kai.model.MessageDto
import com.pluralsight.kai.model.MessageRole
import com.pluralsight.kai.model.ResponseFormatDto
import com.pluralsight.kai.model.ToolDto
import jdk.jfr.internal.OldObjectSample.emit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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

    override suspend fun chat(
        systemPrompt: String,
        history: List<MessageDto>
    ): String {
        val messages = listOf(MessageDto(role = MessageRole.SYSTEM, content = systemPrompt)) + history
        val parsed = chatEx(messages)
        return parsed.choices.firstOrNull()?.message?.content
            ?: throw IllegalStateException("No assistant message returned")

    }

    override suspend fun chatEx(
        messages: List<MessageDto>,
        reasoningEffort: String?,
        tools: List<ToolDto>?,
        responseFormat: ResponseFormatDto?
    ): ChatCompletionResponseDto = withContext(Dispatchers.IO) {
        val requestDto = ChatCompletionRequestDto(
            model = config.chatModel,
            messages = messages,
            responseFormat = responseFormat,
            reasoningEffort = reasoningEffort,
            tools = tools
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

    override fun chatStream(
        systemPrompt: String,
        userPrompt: String
    ): Flow<String> = flow {
        val requestDto = ChatCompletionRequestDto(
            model = config.chatModel,
            messages = listOf(
                MessageDto(role = MessageRole.SYSTEM, content = systemPrompt),
                MessageDto(role = MessageRole.USER, content = userPrompt)
            ),
            stream = true
        )

        val payload = json.encodeToString(requestDto)
        val request = Request.Builder()
            .url("${config.baseUrl}/chat/completions")
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "text/event-stream")
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()

        http.newCall(request).execute().use { response ->
            require(response.isSuccessful) { "LLM request failed: ${response.code}" }
            val source = response.body.source()

            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: break

                if (!line.startsWith("data:")) continue
                val data = line.removePrefix("data:").trim()
                if (data == "[DONE]") break

                val chunk = json.decodeFromString<ChatCompletionChunkDto>(data)
                chunk.choices.firstOrNull()?.delta?.content?.let { emit(it) }
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun embed(texts: List<String>): List<List<Double>> = withContext(Dispatchers.IO) {
        val requestDto = EmbeddingRequestDto(model = config.embeddingModel, input = texts)
        val payload = json.encodeToString(requestDto)
        val request = Request.Builder()
            .url("${config.baseUrl}/embeddings")
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .addHeader("Content-Type", "application/json")
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()

        http.newCall(request).execute().use { response ->
            val body = response.body.string()
            require(response.isSuccessful) { "Embedding request failed: ${response.code} $body" }

            val parsed = json.decodeFromString<EmbeddingResponseDto>(body)
            parsed.data.sortedBy { it.index }.map { it.embedding }
        }

    }
}