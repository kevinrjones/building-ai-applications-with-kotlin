package com.pluralsight.kai.ai

import com.pluralsight.kai.model.ChatCompletionResponseDto
import com.pluralsight.kai.model.MessageDto
import com.pluralsight.kai.model.ResponseFormatDto
import kotlinx.coroutines.flow.Flow

interface AiClient {
    suspend fun chat(systemPrompt: String, userPrompt: String): String
    suspend fun chatEx(
        messages: List<MessageDto>,
        responseFormat: ResponseFormatDto? = null
    ): ChatCompletionResponseDto
    fun chatStream(systemPrompt: String, userPrompt: String): Flow<String>

}