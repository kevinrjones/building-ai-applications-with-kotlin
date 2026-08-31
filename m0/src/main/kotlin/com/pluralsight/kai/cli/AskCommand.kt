package com.pluralsight.kai.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.pluralsight.kai.ai.AiClient
import kotlinx.coroutines.runBlocking

class AskCommand(private val client: AiClient) : CliktCommand(name = "ask") {
    private val prompt by argument()

    override fun run() = runBlocking {

        echo(client.chat("You are a helpful Kotlin assistant", userPrompt = prompt))

    }
}