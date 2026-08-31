package com.pluralsight.kai.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class MessageRole {
    @SerialName("system") SYSTEM,
    @SerialName("user") USER,
    @SerialName("assistant") ASSISTANT,
    @SerialName("tool") TOOL
}

@Serializable
enum class FinishReason {
    @SerialName("stop") STOP,
    @SerialName("length") LENGTH,
    @SerialName("tool_calls") TOOL_CALLS,
    @SerialName("content_filter") CONTENT_FILTER
}


@Serializable
data class MessageDto(
    val role: MessageRole,
    val content: String
)


@Serializable
data class ChatCompletionRequestDto(
    val model: String,
    val messages: List<MessageDto>,
    val temperature: Double = 1.0
)

@Serializable
data class ChatCompletionResponseDto(
    val id: String,
    val choices: List<ChoiceDto>
)

@Serializable
data class ChoiceDto(
    val index: Int,
    val message: MessageDto,
    @SerialName("finish_reason") val finishReason: FinishReason? = null
)