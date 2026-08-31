package com.pluralsight.kai.cli

import com.github.ajalt.clikt.core.parse
import com.pluralsight.kai.ai.AiClient
import com.pluralsight.kai.model.ChatCompletionResponseDto
import com.pluralsight.kai.model.MessageDto
import com.pluralsight.kai.model.ResponseFormatDto
import com.pluralsight.kai.model.ToolDto
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FakeAiClient(private val canned: String) : AiClient {
    override suspend fun chat(systemPrompt: String, userPrompt: String): String = canned

    override fun chatStream(systemPrompt: String, userPrompt: String): Flow<String> = emptyFlow()

    override suspend fun chat(systemPrompt: String, history: List<MessageDto>): String = canned

    override suspend fun chatEx(
        messages: List<MessageDto>,
        tools: List<ToolDto>?,
        responseFormat: ResponseFormatDto?
    ): ChatCompletionResponseDto {
        error("not used")
    }

    override suspend fun embed(texts: List<String>): List<List<Double>> = emptyList()
}

class RecordingAiClient(private val canned: String) : AiClient {
    var seenSystem: String? = null
    var seenUser: String? = null
    override suspend fun chat(systemPrompt: String, userPrompt: String): String {
        seenSystem = systemPrompt
        seenUser = userPrompt
        return canned
    }

    override fun chatStream(systemPrompt: String, userPrompt: String): Flow<String> = emptyFlow()

    override suspend fun chat(systemPrompt: String, history: List<MessageDto>): String = canned

    override suspend fun chatEx(
        messages: List<MessageDto>,
        tools: List<ToolDto>?,
        responseFormat: ResponseFormatDto?
    ): ChatCompletionResponseDto {
        error("not used")
    }

    override suspend fun embed(texts: List<String>): List<List<Double>> = emptyList()
}

class AskCommandTest {
    @Test
    fun `returns model response for a prompt`() = runTest {
        val client = FakeAiClient("Kotlin is concise.")
        assertEquals("Kotlin is concise.", client.chat("sys", "What is Kotlin?"))
    }


    @Test
    fun `ask command wires prompt and prints response`() = runTest {
        val fake = RecordingAiClient("Kotlin is concise.")
        val command = AskCommand(fake)

        val output = captureStdout {
            command.parse(listOf("What is Kotlin?"))
        }

        assertEquals("You are a helpful Kotlin assistant", fake.seenSystem)
        assertEquals("What is Kotlin?", fake.seenUser)
        assertTrue(output.contains("Kotlin is concise."))
    }

    private fun captureStdout(block: () -> Unit): String {
        val originalOut = System.out
        val output = ByteArrayOutputStream()
        System.setOut(PrintStream(output))
        try {
            block()
        } finally {
            System.setOut(originalOut)
        }
        return output.toString().trim()
    }
}