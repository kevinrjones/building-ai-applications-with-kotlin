package com.pluralsight.kai.ai

import com.pluralsight.kai.model.ChatCompletionResponseDto
import com.pluralsight.kai.model.MessageDto
import com.pluralsight.kai.model.ResponseFormatDto

interface AiClient {
    suspend fun chat(systemPrompt: String, userPrompt: String): String
    suspend fun chatEx(
        messages: List<MessageDto>,
        responseFormat: ResponseFormatDto? = null
    ): ChatCompletionResponseDto

}