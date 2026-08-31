package com.pluralsight.kai.ai

import com.pluralsight.kai.model.ChatCompletionResponseDto
import com.pluralsight.kai.model.MessageDto
import com.pluralsight.kai.model.ResponseFormatDto
import com.pluralsight.kai.model.ToolDto
import kotlinx.coroutines.flow.Flow

interface AiClient {
    suspend fun chat(systemPrompt: String, userPrompt: String): String

    fun chatStream(systemPrompt: String, userPrompt: String): Flow<String>

    // History-aware overload added in Module 9 for the chat REPL.
    suspend fun chat(systemPrompt: String, history: List<MessageDto>): String

    suspend fun chatEx(
        messages: List<MessageDto>,
        tools: List<ToolDto>? = null,
        responseFormat: ResponseFormatDto? = null
    ): ChatCompletionResponseDto

    suspend fun embed(texts: List<String>): List<List<Double>>
}