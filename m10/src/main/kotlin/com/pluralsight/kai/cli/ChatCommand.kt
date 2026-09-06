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

// ./gradlew -q --console=plain run --args="chat --session team-1"
const val COMPRESS_THRESHOLD = 10
private const val COMPRESS_COUNT = 6

private val logger = KotlinLogging.logger(name = "module")

class ChatCommand(private val client: AiClient) : CliktCommand(name = "chat") {
    private val session by option("--session").required()
    override fun run() = runBlocking {
        val memory = ConversationMemory()
        echo("Chat session '$session' — type 'exit' to quit.")
        while (true) {
            print(">> ")
            val input = readlnOrNull()?.trim() ?: break
            if (input.equals("exit", ignoreCase = true)) break
            if (input.isEmpty()) continue

            val prompt = PromptTemplates.assistant.render(mapOf("text" to input))
            memory.add(MessageRole.USER, prompt.userPrompt)

            compressIfNeeded(memory)

            val reply = client.chat(
                systemPrompt = prompt.systemPrompt,
                history = memory.snapshot()
            )
            memory.add(MessageRole.ASSISTANT, reply)
            echo(reply)
        }
    }

    private suspend fun compressIfNeeded(memory: ConversationMemory) {
        if (memory.messageCount() <= COMPRESS_THRESHOLD) return

        logger.debug { "compressing turns=$COMPRESS_COUNT session=$session" }

        val oldest = memory.snapshot().take(COMPRESS_COUNT)
        val turnsText = oldest.joinToString("\n") { "${it.role}: ${it.content.orEmpty()}" }

        val renderedPrompt = PromptTemplates.summarizeHistory.render(mapOf("turns" to turnsText))

        val summary = client.chat(
            systemPrompt = renderedPrompt.systemPrompt,
            userPrompt = renderedPrompt.userPrompt
        )

        memory.replaceOldest(
            COMPRESS_COUNT,
            MessageDto(role = MessageRole.SYSTEM, content = "Conversation so far: $summary")
        )

        logger.info { "memory compressed turns=$COMPRESS_COUNT session=$session" }

    }
}















