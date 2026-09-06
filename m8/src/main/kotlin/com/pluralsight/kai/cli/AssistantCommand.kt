package com.pluralsight.kai.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.pluralsight.kai.ai.AiClient
import com.pluralsight.kai.model.FinishReason
import com.pluralsight.kai.model.MessageDto
import com.pluralsight.kai.model.MessageRole
import com.pluralsight.kai.model.ToolDto
import com.pluralsight.kai.model.ToolFunctionDto
import com.pluralsight.kai.prompt.PromptTemplates
import com.pluralsight.kai.tools.ToolRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Duration.Companion.seconds

//./gradlew run --args="assistant --text 'calculate 12*11 and explain'"

const val MAX_TOOL_ROUNDS = 4

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
            name = "calc",
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
        val prompt = PromptTemplates.assistant.render(mapOf("text" to text))
        val messages = mutableListOf(
            MessageDto(role = MessageRole.SYSTEM, content = prompt.systemPrompt),
            MessageDto(role = MessageRole.USER, content = prompt.userPrompt)
        )

        repeat(MAX_TOOL_ROUNDS) { round ->
            val response = withTimeout(30.seconds) {
                client.chatEx(messages, reasoningEffort = "none", toolDefinitions)
            }
            val choice = response.choices.firstOrNull() ?: throw IllegalStateException("No choices returned")
            val message: MessageDto = choice.message

            if (choice.finishReason != FinishReason.TOOL_CALLS || message.toolCalls.isNullOrEmpty()) {
                echo(message.content ?: "No assistant message returned")
                return@runBlocking
            }
            messages += message

            message.toolCalls.forEach { toolCall ->
                val result: Result<String> = registry.execute(toolCall.function.name, toolCall.function.arguments)

                val content = result.getOrElse { cause ->
                    """{"error":"${cause.message}"}"""
                }
                messages += MessageDto(role = MessageRole.TOOL, content = content, toolCallId = toolCall.id)
            }

        }
        echo("Tool loop did not converge after $MAX_TOOL_ROUNDS rounds.", err = true)
    }
}