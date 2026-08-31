package com.pluralsight.kai.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.pluralsight.kai.ai.AiClient
import com.pluralsight.kai.prompt.PromptTemplates
import kotlinx.coroutines.runBlocking

/*
./gradlew run --args="classify --text \"How do I cancel a coroutine without leaking work?\""
./gradlew run --args="classify --text \"After upgrading to Kotlin 2.0, ./gradlew test fails with NoSuchMethodError in kotlinx.coroutines.\""
./gradlew run --args="classify --text \"Please add a --json flag to summarize so CI jobs can parse the output\""
 */

class ClassifyCommand(private val client: AiClient) : CliktCommand(name = "classify") {
    private val text by option("--text").required()

    override fun run() = runBlocking {
        val prompt = PromptTemplates.classify.render(mapOf("text" to text))
        echo(client.chat(systemPrompt = "You are a helpful Kotlin assistant.", userPrompt = prompt).trim())
    }
}
