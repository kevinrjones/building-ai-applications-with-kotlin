package com.pluralsight.kai.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.pluralsight.kai.ai.AiClient
import com.pluralsight.kai.eval.EvalCase
import com.pluralsight.kai.eval.EvalRunner
import com.pluralsight.kai.eval.EvalSuite
import com.pluralsight.kai.eval.report
import com.pluralsight.kai.rag.DocumentChunk
import com.pluralsight.kai.rag.Retriever
import com.pluralsight.kai.safety.SafetyPipeline
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File

private val logger = KotlinLogging.logger(name = "module")

/**
 * Runs a JSON eval suite from `suites/<name>.json` through the FULL pipeline —
 * the same SafetyPipeline (and, for RAG cases, the same retriever + grounded
 * prompt) used by the real commands. A blocked prompt is itself a result: the
 * `injection-defense` case passes because the block message is the expected answer.
 *
 * ./gradlew -q --console=plain run --args="eval --suite smoke"
 */
class EvalCommand(
    private val client: AiClient,
    private val retriever: Retriever,
    private val safety: SafetyPipeline = SafetyPipeline()
) : CliktCommand(name = "eval") {
    private val suite by option("--suite").required()
    private val suitesDir by option("--suitesDir").file(canBeFile = false).default(File("suites"))
    private val docs by option("--docs").file(canBeFile = false).default(File("docs"))

    private val json = Json { ignoreUnknownKeys = true }

    override fun run() = runBlocking {
        val suiteFile = File(suitesDir, "$suite.json")
        if (!suiteFile.exists()) {
            echo("Suite not found: ${suiteFile.path}", err = true)
            return@runBlocking
        }

        val parsed = json.decodeFromString<EvalSuite>(suiteFile.readText())
        echo("Running suite '${parsed.name}' (${parsed.cases.size} cases)")

        // Chunks are only loaded if at least one case actually needs RAG.
        val chunks = lazy { retriever.loadChunks(docs) }

        val results = EvalRunner().run(parsed) { case -> answer(case, chunks) }
        echo(results.report())
        logger.info { "eval suite=${parsed.name} passed=${results.count { it.pass }}/${results.size}" }

        if (results.any { !it.pass }) throw ProgramResult(1)
    }

    private suspend fun answer(case: EvalCase, chunks: Lazy<List<DocumentChunk>>): String {
        // 1. Same input screening as every real command.
        val input = safety.checkInput(case.prompt)
        if (!input.allowed) return "Blocked: ${input.reason}"

        // 2. Plain chat, or the full grounded-RAG flow for useRag cases.
        val answer = if (case.useRag) {
            val retrieved = retriever.topK(case.prompt, chunks.value, k = 6)
            val safeChunks = retrieved.filter { scored ->
                val check = safety.checkRetrievedChunk(scored.chunk.text)
                if (!check.allowed) logger.warn { "Dropped chunk from ${scored.chunk.source}: ${check.reason}" }
                check.allowed
            }
            client.chat(
                systemPrompt = retriever.groundedPrompt(safeChunks),
                userPrompt = "USER QUESTION: ${case.prompt}"
            )
        } else {
            client.chat(
                systemPrompt = "You are a concise, helpful Kotlin assistant.",
                userPrompt = case.prompt
            )
        }

        // 3. Same output validation as every real command.
        val output = safety.checkOutput(answer)
        return if (output.allowed) answer else "Rejected output: ${output.reason}"
    }
}
