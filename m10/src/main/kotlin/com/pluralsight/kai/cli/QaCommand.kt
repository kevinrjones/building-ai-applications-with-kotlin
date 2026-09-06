package com.pluralsight.kai.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.pluralsight.kai.ai.AiClient
import com.pluralsight.kai.prompt.PromptTemplates
import com.pluralsight.kai.rag.Retriever
import com.pluralsight.kai.safety.SafetyPipeline
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

// ./gradlew run --args="qa --question \"Which paths retry and which do not, and how does that change customer-facing behavior when Billing vs Analytics is down? Cite your sources\" --docs ./docs"
// ./gradlew run --args="qa --question \"Which paths retry and which do not, and how does that change customer-facing behavior when Billing vs Analytics is down? Cite your sources\""

private val logger = KotlinLogging.logger(name = "module")

class QaCommand(
    private val client: AiClient,
    private val retriever: Retriever,
    private val safety: SafetyPipeline = SafetyPipeline()
) : CliktCommand(name = "qa") {
    private val question by option("--question").required()
    private val docs by option("--docs").file(mustExist = true, canBeFile = false).required()

    override fun run() = runBlocking {

        safety.checkInput(question).let {
            if (!it.allowed) {
                echo("Blocked: ${it.reason}", err = true)
                return@runBlocking
            }
        }

        val chunks = retriever.loadChunks(docs)
        if (chunks.isEmpty()) {
            echo("No documents found in ${docs.path}.", err = true)
            return@runBlocking
        }

        val retrieved = withTimeout(30.seconds){ retriever.topK(question, chunks) }

        val safeChunks = retrieved.filter { scored ->
            val check = safety.checkRetrievedChunk(scored.chunk.text)
            if (!check.allowed) logger.warn { "Dropped chunk from ${scored.chunk.source}: ${check.reason}" }
            check.allowed
        }


        val prompt = PromptTemplates.qa.render(
            mapOf("context" to retriever.retrievedContext(safeChunks), "question" to question)
        )
        val answer = client.chat(systemPrompt = prompt.systemPrompt, userPrompt = prompt.userPrompt)
        echo(answer)

    }
}