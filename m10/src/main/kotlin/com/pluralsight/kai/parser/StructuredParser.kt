package com.pluralsight.kai.parser

import kotlinx.serialization.json.Json

/**
 * Part 4.
 */
class StructuredParser(
    val json: Json = Json { ignoreUnknownKeys = true }
) {
    inline fun <reified T> parse(raw: String): Result<T> = runCatching {
        json.decodeFromString<T>(stripFences(raw))
    }

    // Models frequently wrap JSON in ```json fences — strip them before decoding.
    fun stripFences(raw: String): String =
        raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
}
