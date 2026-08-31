package com.pluralsight.kai.prompt

// Teaching note: acknowledge the `Map<String, String>` input is stringly-typed — a deliberate simplicity tradeoff;
// mention typed parameter objects as the production upgrade.
fun interface PromptTemplate {
    fun render(input: Map<String, String>): String
}

object PromptTemplates {
    val summarize = PromptTemplate { values ->
        """
        You are a Kotlin assistant.
        Summarize the input in ${values["style"] ?: "bullet"} format.
        Keep to ${values["maxItems"] ?: "5"} items.
        Input:
        ${values["text"].orEmpty()}
        """.trimIndent()
    }

    val rewrite = PromptTemplate { values ->
        """
        You are a Kotlin assistant. Rewrite the input text in a ${values["style"] ?: "concise"} style.

        Example 1:
        Input: The function, which is called by the caller, will then proceed to return the value that was computed.
        Output: The function returns the computed value.

        Example 2:
        Input: It is possible that in some circumstances the request might potentially fail.
        Output: The request can fail.

        Input: ${values["text"].orEmpty()}
        Output:
        """.trimIndent()
    }

    val classify = PromptTemplate { values ->
        """
        You are a Kotlin assistant. Classify the input text into exactly one category:
        QUESTION, BUG_REPORT, FEATURE_REQUEST, or OTHER.

        Example 1:
        Input: How do I cancel a coroutine?
        Output: QUESTION

        Example 2:
        Input: The app crashes when I pass an empty list.
        Output: BUG_REPORT

        Input: ${values["text"].orEmpty()}
        Output:
        """.trimIndent()
    }

    val summarizeHistory = PromptTemplate { values ->
        """
        Condense the following conversation turns into one short paragraph.
        Preserve decisions, names, and facts the assistant needs to stay coherent.
        Turns:
        ${values["turns"].orEmpty()}
        """.trimIndent()
    }

}