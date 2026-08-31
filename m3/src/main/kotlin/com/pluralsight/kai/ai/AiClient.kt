package com.pluralsight.kai.ai

interface AiClient {
    suspend fun chat(systemPrompt: String, userPrompt: String): String
}