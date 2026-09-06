package com.pluralsight.kai.ai

import com.pluralsight.kai.model.ChatCompletionResponseDto
import com.pluralsight.kai.model.MessageDto
import com.pluralsight.kai.model.ResponseFormatDto
import com.pluralsight.kai.model.ToolDto
import kotlinx.coroutines.flow.Flow

interface AiClient {
    suspend fun chat(systemPrompt: String, userPrompt: String): String
    suspend fun chat(systemPrompt: String, history: List<MessageDto>): String

    suspend fun chatEx(
        messages: List<MessageDto>,
        reasoningEffort: String? = null,
        tools: List<ToolDto>? = null,
        responseFormat: ResponseFormatDto? = null
    ): ChatCompletionResponseDto
    fun chatStream(systemPrompt: String, userPrompt: String): Flow<String>
    suspend fun embed(texts: List<String>): List<List<Double>>
}