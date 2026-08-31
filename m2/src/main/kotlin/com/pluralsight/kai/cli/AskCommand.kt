package com.pluralsight.kai.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.pluralsight.kai.config.AppConfig
import com.pluralsight.kai.model.ChatCompletionRequestDto
import com.pluralsight.kai.model.ChatCompletionResponseDto
import com.pluralsight.kai.model.MessageDto
import com.pluralsight.kai.model.MessageRole
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.LoggerFactory

class AskCommand : CliktCommand("ask") {
    private val prompt by argument()
    override fun run() {
        val config = AppConfig.fromEnv()

        val json = Json { ignoreUnknownKeys = true }

        // add after the initial code to show the log
        val wireLogger = LoggerFactory.getLogger("http.wire")
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            wireLogger.info(message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }


        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val requestDto = ChatCompletionRequestDto(
            model = config.chatModel,
            messages = listOf(
                MessageDto(role = MessageRole.SYSTEM, content = "You are a helpful Kotlin assistant"),
                MessageDto(role = MessageRole.USER, content = prompt),
            )
        )

        val payload = json.encodeToString(requestDto)

        val request = Request.Builder()
            .url("${config.baseUrl}/chat/completions")
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .addHeader("Content-Type", "application/json")
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val body: String = response.body.string().orEmpty()
            require(response.isSuccessful) { "LLM request failed: ${response.code} $body" }
            val parsed = json.decodeFromString<ChatCompletionResponseDto>(body)
            echo(parsed.choices.firstOrNull()?.message?.content ?: "No assistant message returned")
        }
    }
}