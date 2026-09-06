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
import com.pluralsight.kai.prompt.PromptTemplates
import com.pluralsight.kai.prompt.RenderedPrompt
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking

// ./gradlew run --args="review --file sample.kt"


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
        val first = requestReview(PromptTemplates.review.render(mapOf("code" to code)))
        val parsed: Result<ReviewResult> = parser.parse<ReviewResult>(first)
        if (parsed.isSuccess) return parsed

        logger.info { "review parse failed, retrying once with a corrective prompt" }
        echo("review parse failed, retrying once with a corrective prompt" )
        val second = requestReview(
            PromptTemplates.review.render(
                mapOf(
                    "code" to code,
                    "correction" to """
                        Your previous answer was not valid JSON for the required schema.
                        Return ONLY valid JSON with this schema:
                {
                  "summary": "string",
                  "severity": "LOW|MEDIUM|HIGH",
                  "findings": ["string"]
                }
                        , no prose, no code fences.
                    """.trimIndent()
                )
            )
        )
        return parser.parse(second)
    }

    private suspend fun requestReview(prompt: RenderedPrompt): String {
        val response = client.chatEx(
            messages = listOf(
                MessageDto(role = MessageRole.SYSTEM, content = prompt.systemPrompt),
                MessageDto(role = MessageRole.USER, content = prompt.userPrompt)
            ),
            responseFormat = ResponseFormatDto() // JSON mode — the primary mechanism
        )
        return response.choices.firstOrNull()?.message?.content
            ?: throw IllegalStateException("No assistant message returned")
    }
}
