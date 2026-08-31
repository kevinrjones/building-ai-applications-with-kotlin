package com.pluralsight.kai.safety

data class SafetyResult(val allowed: Boolean, val reason: String? = null)

class SafetyPipeline {
    // Deny-patterns are a first line of defense, not a complete one.
    private val denyPatterns = listOf(
        Regex("ignore (all )?previous instructions", RegexOption.IGNORE_CASE),
        Regex("reveal .*system prompt", RegexOption.IGNORE_CASE),
        Regex("you are now unrestricted", RegexOption.IGNORE_CASE)
    )

    fun checkInput(text: String): SafetyResult {
        if (text.length > 20_000) return SafetyResult(false, "Input too long")
        denyPatterns.firstOrNull { it.containsMatchIn(text) }
            ?.let { return SafetyResult(false, "Injection pattern detected: ${it.pattern}") }
        return SafetyResult(true)
    }

    // Retrieved content is DATA, not instructions — run it through the same checks
    // before it enters the prompt, and keep it demarcated inside <context> tags.
    fun checkRetrievedChunk(text: String): SafetyResult = checkInput(text)

    fun checkOutput(text: String): SafetyResult {
        if (text.isBlank()) return SafetyResult(false, "Empty model output")
        return SafetyResult(true)
    }
}
