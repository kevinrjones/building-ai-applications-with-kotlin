package com.pluralsight.kai.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.pluralsight.kai.ai.AiClient
import kotlinx.coroutines.runBlocking

private const val MAX_INPUT_LENGTH = 8000

/*
./gradlew run --args="summarize --text \"Retrieval-augmented generation (RAG) is a technique that enables large language models (LLMs) to retrieve and incorporate new information from external data sources.[1] With RAG, LLMs first refer to a specified set of documents, then respond to user queries. These documents supplement information from the LLM's pre-existing training data.[2] This allows LLMs to use domain-specific and/or updated information that is not available in the training data.[2] For example, this enables LLM-based chatbots to access internal company data or generate responses based on authoritative sources. The technique was first proposed in 2020 and has since become a widely adopted approach in modern AI systems.RAG improves LLMs by incorporating information retrieval before generating responses.[3] Unlike LLMs that rely on static training data, RAG pulls relevant text from databases, uploaded documents, or web sources.[1] According to Ars Technica, RAG is a way of improving LLM performance, in essence by blending the LLM process with a web search or other document look-up process to help LLMs stick to the facts. This method helps reduce AI hallucinations,[3] which have caused chatbots to describe policies that don't exist, or recommend nonexistent legal cases to lawyers that are looking for citations to support their arguments.[4]RAG also reduces the need to retrain LLMs with new data, saving on computational and financial costs.[1] Beyond efficiency gains, RAG also allows LLMs to include sources in their responses, so users can verify the cited sources. This provides greater transparency, as users can cross-check retrieved content to ensure accuracy and relevance.The term retrieval-augmented generation (RAG) was introduced in a 2020 paper that described combining a parametric language model with a non-parametric external memory accessed through retrieval at inference time.\""
 */


class SummarizeCommand(private val client: AiClient) : CliktCommand("summarize") {
    private val text by option("--text").required()

    override fun run() = runBlocking {

        require(text.isNotBlank()) { "Input text cannot be blank" }
        require(text.length < MAX_INPUT_LENGTH) { "Input text cannot be longer than $MAX_INPUT_LENGTH characters" }

        val prompt = "Summarize the following text: $text in at most 5 bullet points"

        echo(client.chat(systemPrompt = "You are a helpful Kotlin assistant", userPrompt = prompt))
    }
}