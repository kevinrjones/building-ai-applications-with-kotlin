package com.pluralsight.kai.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.pluralsight.kai.ai.AiClient
import com.pluralsight.kai.prompt.PromptTemplates
import kotlinx.coroutines.runBlocking

// The command

/*
./gradlew run --args="summarize --style haiku --text \"Retrieval-augmented generation (RAG) is a technique that enables large language models (LLMs) to retrieve and incorporate new information from external data sources. With RAG, LLMs first refer to a specified set of documents, then respond to user queries. These documents supplement information from the LLM's pre-existing training data. This allows LLMs to use domain-specific and/or updated information that is not available in the training data. For example, this enables LLM-based chatbots to access internal company data or generate responses based on authoritative sources. The technique was first proposed in 2020 and has since become a widely adopted approach in modern AI systems.RAG improves LLMs by incorporating information retrieval before generating responses. Unlike LLMs that rely on static training data, RAG pulls relevant text from databases, uploaded documents, or web sources. According to Ars Technica, RAG is a way of improving LLM performance, in essence by blending the LLM process with a web search or other document look-up process to help LLMs stick to the facts. This method helps reduce AI hallucinations, which have caused chatbots to describe policies that don't exist, or recommend nonexistent legal cases to lawyers that are looking for citations to support their arguments.RAG also reduces the need to retrain LLMs with new data, saving on computational and financial costs. Beyond efficiency gains, RAG also allows LLMs to include sources in their responses, so users can verify the cited sources. This provides greater transparency, as users can cross-check retrieved content to ensure accuracy and relevance.The term retrieval-augmented generation (RAG) was introduced in a 2020 paper that described combining a parametric language model with a non-parametric external memory accessed through retrieval at inference time.\""
./gradlew run --args="rewrite --style concise --text \"Hey team — just a quick note that the release is kind of delayed again because a couple of bugs popped up in payments and login, and QA found some weird edge-case behavior on iOS. We’re working on it and hopefully can ship soon, maybe Friday, but no promises yet. Also support is getting more tickets about failed password resets, so if anyone can jump in there that would help.\""

Before: The release is delayed due to bugs in payments and login, as well as an edge case on iOS. We're working to ship by Friday, but no guarantees.
Support is receiving more tickets about failed password resets, so any assistance there would be appreciated.

After: The release is delayed due to bugs in payments and login, plus QA found issues on iOS. We're working on it and hope to ship by Friday, but no guarantees.
Support also needs help with failed password resets.
 */


private const val MAX_INPUT_LENGTH = 8000

class RewriteCommand(private val client: AiClient) : CliktCommand(name = "rewrite") {
    private val text by option("--text").required()
    private val style by option("--style").default("bullet")

    override fun run() = runBlocking {

        require(text.isNotBlank()) { "Input text cannot be blank" }
        require(text.length < MAX_INPUT_LENGTH) { "Input text cannot be longer than $MAX_INPUT_LENGTH characters" }

        val prompt = PromptTemplates.rewrite.render(mapOf("text" to text, "style" to style))

        echo(client.chat(systemPrompt = "You are a helpful Kotlin assistant", userPrompt = prompt))
    }
}