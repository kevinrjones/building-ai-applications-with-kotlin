package com.pluralsight.kai.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.pluralsight.kai.ai.AiClient
import com.pluralsight.kai.flow.retryBeforeFirstEmission
import com.pluralsight.kai.prompt.PromptTemplates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

// ./gradlew -q run --args="stream --text 'Explain Kotlin flows'"


suspend fun renderStream(tokens: Flow<String>) {
    tokens.collect { token -> print(token) }
    println()
}


class StreamCommand(private val client: AiClient) : CliktCommand(name = "stream") {
    private val text by option("--text").required()
    override fun run() = runBlocking {
        val prompt = PromptTemplates.stream.render(mapOf("text" to text))
        withTimeout(30.seconds) {
            renderStream(
                client.chatStream(prompt.systemPrompt, userPrompt = prompt.userPrompt)
                    .retryBeforeFirstEmission()
            )
        }
    }
}