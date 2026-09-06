package com.pluralsight.kai.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.pluralsight.kai.ai.AiClient
import com.pluralsight.kai.prompt.PromptTemplates
import kotlinx.coroutines.runBlocking

private const val MAX_INPUT_LENGTH = 8000

/*
./gradlew run --args="rewrite --style concise --text \"Hey team — just a quick note that the release is kind of delayed again because a couple of bugs popped up in payments and login, and QA found some weird edge-case behavior on iOS. We’re working on it and hopefully can ship soon, maybe Friday, but no promises yet. Also support is getting more tickets about failed password resets, so if anyone can jump in there that would help.\""
*/

class RewriteCommand(private val client: AiClient) : CliktCommand(name = "rewrite") {
    private val text by option("--text").required()
    private val style by option("--style").default("bullet")

    override fun run() = runBlocking {

        require(text.isNotBlank()) { "Input text cannot be blank" }
        require(text.length < MAX_INPUT_LENGTH) { "Input text cannot be longer than $MAX_INPUT_LENGTH characters" }

        val renderedPrompt = PromptTemplates.rewrite.render(mapOf("text" to text, "style" to style))

        echo(client.chat(
            systemPrompt = renderedPrompt.systemPrompt,
            userPrompt = renderedPrompt.userPrompt
        ))
    }
}