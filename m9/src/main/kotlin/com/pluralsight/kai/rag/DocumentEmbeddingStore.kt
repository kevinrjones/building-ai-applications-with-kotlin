package com.pluralsight.kai.rag

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

interface DocumentEmbeddingStore {
    fun load(): List<List<Double>>?

    fun save(chunks: List<DocumentChunk>, embeddings: List<List<Double>>)
}

class FileDocumentEmbeddingStore(
    private val file: File,
    private val json: Json = Json { prettyPrint = true }
) : DocumentEmbeddingStore {
    override fun load(): List<List<Double>>? {
        if (!file.exists()) return null
        return runCatching {
            json.decodeFromString(PersistedDocumentEmbeddings.serializer(), file.readText()).embeddings
        }.getOrNull()
    }

    override fun save(chunks: List<DocumentChunk>, embeddings: List<List<Double>>) {
        file.parentFile?.mkdirs()
        val payload = PersistedDocumentEmbeddings(embeddings = embeddings)
        file.writeText(json.encodeToString(PersistedDocumentEmbeddings.serializer(), payload))
    }
}

@Serializable
private data class PersistedDocumentEmbeddings(
    val embeddings: List<List<Double>>
)