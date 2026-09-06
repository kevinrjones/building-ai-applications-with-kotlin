package com.pluralsight.kai.com.pluralsight.kai.eval

import com.pluralsight.kai.eval.EvalCase
import com.pluralsight.kai.eval.EvalRunner
import com.pluralsight.kai.eval.EvalSuite
import com.pluralsight.kai.eval.report
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EvalRunnerTest {

    private val suite = EvalSuite(
        name = "test",
        cases = listOf(
            EvalCase(name = "pass-case", prompt = "p1", mustContain = "suspend"),
            EvalCase(name = "fail-case", prompt = "p2", mustContain = "missing"),
            EvalCase(name = "rag-case", prompt = "p3", mustContain = "retry-policy.md", useRag = true)
        )
    )

    @Test
    fun `mustContain is checked case-insensitively per case`() = runBlocking {
        val results = EvalRunner().run(suite) { case ->
            when (case.name) {
                "pass-case" -> "Coroutines use SUSPEND functions"
                "rag-case" -> "According to retry-policy.md we retry 3 times"
                else -> "nothing relevant"
            }
        }

        assertEquals(3, results.size)
        assertTrue(results[0].pass)
        assertFalse(results[1].pass)
        assertTrue(results[2].pass)
    }

    @Test
    fun `report lists one line per case plus a summary`() = runBlocking {
        val results = EvalRunner().run(suite) { "suspend retry-policy.md" }
        val report = results.report()

        assertTrue(report.contains("pass-case: PASS"))
        assertTrue(report.contains("fail-case: FAIL"))
        assertTrue(report.contains("2/3 passed"))
    }

    @Test
    fun `suite json deserializes with optional useRag flag`() {
        val json = """
            {
              "name": "smoke",
              "cases": [
                { "name": "a", "prompt": "q", "mustContain": "x" },
                { "name": "b", "prompt": "q", "mustContain": "y", "useRag": true }
              ]
            }
        """.trimIndent()

        val parsed = Json { ignoreUnknownKeys = true }.decodeFromString<EvalSuite>(json)

        assertEquals("smoke", parsed.name)
        assertFalse(parsed.cases[0].useRag)
        assertTrue(parsed.cases[1].useRag)
    }
}
