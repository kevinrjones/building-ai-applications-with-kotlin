package com.pluralsight.kai.prompt

data class RenderedPrompt(val systemPrompt: String, val userPrompt: String)

fun interface PromptTemplate {
    fun render(input: Map<String, String>): RenderedPrompt
}

object PromptTemplates {
    val summarize = PromptTemplate { input ->
        RenderedPrompt(
            systemPrompt = """
                You are a Kotlin assistant.
                Summarize the supplied input.
                Follow the style and maximum item count requested by the user.
            """.trimIndent(),
            userPrompt = """
                Style: ${input["style"] ?: "bullet"}
                Maximum items: ${input["maxItems"] ?: "5"}

                Input:
                <text>
                ${input["text"].orEmpty()}
                </text>
            """.trimIndent()
        )
    }

    val rewrite = PromptTemplate { input ->
        RenderedPrompt(systemPrompt = """
            You are a Kotlin assistant. Rewrite the supplied input text in the style requested by the user.

                Example 1:
                Input: The function, which is called by the caller, will then proceed to return the value that was computed.
                Output: The function returns the computed value.

                Example 2:
                Input: It is possible that in some circumstances the request might potentially fail.
                Output: The request can fail.

        """.trimIndent(),
            userPrompt = """
                Style: ${input["style"] ?: "concise"}
                Input:
                <text>
                ${input["text"].orEmpty()}
                </text>
            """.trimIndent())

    }

    val classify = PromptTemplate { input ->
        RenderedPrompt(
            systemPrompt = """
                You are a Kotlin assistant. Classify the supplied input text into exactly one category:
                QUESTION, BUG_REPORT, FEATURE_REQUEST, or OTHER.

                Example 1:
                Input: How do I cancel a coroutine?
                Output: QUESTION

                Example 2:
                Input: The app crashes when I pass an empty list.
                Output: BUG_REPORT
            """.trimIndent(),
            userPrompt = """
                Input:
                <text>
                ${input["text"].orEmpty()}
                </text>
                Output:
            """.trimIndent()
        )
    }

    val review = PromptTemplate { input ->
        RenderedPrompt(
            systemPrompt = """
                You are a Kotlin code reviewer. 
                Return ONLY valid JSON 
            """.trimIndent(),
            userPrompt = """
                ${input["correction"].orEmpty()}
                Please review the following Kotlin code:
                <code>
                ${input["code"].orEmpty()}
                </code>
            """.trimIndent()
        )
    }

    val stream = PromptTemplate { values ->
        RenderedPrompt(
            systemPrompt = "You are a helpful Kotlin assistant.",
            userPrompt = values["text"].orEmpty()
        )
    }

    val assistant = PromptTemplate { values ->
        RenderedPrompt(
            systemPrompt = "You are a helpful Kotlin assistant. Use tools when they help.",
            userPrompt = values["text"].orEmpty()
        )
    }


}