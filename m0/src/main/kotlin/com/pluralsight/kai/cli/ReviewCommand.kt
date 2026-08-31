package com.pluralsight.kai.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.pluralsight.kai.ai.AiClient
import com.pluralsight.kai.model.MessageDto
import com.pluralsight.kai.model.MessageRole
import com.pluralsight.kai.model.ResponseFormatDto
import com.pluralsight.kai.model.ReviewResult
import com.pluralsight.kai.parser.StructuredParser
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking

// ./gradlew run --args="review --file sample.kt"


private val SYSTEM_PROMPT = """
    You are a Kotlin code reviewer.
    Return ONLY valid JSON with this schema:
    {
      "summary": "string",
      "severity": "LOW|MEDIUM|HIGH",
      "findings": ["string"]
    }
""".trimIndent()


private val logger = KotlinLogging.logger(name = "module")

class ReviewCommand(
    private val client: AiClient,
    private val parser: StructuredParser = StructuredParser()
) : CliktCommand(name = "review") {
    private val file by option("--file").file(mustExist = true, canBeDir = false).required()

    override fun run() = runBlocking {
        val code = file.readText()
        val result = reviewWithRetry(code)
        result.fold(
            onSuccess = { review ->
                echo("Summary : ${review.summary}")
                echo("Severity: ${review.severity}")
                review.findings.forEach { echo(" - $it") }
            },
            onFailure = { cause ->
                logger.warn(cause) { "review parse failed after retry" }
                echo("Could not produce a structured review: ${cause.message}", err = true)
            }
        )
    }

    // Bounded retry: on parse failure, retry once with a corrective prompt, then fail clearly.
    private suspend fun reviewWithRetry(code: String): Result<ReviewResult> {
        val first = requestReview(userPrompt = "Review this Kotlin code:\n$code")
        val parsed = parser.parse<ReviewResult>(first)
        if (parsed.isSuccess) return parsed

        logger.info { "review parse failed, retrying once with a corrective prompt" }
        val second = requestReview(
            userPrompt = """
                Your previous answer was not valid JSON for the required schema.
                Return ONLY valid JSON, no prose, no code fences.
                Review this Kotlin code:
                $code
            """.trimIndent()
        )
        return parser.parse(second)
    }

    private suspend fun requestReview(userPrompt: String): String {
        val response = client.chatEx(
            messages = listOf(
                MessageDto(role = MessageRole.SYSTEM, content = SYSTEM_PROMPT),
                MessageDto(role = MessageRole.USER, content = userPrompt)
            ),
            responseFormat = ResponseFormatDto() // JSON mode — the primary mechanism
        )
        return response.choices.firstOrNull()?.message?.content
            ?: throw IllegalStateException("No assistant message returned")
    }
}

