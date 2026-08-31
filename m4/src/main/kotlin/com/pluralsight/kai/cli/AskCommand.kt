package com.pluralsight.kai.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.pluralsight.kai.ai.AiClient
import com.pluralsight.kai.config.AppConfig
import com.pluralsight.kai.model.ChatCompletionRequestDto
import com.pluralsight.kai.model.ChatCompletionResponseDto
import com.pluralsight.kai.model.MessageDto
import com.pluralsight.kai.model.MessageRole
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.LoggerFactory

class AskCommand(private val client: AiClient) : CliktCommand("ask") {
    private val prompt by argument()
    override fun run() = runBlocking {

        echo(client.chat("You are a helpful Kotlin assistant", prompt))

    }
}