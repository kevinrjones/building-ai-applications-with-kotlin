package com.pluralsight.kai.rag

import com.pluralsight.kai.ai.AiClient
import java.io.File

data class DocumentChunk(val source: String, val text: String)

data class ScoredChunk(val chunk: DocumentChunk, val score: Double)


class Retriever(
    private val client: AiClient,
    private val chunker: Chunker = Chunker(),
    private val embeddingStore: DocumentEmbeddingStore? = null

) {
    fun loadChunks(docsDir: File): List<DocumentChunk> =
        docsDir.walkTopDown()
            .filter { it.isFile && it.extension in setOf("md", "txt") }
            .flatMap { file -> chunker.chunk(file.readText()).map { DocumentChunk(file.name, it) } }
            .toList()

    suspend fun topK(question: String, chunks: List<DocumentChunk>, k: Int = 3): List<ScoredChunk> {
        if (chunks.isEmpty()) return emptyList()

        // call the embedding store to get the chunks for the document
        val chunkEmbeddings = embeddingStore?.load() ?:
            client.embed(chunks.map { it.text }).also { embeddingStore?.save(chunks, it) }

        // call the embedding store to get the chunks for the query
        val queryEmbedding = client.embed(listOf(question)).first()

        // calculate the cosines
        return chunks.indices
            .map { i -> ScoredChunk(chunks[i], Cosine.similarity(queryEmbedding, chunkEmbeddings[i])) }
            .sortedByDescending { it.score }
            .take(k)
    }

    fun retrievedContext(retrieved: List<ScoredChunk>): String = buildString {
        retrieved.forEach { scored ->
            appendLine("[chunk — source: ${scored.chunk.source}]")
            appendLine(scored.chunk.text)
        }
    }

    fun groundedPrompt(retrieved: List<ScoredChunk>): String = buildString {
        appendLine("Answer using ONLY the material inside <context>. Cite sources by filename.")
        appendLine("<context>")
        append(retrievedContext(retrieved))
        appendLine("</context>")
    }


}