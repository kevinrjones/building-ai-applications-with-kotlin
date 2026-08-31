package com.pluralsight.kai.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.pluralsight.kai.ai.AiClient
import com.pluralsight.kai.rag.Retriever
import com.pluralsight.kai.safety.SafetyPipeline
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

/**
 * Non-RAG based answers to
 *
 * /gradlew run --args="qa --question \"Which paths retry and which do not, and how does that change customer-facing behavior when Billing vs Analytics is down? Cite your sources\" --docs ./docs"
 *
 * The paths that retry and those that do not are as follows:
 *
 * ### Retry Paths:
 * 1. **API Gateway -> Billing Service**
 *    - Retries: 2 retries with exponential backoff (200 ms, 400 ms).
 *    - Customer-facing behavior when Billing is down: If the circuit is open, existing subscribers can access previously purchased features, but new purchases are blocked, and the gateway emits an alert `ALERT-BILLING-CB-OPEN`.
 *
 * 2. **Billing Service -> PaymentProviderX**
 *    - Retries: 3 retries with jittered exponential backoff (500 ms, 1 s, 2 s +/- 20%).
 *    - Customer-facing behavior when PaymentProviderX is down: In read-only mode, renewal retries continue for up to 6 hours, but new checkout endpoints return a `503` with the message `Try again later`.
 *
 * ### Non-Retry Paths:
 * 1. **API Gateway -> Analytics Pipeline**
 *    - Retries: 0 retries.
 *    - Customer-facing behavior when Analytics is down: Core user requests are unaffected, but the daily KPI report can be delayed by up to 24 hours. If the analytics write fails, the request still succeeds and logs `ANALYTICS_DROPPED_EVENT`.
 *
 * In summary, when Billing is down, it affects new purchases and may block access to features for new users, while existing subscribers can still access previously purchased features. When Analytics is down, core user requests remain unaffected, but reporting may be delayed.
 */
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


        // k = 6: with three sources in the corpus (one of them large), the default k = 3 lets
        // resilience-playbook.md and poisoned.md fill every slot, so retry-policy.md is never
        // retrieved and the conflict-awareness defense has nothing to surface. Widening top-K
        // gets both conflicting sources into the context.
        val retrieved = withTimeout(30.seconds) { retriever.topK(question, chunks, k = 6) }

        // 2. Screen every retrieved chunk; drop flagged chunks WITH an audit log line.
        val safeChunks = retrieved.filter { scored ->
            val check = safety.checkRetrievedChunk(scored.chunk.text)
            if (!check.allowed) logger.warn { "Dropped chunk from ${scored.chunk.source}: ${check.reason}" }
            check.allowed
        }

        val answer =
            client.chat(systemPrompt = retriever.groundedPrompt(safeChunks), userPrompt = "USER QUESTION: $question")

        // 3. Validate the output before showing it.
        safety.checkOutput(answer).let {outputCheck ->
            if (!outputCheck.allowed) {
                echo("Rejected output: ${outputCheck.reason}", err = true)
                return@runBlocking
            }
        }
        echo(answer)
    }
}