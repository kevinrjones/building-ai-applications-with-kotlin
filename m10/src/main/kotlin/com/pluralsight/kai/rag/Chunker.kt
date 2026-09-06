package com.pluralsight.kai.rag

class Chunker(
    private val chunkSize: Int = 600,
    private val overlap: Int = 120
) {
    init {
        require(overlap < chunkSize) { "Overlap must be smaller than chunk size" }
    }

    fun chunk(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        val chunks = mutableListOf<String>()
        var start = 0
        while (start < text.length) {
            val end = minOf(start + chunkSize, text.length)
            chunks += text.substring(start, end)
            if (end == text.length) break
            start = end - overlap // step back so consecutive chunks share context
        }
        return chunks
    }
}
