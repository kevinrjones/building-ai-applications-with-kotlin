package com.pluralsight.kai.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.pluralsight.kai.ai.AiClient
import com.pluralsight.kai.model.MessageDto
import com.pluralsight.kai.model.ToolDto
import com.pluralsight.kai.model.ToolFunctionDto
import com.pluralsight.kai.tools.ToolRegistry
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Duration.Companion.seconds
import com.pluralsight.kai.model.MessageRole
import com.pluralsight.kai.model.FinishReason

private val logger = KotlinLogging.logger(name = "module")

const val MAX_TOOL_ROUNDS = 4

// JSON Schema for the calculator's arguments — the contract the model must honor.
private val calculatorSchema = buildJsonObject {
    put("type", "object")
    put("properties", buildJsonObject {
        put("op", buildJsonObject {
            put("type", "string")
            put("enum", buildJsonArray {
                add("add"); add("subtract"); add("multiply"); add("divide")
            })
        })
        put("a", buildJsonObject { put("type", "number") })
        put("b", buildJsonObject { put("type", "number") })
    })
    put("required", buildJsonArray { add("op"); add("a"); add("b") })
}

private val toolDefinitions = listOf(
    ToolDto(
        function = ToolFunctionDto(
            name = "calculator",
            description = "Performs basic arithmetic: add, subtract, multiply, divide.",
            parameters = calculatorSchema
        )
    )
)

class AssistantCommand(
    private val client: AiClient,
    private val registry: ToolRegistry
) : CliktCommand(name = "assistant") {
    private val text by option("--text").required()

    override fun run() = runBlocking {
        val messages = mutableListOf(
            MessageDto(role = MessageRole.SYSTEM, content = "You are a helpful Kotlin assistant. Use tools when they help."),
            MessageDto(role = MessageRole.USER, content = text)
        )

        repeat(MAX_TOOL_ROUNDS) { round ->
            // Each model round is wrapped with Module 6's timeout pattern.
            val response = withTimeout(30.seconds) {
                client.chatEx(messages = messages, tools = toolDefinitions)
            }
            val choice = response.choices.firstOrNull() ?: throw IllegalStateException("No choices returned")
            val message = choice.message

            if (choice.finishReason != FinishReason.TOOL_CALLS || message.toolCalls.isNullOrEmpty()) {
                echo(message.content ?: "No assistant message returned")
                return@runBlocking
            }

            messages += message // echo the assistant's tool-call message back

            message.toolCalls.forEach { toolCall ->
                logger.info { "tool round=$round name=${toolCall.function.name} args=${toolCall.function.arguments}" }
                // Registry: permission check -> validation -> execution.
                val result = registry.execute(toolCall.function.name, toolCall.function.arguments)
                val content = result.getOrElse { cause ->
                    // Controlled failure path: the model gets an explainable error, not a crash.
                    """{"error":"${cause.message}"}"""
                }
                messages += MessageDto(role = MessageRole.TOOL, content = content, toolCallId = toolCall.id)
            }
        }

        echo("Tool loop did not converge after $MAX_TOOL_ROUNDS rounds.", err = true)
    }
}

