package com.pluralsight.kai.eval

import kotlinx.serialization.Serializable
import kotlin.time.measureTime

/**
 * One eval case: a prompt to send through the full pipeline (safety checks included)
 * and a crude substring assertion on the answer.
 *
 * `useRag = true` routes the prompt through the retriever (same grounded flow as the
 * `qa` command); otherwise it is a plain chat request.
 */
@Serializable
data class EvalCase(
    val name: String,
    val prompt: String,
    val mustContain: String,
    val useRag: Boolean = false
)

@Serializable
data class EvalSuite(val name: String, val cases: List<EvalCase>)

data class EvalResult(val name: String, val pass: Boolean, val durationMs: Long)

class EvalRunner {
    suspend fun run(suite: EvalSuite, ask: suspend (EvalCase) -> String): List<EvalResult> {
        val results = mutableListOf<EvalResult>()
        for (case in suite.cases) {
            var response = ""
            val duration = measureTime { response = ask(case) }
            val pass = response.contains(case.mustContain, ignoreCase = true)
            results += EvalResult(case.name, pass, duration.inWholeMilliseconds)
        }
        return results
    }
}

fun List<EvalResult>.report(): String =
    joinToString("\n") { "${it.name}: ${if (it.pass) "PASS" else "FAIL"} (${it.durationMs} ms)" } +
        "\n${count { it.pass }}/$size passed"
