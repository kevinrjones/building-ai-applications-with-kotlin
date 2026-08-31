package com.pluralsight.kai.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.pluralsight.kai.ai.AiClient
import com.pluralsight.kai.memory.ConversationMemory
import com.pluralsight.kai.model.MessageDto
import com.pluralsight.kai.model.MessageRole
import com.pluralsight.kai.prompt.PromptTemplates
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking

// ./gradlew run --args="chat --session team-1"

private val logger = KotlinLogging.logger(name = "module")

const val COMPRESS_THRESHOLD = 10
private const val COMPRESS_COUNT = 6

class ChatCommand(private val client: AiClient) : CliktCommand(name = "chat") {
    private val session by option("--session").required()

    override fun run() = runBlocking {
        logger.debug { "Starting chat session '$session'" }

        val memory = ConversationMemory()
        echo("Chat session '$session' — type 'exit' to quit.")
        while (true) {
            print("> ")
            val input = readlnOrNull()?.trim() ?: break
            if (input.equals("exit", ignoreCase = true)) break
            if (input.isEmpty()) continue

            memory.add(MessageRole.USER, input)
            compressIfNeeded(memory)
            val reply = client.chat(
                systemPrompt = "You are a helpful Kotlin assistant.",
                history = memory.snapshot() // add a history-aware chat overload
            )
            memory.add(MessageRole.ASSISTANT, reply)
            echo(reply)
        }
    }

    // Summarization-based compression: condense the oldest turns into one
    // system message. The original system prompt is never replaced.
    private suspend fun compressIfNeeded(memory: ConversationMemory) {
        if (memory.messageCount() <= COMPRESS_THRESHOLD) return

        logger.debug { "compressing turns=$COMPRESS_COUNT session=$session" }

        val oldest = memory.snapshot().take(COMPRESS_COUNT)
        val turnsText = oldest.joinToString("\n") { "${it.role}: ${it.content.orEmpty()}" }
        val summary = client.chat(
            systemPrompt = "You compress conversation history.",
            userPrompt = PromptTemplates.summarizeHistory.render(mapOf("turns" to turnsText))
        )
        memory.replaceOldest(
            COMPRESS_COUNT,
            MessageDto(role = MessageRole.SYSTEM, content = "Conversation so far: $summary")
        )
        logger.info { "memory compressed turns=$COMPRESS_COUNT session=$session" }
    }
}
