package com.pluralsight.kai.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.pluralsight.kai.ai.AiClient
import com.pluralsight.kai.ai.retryBeforeFirstEmission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

suspend fun renderStream(tokens: Flow<String>) {
    tokens.collect { token -> print(token) }
    println()
}

class StreamCommand(private val client: AiClient) : CliktCommand(name = "stream") {
    private val text by option("--text").required()

    override fun run() = runBlocking {
        // Timeout wraps the whole streaming call; retry only before the first token.
        withTimeout(30.seconds) {
            renderStream(
                client.chatStream(
                    systemPrompt = "You are a helpful Kotlin assistant.",
                    userPrompt = text
                ).retryBeforeFirstEmission()
            )
        }
    }
}
