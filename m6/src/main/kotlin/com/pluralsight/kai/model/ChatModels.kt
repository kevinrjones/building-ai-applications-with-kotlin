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
enum class ResponseFormatType {
    @SerialName("json_object") JSON_OBJECT
}

@Serializable
data class ResponseFormatDto(val type: ResponseFormatType = ResponseFormatType.JSON_OBJECT)



@Serializable
data class MessageDto(
    val role: MessageRole,
    val content: String
)


@Serializable
data class ChatCompletionRequestDto(
    val model: String,
    val messages: List<MessageDto>,
    val temperature: Double = 1.0,
    val stream: Boolean = false,
    @SerialName("response_format") val responseFormat: ResponseFormatDto? = null,
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

@Serializable
data class ChatDeltaDto(val content: String? = null)

@Serializable
data class StreamChoiceDto(
    val index: Int,
    val delta: ChatDeltaDto,
    @SerialName("finish_reason") val finishReason: String? = null
)


@Serializable
data class ChatCompletionChunkDto(
    val id: String,
    val choices: List<StreamChoiceDto>
)