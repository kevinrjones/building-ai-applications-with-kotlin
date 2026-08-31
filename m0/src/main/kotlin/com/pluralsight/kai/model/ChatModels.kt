package com.pluralsight.kai.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
enum class MessageRole {
    @SerialName("system") SYSTEM,
    @SerialName("user") USER,
    @SerialName("assistant") ASSISTANT,
    @SerialName("tool") TOOL
}

@Serializable
enum class ResponseFormatType {
    @SerialName("json_object") JSON_OBJECT
}

@Serializable
enum class FinishReason {
    @SerialName("stop") STOP,
    @SerialName("length") LENGTH,
    @SerialName("tool_calls") TOOL_CALLS,
    @SerialName("content_filter") CONTENT_FILTER
}

@Serializable
enum class ToolType {
    @SerialName("function") FUNCTION
}



// MessageDto gains optional tool fields:
@Serializable
data class MessageDto(
    val role: MessageRole,
    val content: String? = null,
    @SerialName("tool_calls") val toolCalls: List<ToolCallDto>? = null,
    @SerialName("tool_call_id") val toolCallId: String? = null
)

@Serializable
data class ResponseFormatDto(val type: ResponseFormatType = ResponseFormatType.JSON_OBJECT)


@Serializable
data class ChatCompletionRequestDto(
    val model: String,
    val messages: List<MessageDto>,
    val temperature: Double = 1.0,
    val stream: Boolean = false,
    @SerialName("response_format") val responseFormat: ResponseFormatDto? = null,
    val tools: List<ToolDto>? = null
)

@Serializable
data class UsageDto(
    @SerialName("prompt_tokens") val promptTokens: Int = 0,
    @SerialName("completion_tokens") val completionTokens: Int = 0,
    @SerialName("total_tokens") val totalTokens: Int = 0
)

@Serializable
data class ChatCompletionResponseDto(
    val id: String,
    val choices: List<ChoiceDto>,
    val usage: UsageDto? = null // OpenAI returns this on non-streaming responses
)

@Serializable
data class ChoiceDto(
    val index: Int,
    val message: MessageDto,
    @SerialName("finish_reason") val finishReason: FinishReason? = null
)

// --- Streaming (SSE) chunk DTOs (Module 6) ---

@Serializable
data class ChatDeltaDto(val content: String? = null)

@Serializable
data class StreamChoiceDto(
    val index: Int,
    val delta: ChatDeltaDto,
    @SerialName("finish_reason") val finishReason: FinishReason? = null
)

@Serializable
data class ChatCompletionChunkDto(
    val id: String,
    val choices: List<StreamChoiceDto>
)

// --- Tool-calling wire protocol DTOs (Module 7) ---

@Serializable
data class ToolFunctionDto(
    val name: String,
    val description: String,
    val parameters: JsonObject // JSON Schema
)

@Serializable
data class ToolDto(val type: ToolType = ToolType.FUNCTION, val function: ToolFunctionDto)

@Serializable
data class ToolCallFunctionDto(val name: String, val arguments: String)

@Serializable
data class ToolCallDto(
    val id: String,
    val type: ToolType = ToolType.FUNCTION,
    val function: ToolCallFunctionDto
)

@Serializable
data class EmbeddingRequestDto(val model: String, val input: List<String>)

@Serializable
data class EmbeddingDto(val index: Int, val embedding: List<Double>)

@Serializable
data class EmbeddingResponseDto(val data: List<EmbeddingDto>)
